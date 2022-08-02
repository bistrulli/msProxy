package userTest;

import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.internal.connection.Time;

import app.Proxy;
import kong.unirest.Unirest;
import proxyLogic.SimpleProxy;

public class mainTest {

	public static void main(String[] args) throws Exception {
		Unirest.config().concurrency(2000, 2000);
		
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase sysDb = client.getDatabase("sys");
		MongoCollection<Document> mss = sysDb.getCollection("ms");
		Document msObs = mss.find(Filters.eq("name", "ms1")).first();
		
		if(msObs == null) {
			throw new Exception("ms "+"ms1"+" not found");
		}
		
		Proxy p = new Proxy(SimpleProxy.class, 8081, Integer.MAX_VALUE);
		p.setMs(msObs);
		p.start();
		
		TimeUnit.SECONDS.sleep(3);
		userThread[] users=new userThread[200];
		for (int i = 0; i < users.length; i++) {
			users[i]=new userThread();
			users[i].start();
		}
		
		while(true) {
			TimeUnit.SECONDS.sleep(1);
			System.out.println(userThread.avgRt()/10E9);
		}
	}

}
