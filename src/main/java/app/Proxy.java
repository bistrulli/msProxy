package app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.bson.Document;

import com.sun.net.httpserver.HttpServer;

import mnt.Event;
import mnt.EventMnt;
import proxyLogic.AcquireHandler;

@SuppressWarnings("restriction")
public class Proxy {

	private HttpServer server = null;
	private int port = -1;
	private int backlogSize = -1;
	private ThreadPoolExecutor threadpool = null;
	private Class<? extends Runnable> prxLogic = null;
	private ConcurrentLinkedQueue<Event> events=null;
	private EventMnt monitor=null;
	private Document ms;

	public Proxy(Class<? extends Runnable> prxLogic, int port, int backlogSize,Document ms) {
		this.port = port;
		this.backlogSize = backlogSize;
		this.prxLogic = prxLogic;
		try {
			this.server = HttpServer.create(new InetSocketAddress("localhost", port), this.backlogSize);
			this.server.createContext("/", new AcquireHandler(this));
			this.server.setExecutor((ThreadPoolExecutor) Executors.newFixedThreadPool(100));
			//this.server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.initThreadpool();
		this.events=new ConcurrentLinkedQueue<Event>(); 
		this.ms=ms;
		this.monitor=new EventMnt(this,this.ms.getString("name"));
		this.monitor.start();
	}

	public void start() {
		this.server.start();
	}

	public void stop() {
		this.server.stop(2);
	}

	public void initThreadpool() {
		this.threadpool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		//this.threadpool = (ThreadPoolExecutor) Executors.newFixedThreadPool(200);
	}

	public ThreadPoolExecutor getThreadpool() {
		return threadpool;
	}

	public Class<? extends Runnable> getPrxLogic() {
		return prxLogic;
	}
	
	public void addEvent(Event evt) {
		this.events.add(evt);
	}

	public ConcurrentLinkedQueue<Event> getEvents() {
		return events;
	}

	public void setMs(Document ms) {
		this.ms=ms;
	}

	public Document getMs() {
		return ms;
	}
}
