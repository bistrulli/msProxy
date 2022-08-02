package proxyLogic;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;

import app.Proxy;
import kong.unirest.Unirest;

public class SimpleProxy implements Runnable {

	private String tgtHost = null;
	private Integer tgtPort = null;
	private HttpExchange req = null;
	private MongoClient client;
	private Proxy prx = null;
	private MongoCollection<Document> rt = null;

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
		this.client = MongoClients.create("mongodb://localhost:27017");
		this.prx = prx;

		MongoDatabase msDb = this.client.getDatabase(this.prx.getMs().getString("name"));
		this.rt = msDb.getCollection("rt");
		//System.out.println(this.rt.countDocuments());
	}

	@Override
	public void run() {
		switch (this.req.getRequestMethod()) {
		case "GET": {
			long st = System.currentTimeMillis();
			String requestedURL = "http://%s:%d%s"
					.formatted(new Object[] { this.req.getRequestHeaders().getFirst("Host").split(":")[0], this.tgtPort,
							this.req.getRequestURI() });

			kong.unirest.HttpResponse<String> resp = Unirest.get(URI.create(requestedURL).toString())
					.header("Connection", "close").asString();

			req.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
			req.getResponseHeaders().set("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
			OutputStream outputStream = req.getResponseBody();
			try {
				req.sendResponseHeaders(200, resp.getBody().length());
				outputStream.write(resp.getBody().getBytes());
				outputStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			this.rt.insertOne(new Document().append("st", st).append("end", System.currentTimeMillis()));
			
			this.client.close();
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + this.req.getRequestMethod());
		}
	}

}
