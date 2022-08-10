package proxyLogic;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.Proxy;
import kong.unirest.Unirest;

public class AcquireHandler implements HttpHandler {

	private Proxy prx = null;
	
	public AcquireHandler(Proxy prx) {
		this.prx = prx;
	}

	public Integer pickReplica() {
		// per il momento e raound robin
		Integer port = ((List<Integer>) this.prx.getMs().get("ports")).get(0);
		//Collections.rotate((List<Integer>) this.prx.getMs().get("ports"), 1);
		return port;
	}

	public void forwardRqt(Integer port, HttpExchange req) {
		// qui la logica per l'effettivo foward della richiesta
		Constructor<? extends Runnable> c = null;
		try {
			c = this.prx.getPrxLogic().getDeclaredConstructor(Integer.class, HttpExchange.class, Proxy.class);
			this.prx.getThreadpool().submit(c.newInstance(port, req, this.prx));
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		this.forwardRqt(this.pickReplica(), exchange);
	}
}
