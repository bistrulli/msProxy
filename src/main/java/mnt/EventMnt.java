package mnt;

import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import app.Proxy;

public class EventMnt extends Thread {

	private Proxy prx = null;
	private MongoClient mongo = null;
	private String msname = null;
	private MongoDatabase msDB = null;

	public EventMnt(Proxy prx, String msname) {
		this.prx = prx;
		this.mongo = MongoClients.create("mongodb://localhost:27017");
		this.msname = msname;
		this.msDB = this.mongo.getDatabase(this.msname);
	}

	@Override
	public void run() {
		super.run();

		MongoCollection<Document> msRT = this.msDB.getCollection("rt");
		Event event = null;
		while (true) {
			ArrayList<Document> evts = new ArrayList<Document>();
			while ((event = this.prx.getEvents().poll()) != null) {
				Document evtDoc = new Document();
				evtDoc.append("st", event.getStart()).append("end", event.getEnd());
				evts.add(evtDoc);
			}
			if(evts.size()>0)
				msRT.insertMany(evts);
		}
	}
}
