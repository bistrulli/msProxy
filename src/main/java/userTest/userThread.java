package userTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class userThread extends Thread{
	
	private static Long nrq = 0l;
	private static Long cumRt = 0l;
	
	private static boolean toStop=false;
	
	public static void stop(boolean stop) {
		userThread.toStop=stop;
	}
	
	public synchronized static void addRt(long rt) {
		userThread.cumRt += rt;
		userThread.nrq += 1;
	}

	public static float avgRt() {
		return userThread.cumRt.floatValue() / userThread.nrq.floatValue();
	}

	private HttpClient client=null;
	private HttpRequest request=null;
	
	public userThread() {
		this.client = HttpClient.newHttpClient();
		this.request = HttpRequest.newBuilder()
		      .uri(URI.create("http://localhost:3000/"))
		      .build();
	}
	
	private void doWorkload() {
		try {
			HttpResponse<String> resp =this.client.send(request, BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		super.run();
		while(!userThread.toStop) {
			long st=System.nanoTime();
			this.doWorkload();
			userThread.addRt(System.nanoTime()-st);
		}
	}
}
