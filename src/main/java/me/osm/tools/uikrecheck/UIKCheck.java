package me.osm.tools.uikrecheck;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import me.osm.tools.uikrecheck.bobjects.Uik;
import me.osm.tools.uikrecheck.dao.UIKDao;
import me.osm.tools.uikrecheck.hutil.HyperSqlDbServer;
import me.osm.tools.uikrecheck.hutil.SessionFactorySinglton;
import me.osm.tools.uikrecheck.rest.UikImport;
import me.osm.tools.uikrecheck.rest.UikRest;
import me.osm.tools.uikrecheck.serilization.SerializationProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Transaction;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Flags;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.RestExpress;
import org.restexpress.pipeline.Postprocessor;

public class UIKCheck {

	private static HyperSqlDbServer hsql;
	private static RestExpress rest;

	public static void main(String[] args) {
		
		Properties properties = new Properties();
		
		properties.setProperty("server.database.0", "data/uikcheck");
		properties.setProperty("server.dbname.0", "uikcheck");
		properties.setProperty("server.remote_open", "false");
		properties.setProperty("hsqldb.reconfig_logging", "false");
		
		hsql = new HyperSqlDbServer(properties);
		hsql.start();

		RestExpress.setSerializationProvider(new SerializationProvider());
		
		rest = new RestExpress()
				.setName("uik")
				.addPostprocessor(new Postprocessor() {
					
					public void process(Request request, Response response) {
						response.addHeader("Access-Control-Allow-Origin", "*");
					}
				});

		rest.uri("/uik/unreviewed/random.{format}",
				new UikRest())
				.method(HttpMethod.GET)
				.defaultFormat("json")
				.flag(Flags.Cache.DONT_CACHE);

		rest.uri("/uik/{id}/review",
				new UikRest())
				.method(HttpMethod.POST)
				.flag(Flags.Cache.DONT_CACHE);

		rest.uri("/uik/_import",
				new UikImport())
				.method(HttpMethod.GET)
				.flag(Flags.Cache.DONT_CACHE);

		rest.bind(8081);

		Runtime runtime = Runtime.getRuntime();
		Thread thread = new Thread(new Runnable() {
			
			public void run() {
				UIKCheck.hsql.stop();
				UIKCheck.rest.shutdown();
			}
		});
        runtime.addShutdownHook(thread);
        
        Transaction trx = SessionFactorySinglton.getFactory().getCurrentSession().beginTransaction();
        System.out.println(UIKDao.countUnreviwed());
        
        trx.commit();
	}

}
