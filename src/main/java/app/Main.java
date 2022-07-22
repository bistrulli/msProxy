package app;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import kong.unirest.Unirest;
import proxyLogic.SimpleProxy;

public class Main {

	private static Integer tgtPort=null;
	private static Integer prxPort=null;

	public static void main(String[] args) {
		
		Unirest.config().concurrency(2000,2000); 
		
		Main.getCliOptions(args);

		SimpleProxy.setTgtHost("localhost");
		SimpleProxy.setTgtPort(Main.tgtPort);

		Proxy p = new Proxy(SimpleProxy.class, Main.prxPort, 4000);
		p.start();

	}

	public static void getCliOptions(String[] args) {

		int c;
		LongOpt[] longopts = new LongOpt[2];
		longopts[0] = new LongOpt("tgtPort", LongOpt.REQUIRED_ARGUMENT, null, 0);
		longopts[1] = new LongOpt("prxPort", LongOpt.REQUIRED_ARGUMENT, null, 1);

		Getopt g = new Getopt("ddctrl", args, "", longopts);
		g.setOpterr(true);
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 0:
				try {
					Main.tgtPort = Integer.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 1:
				try {
					Main.prxPort = Integer.valueOf(g.getOptarg());
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
