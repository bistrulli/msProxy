package proxyLogic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import app.Proxy;
import kong.unirest.Header;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import mnt.Event;

public class SimpleProxy implements Runnable {

	private String tgtHost = null;
	private Integer tgtPort = null;
	private HttpExchange req = null;
	private Proxy prx = null;

	public String getTgtHost() {
		return this.tgtHost;
	}

	public Integer getTgtPort() {
		return this.tgtPort;
	}

	public HttpExchange getReq() {
		return req;
	}

	public SimpleProxy(Integer tgtPort, HttpExchange req, Proxy prx) {
		this.tgtPort = tgtPort;
		this.req = req;
		this.prx = prx;
	}

	@Override
	public void run() {

		System.out.println(this.req.getRequestMethod() + "Request");

		switch (this.req.getRequestMethod()) {
		case "GET": {
			long st = (new Date()).getTime();

			String requestedURL = "http://%s:%d%s"
					.formatted(new Object[] { this.req.getRequestHeaders().getFirst("Host").split(":")[0], this.tgtPort,
							this.req.getRequestURI() });

			kong.unirest.HttpResponse<String> resp = Unirest.get(URI.create(requestedURL).toString()).asString();
			this.copyRespHeader(resp, req);

			OutputStream outputStream = req.getResponseBody();
			try {
				req.sendResponseHeaders(resp.getStatus(), resp.getBody().length());
				outputStream.write(resp.getBody().getBytes());
				resp = null;
				outputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Event e = new Event(st, (new Date()).getTime());
			this.prx.addEvent(e);
			break;
		}
		case "POST": {
			long st = (new Date()).getTime();

			String requestedURL = "http://%s:%d%s"
					.formatted(new Object[] { this.req.getRequestHeaders().getFirst("Host").split(":")[0], this.tgtPort,
							this.req.getRequestURI() });

			String mimeType = req.getRequestHeaders().get("Content-type").get(0);
			String rqstBody = this.getRqstBody(req);
			HttpResponse<String> resp=null;
			if(mimeType.contains("application/x-www-form-urlencoded")) {
				resp = Unirest.post(URI.create(requestedURL).toString()).contentType(mimeType)
						.fields(this.getPostParam(rqstBody, mimeType)).asString();
				this.copyRespHeader(resp, req);
			}
			if(mimeType.contains("application/json")) {
				resp = Unirest.post(URI.create(requestedURL).toString()).contentType(mimeType)
						.body(rqstBody).asString();
				this.copyRespHeader(resp, req);
			}

			OutputStream outputStream = req.getResponseBody();
			try {
				req.sendResponseHeaders(resp.getStatus(), resp.getBody().length());
				outputStream.write(resp.getBody().getBytes());
				resp = null;
				outputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Event e = new Event(st, (new Date()).getTime());
			this.prx.addEvent(e);
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + this.req.getRequestMethod());
		}
	}

	private void copyRespHeader(kong.unirest.HttpResponse<String> resp, HttpExchange req) {
		List<Header> headerList = resp.getHeaders().all();
		for (Header header : headerList) {
			req.getResponseHeaders().set(header.getName(), header.getValue());
		}
		req.getResponseHeaders().remove(org.apache.http.HttpHeaders.CONNECTION);
		req.getResponseHeaders().remove(org.apache.http.HttpHeaders.TRANSFER_ENCODING);
	}

	private String getRqstBody(HttpExchange req) {
		StringBuilder sb = new StringBuilder();
		InputStream ios = req.getRequestBody();
		int i;
		try {
			while ((i = ios.read()) != -1) {
				sb.append((char) i);
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return sb.toString();
	}

	private HashMap<String, Object> getPostParam(String reqBody, String mimeType) {
		String[] params = reqBody.split("&");
		HashMap<String, Object> fields = new HashMap<>();
		for (String param : params) {
			String key = param.split("=")[0];
			String value = param.split("=")[1];
			try {
				key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
				value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
				System.out.println("%s=%s".formatted(new String[] { key, value }));
			} catch (Exception e) {
				e.printStackTrace();
			}
			fields.put(key, value);
		}
		return fields;
	}
}
