package userTest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import app.Proxy;
import kong.unirest.Unirest;
import proxyLogic.SimpleProxy;

public class mainTest {

	public static void main(String[] args) throws Exception {
		Unirest.config().concurrency(200, 200);
		//Unirest.config().automaticRetries(true);
		Unirest.config().cacheResponses(false);
		Unirest.config().connectTimeout(0);
		Unirest.config().socketTimeout(0);
		
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase sysDb = client.getDatabase("sys");
		MongoCollection<Document> mss = sysDb.getCollection("ms");
		Document msObs = mss.find(Filters.eq("name", "MSauth")).first();
		
		if(msObs == null) {
			throw new Exception("ms "+"ms1"+" not found");
		}
		
		Proxy p = new Proxy(SimpleProxy.class, 8081, Integer.MAX_VALUE,msObs);
		p.start();
		
		TimeUnit.SECONDS.sleep(3);
		userThread[] users=new userThread[100];
		for (int i = 0; i < users.length; i++) {
			users[i]=new userThread();
			users[i].start();	
		}
		
		while(true) {
			TimeUnit.SECONDS.sleep(1);
			System.out.println(Arrays.toString(userThread.avgStats()));
		}
	}

}
