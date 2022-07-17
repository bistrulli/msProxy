package app;

import java.util.concurrent.TimeUnit;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import proxyLogic.SimpleProxy;
import userTest.userThread;

public class Main {

	public static void main(String[] args) {
		// TODO aggingere CLI parameters
		// TODO randere il proxy un task in background
		
		SimpleProxy.setTgtHost("localhost");
		SimpleProxy.setTgtPort(4000);
		
		Proxy p = new Proxy(SimpleProxy.class, 3000, 4000);
		p.start();

	}
	
	public static void getCliOptions(String[] args) {

		int c;
		LongOpt[] longopts = new LongOpt[3];
		longopts[0] = new LongOpt("tgtPort", LongOpt.REQUIRED_ARGUMENT, null, 0);
		longopts[1] = new LongOpt("tgtHost", LongOpt.REQUIRED_ARGUMENT, null, 1);
		longopts[2] = new LongOpt("prxPort", LongOpt.REQUIRED_ARGUMENT, null, 2);

		Getopt g = new Getopt("ddctrl", args, "", longopts);
		g.setOpterr(true);
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 0:
				try {
					Main.isEmu = Integer.valueOf(g.getOptarg()) > 0 ? true : false;
				} catch (NumberFormatException e) {
					System.err.println(String.format("%s is not valid, it must be 0 or 1.", g.getOptarg()));
				}
				break;
			case 1:
				try {
					if (!Main.validate(g.getOptarg())) {
						throw new Exception(String.format("%s is not a valid jedis HOST", g.getOptarg()));
					}
					Main.jedisHost = String.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					Main.tier2Host = String.valueOf(g.getOptarg()); 
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 3:
				try {
					Main.cgv2 = Integer.valueOf(g.getOptarg()) > 0 ? true : false; 
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

}
