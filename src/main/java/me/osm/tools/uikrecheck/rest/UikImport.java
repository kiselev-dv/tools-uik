package me.osm.tools.uikrecheck.rest;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.osm.tools.uikrecheck.bobjects.Revision;
import me.osm.tools.uikrecheck.bobjects.Uik;
import me.osm.tools.uikrecheck.dao.UIKDao;
import me.osm.tools.uikrecheck.hutil.SessionFactorySinglton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

public class UikImport {
	
	public void read(Request request, Response response) {
		String fails = request.getHeader("fails");
		String tasks = request.getHeader("tasks");

		String update = request.getHeader("update");
		
		if(update == null) {
			importUIKS(fails, tasks);
		}
		else {
			updateUIKS(update);
		}
		
		response.setContentType("text");
		response.setBody("Done");
	}

	private void updateUIKS(String update) {
		Transaction trx = SessionFactorySinglton.getFactory().getCurrentSession().beginTransaction();
		
		try {
			Set<String> failStrings = new HashSet<String>();
			List<String> lines = (List<String>)IOUtils.readLines(new FileInputStream(new File(update)));
			for(String line : lines) {
				if(StringUtils.contains(line, "Case") && StringUtils.contains(line, "FAILED")) {
					failStrings.add(StringUtils.substringBetween(line, "Case ", " FAILED"));
				}
			}
			
			Query delQ = SessionFactorySinglton.getFactory().getCurrentSession().createQuery(
					"delete from " + Revision.class.getName() + " rev where rev.subject is null");
			
			delQ.executeUpdate();
			
			for(Uik uik : UIKDao.listUnreviwed()) {
				if(!failStrings.contains(uik.getName())) {
					Revision revision = new Revision();
					revision.setAction("Updated");
					revision.setUser("system");
					
					revision.setRevisionDate(new Date());
					
					revision.setSubject(uik);
					
					SessionFactorySinglton.getFactory().getCurrentSession().save(revision);
				} 
			}
			
			trx.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(!trx.wasCommitted()) {
				trx.rollback();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void importUIKS(String fails, String tasks) {
		try {
			
			Set<String> failStrings = new HashSet<String>();
			List<String> lines = (List<String>)IOUtils.readLines(new FileInputStream(new File(fails)));
			for(String line : lines) {
				if(StringUtils.contains(line, "Case") && StringUtils.contains(line, "FAILED")) {
					failStrings.add(StringUtils.substringBetween(line, "Case ", " FAILED"));
				}
			}
			
			Transaction trx = SessionFactorySinglton.getFactory().getCurrentSession().beginTransaction();
			
			try {
				JSONArray tasksJSON = new JSONObject(IOUtils.toString(new FileInputStream(new File(tasks)))).getJSONArray("cases");
				for(int i=0; i<tasksJSON.length(); i++) {
					JSONObject task = tasksJSON.getJSONObject(i);
					if(failStrings.contains(task.getString("name"))) {
						Uik uik = new Uik();
						
						uik.setName(task.getString("name"));
						String location = task.getJSONObject("first_result").getString("location");
						String[] loc = StringUtils.split(location, '/');
						uik.setLat(Double.valueOf(loc[1]));
						uik.setLon(Double.valueOf(loc[2]));
						
						UIKDao.save(uik);
						
						//import failed case only once
						failStrings.remove(task.getString("name"));
					}
				}
				trx.commit();
			}
			finally {
				if(!trx.wasCommitted()) {
					trx.rollback();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
