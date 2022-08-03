package app;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import kong.unirest.Unirest;
import proxyLogic.SimpleProxy;

public class Main {

	private static Integer prxPort = null;
	private static String msName = null;

	public static void main(String[] args) throws Exception {

		Unirest.config().concurrency(2000, 2000);

		Main.getCliOptions(args);
		
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase sysDb = client.getDatabase("sys");
		MongoCollection<Document> mss = sysDb.getCollection("ms");
		Document msObs = mss.find(Filters.eq("name", Main.msName)).first();
		
		MongoDatabase msdb = client.getDatabase(Main.msName);
		msdb.drop();
		
		if(msObs == null) {
			throw new Exception("ms "+Main.msName+" not found");
		}
		
		client.close();
		
		Proxy p = new Proxy(SimpleProxy.class, Main.prxPort, Integer.MAX_VALUE,msObs);
		p.start();

	}

	public static void getCliOptions(String[] args) {

		int c;
		LongOpt[] longopts = new LongOpt[2];
		longopts[0] = new LongOpt("prxPort", LongOpt.REQUIRED_ARGUMENT, null, 0);
		longopts[1] = new LongOpt("msName", LongOpt.REQUIRED_ARGUMENT, null, 1);

		Getopt g = new Getopt("ddctrl", args, "", longopts);
		g.setOpterr(true);
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 0:
				try {
					Main.prxPort = Integer.valueOf(g.getOptarg());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 1:
				try {
					Main.msName = String.valueOf(g.getOptarg());
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
