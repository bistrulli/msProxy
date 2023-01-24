package userTest;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import kong.unirest.Unirest;

public class userThread extends Thread {

	private static Long nrq = 0l;
	private static Long cumRt = 0l;

	private static boolean toStop = false;
	private static Long lastNcp;

	private ExponentialDistribution dist = null;

	public static void stop(boolean stop) {
		userThread.toStop = stop;
	}

	public synchronized static void addRt(long rt) {
		userThread.cumRt += rt;
		userThread.nrq += 1;
	}

	public static Float avgRt() {
		return userThread.cumRt.floatValue() / userThread.nrq.floatValue();
	}

	public static Double avgX() {
		Long x=0l;
		if (userThread.lastNcp != null) {
			x = (userThread.nrq-userThread.lastNcp);
		}
		userThread.lastNcp = userThread.nrq;
		return x.doubleValue();
	}

	public static Double[] avgStats() {
		Double[] stats=new Double[] {userThread.avgRt()/1E9,userThread.avgX()};
		return stats;
	}

	public userThread() {
		this.dist = new ExponentialDistribution(200);
	}

	private void doWorkload() {
//		HttpResponse<String> resp = Unirest
//				.get(URI.create("http://%s:%d/?login=emilio&name=test".formatted(new Object[] { "localhost", 8081 })).toString()).asString();
		Unirest.post(URI.create("http://%s:%d/".formatted(new Object[] { "localhost", 8081 })).toString())
				.field("login", "emilio").field("name", "test").asString();
	}

	@Override
	public void run() {
		super.run();
		while (!userThread.toStop) {
			Double think = this.dist.sample();
			try {
				TimeUnit.MILLISECONDS.sleep(think.longValue());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long st = System.nanoTime();
			this.doWorkload();
			long end = System.nanoTime() - st;
			userThread.addRt(end);
		}
	}
}
