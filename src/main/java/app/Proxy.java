package app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.bson.Document;

import com.sun.net.httpserver.HttpServer;

import proxyLogic.AcquireHandler;

@SuppressWarnings("restriction")
public class Proxy {

	private HttpServer server = null;
	private int port = -1;
	private int backlogSize = -1;
	private Document ms = null;
	private ThreadPoolExecutor threadpool = null;
	private Class<? extends Runnable> prxLogic = null;

	public Proxy(Class<? extends Runnable> prxLogic, int port, int backlogSize) {
		this.port = port;
		this.backlogSize = backlogSize;
		this.prxLogic = prxLogic;
		try {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), this.backlogSize);
			this.server.createContext("/", new AcquireHandler(this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.initThreadpool();
	}

	public void start() {
		this.server.start();
	}

	public void stop() {
		this.server.stop(2);
	}

	public Document getMs() {
		return ms;
	}

	public void setMs(Document ms) {
		this.ms = ms;
	}

	public void initThreadpool() {
		this.threadpool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	}

	public ThreadPoolExecutor getThreadpool() {
		return threadpool;
	}

	public Class<? extends Runnable> getPrxLogic() {
		return prxLogic;
	}
}
