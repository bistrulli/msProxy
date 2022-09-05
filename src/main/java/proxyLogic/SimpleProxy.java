package proxyLogic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import app.Proxy;
import kong.unirest.Header;
import kong.unirest.HttpRequestWithBody;
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
		switch (this.req.getRequestMethod()) {
		case "GET": {
			long st = (new Date()).getTime();

			String requestedURL = "http://%s:%d%s"
					.formatted(new Object[] { this.req.getRequestHeaders().getFirst("Host").split(":")[0], this.tgtPort,
							this.req.getRequestURI() });

			System.out.println(requestedURL);

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
			
			
			HttpRequestWithBody preq = Unirest.post(URI.create(requestedURL).toString());
			preq=this.forwardPostParam(preq, req);
			kong.unirest.HttpResponse<String> resp = preq.asString();
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
		default:
			throw new IllegalArgumentException("Unexpected value: " + this.req.getRequestMethod());
		}
	}

	private void copyRespHeader(kong.unirest.HttpResponse<String> resp, HttpExchange req) {
		//copio l'header della risposta
		List<Header> headerList = resp.getHeaders().all();
		for (Header header : headerList) {
			if (!header.getName().equalsIgnoreCase("Transfer-Encoding"))
				req.getResponseHeaders().set(header.getName(), header.getValue());
		}
	}

	private HttpRequestWithBody forwardPostParam(HttpRequestWithBody preq, HttpExchange req) {
		//di fatto copio il body della richiesta
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
		preq.body(sb.toString());
		return preq;
	}

}
