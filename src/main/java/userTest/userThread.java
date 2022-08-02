package userTest;

import java.net.URI;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class userThread extends Thread {

	private static Long nrq = 0l;
	private static Long cumRt = 0l;

	private static boolean toStop = false;

	public static void stop(boolean stop) {
		userThread.toStop = stop;
	}

	public synchronized static void addRt(long rt) {
		userThread.cumRt += rt;
		userThread.nrq += 1;
	}

	public static float avgRt() {
		return userThread.cumRt.floatValue() / userThread.nrq.floatValue();
	}

	public userThread() {
	}

	private void doWorkload() {
		HttpResponse<String> resp = Unirest
				.get(URI.create("http://%s:%d".formatted(new Object[] { "localhost", 8081 })).toString()).asString();
	}

	@Override
	public void run() {
		super.run();
		while (!userThread.toStop) {
			long st = System.nanoTime();
			this.doWorkload();
			userThread.addRt(System.nanoTime() - st);
		}
	}
}
