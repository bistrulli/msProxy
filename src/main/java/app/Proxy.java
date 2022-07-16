package app;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;


//TODO da definire la logica del proxy. Includo un semplice http server con infinite threadpool
public class Proxy {
	
	private HttpServer server=null;
	private int port=-1;
	private int backlogSize=-1;

	public Proxy(Class<? extends HttpHandler> prxLogic,int port, int backlogSize) {
		this.port=port;
		this.backlogSize=backlogSize;
		try {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1",port), this.backlogSize);
			Constructor<? extends HttpHandler> c;
			try {
				c = prxLogic.getDeclaredConstructor();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				this.server.createContext("/",prxLogic.newInstance());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
