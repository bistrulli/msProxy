package proxyLogic;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import kong.unirest.Unirest;

public class SimpleProxy implements HttpHandler {

	private static String tgtHost = null;
	private static Integer tgtPort = null;

	public static String getTgtHost() {
		return tgtHost;
	}

	public static void setTgtHost(String tgtHost) {
		SimpleProxy.tgtHost = tgtHost;
	}

	public static Integer getTgtPort() {
		return tgtPort;
	}

	public static void setTgtPort(Integer tgtPort) {
		SimpleProxy.tgtPort = tgtPort;
	}

	private HttpClient client;
	private HttpRequest request;

	public SimpleProxy() {
		// this.client = HttpClient.newHttpClient();
	}

	@Override
	public void handle(HttpExchange req) throws IOException {

//		this.request = HttpRequest.newBuilder()
//			      .uri(URI.create("http://%s:%d/%d".formatted(new Object[] {SimpleProxy.tgtHost,SimpleProxy.tgtPort,System.currentTimeMillis()})))
//			      .build();
		
		kong.unirest.HttpResponse<String> resp=null;
		//resp = this.client.send(request, BodyHandlers.ofString());
		resp = Unirest.get(URI.create("http://%s:%d/%d".formatted(new Object[] {SimpleProxy.tgtHost,SimpleProxy.tgtPort,System.currentTimeMillis()})).toString())
			  .header("Connection", "close").asString();
		

		req.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
		req.getResponseHeaders().set("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
		OutputStream outputStream = req.getResponseBody();
		req.sendResponseHeaders(200, resp.getBody().length());
		outputStream.write(resp.getBody().getBytes());
		//outputStream.flush();
		outputStream.close();
		outputStream = null;
	}

}
