package my.lq.xlsimp.excel.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import my.lq.xlsimp.excel.db.DatabaseContext;


public class TestDbExport {
	
	public static Connection getConnection() throws NamingException,
			SQLException {
		InitialContext ctx = (InitialContext) new DatabaseContext();
		DataSource ds = (DataSource) ctx.lookup("jdbc/ds1");
		return ds.getConnection();
	}
	
	
	private static Map<Integer, String> extractFirstRules(Map<Integer, List<String>> fullrules){
	  	Iterator<Integer> iter = fullrules.keySet().iterator();
	  	Map<Integer, String> rules = new HashMap<Integer, String>();
	  	while(iter.hasNext()){
	  		Integer key = iter.next();
	  		rules.put(key, fullrules.get(key).get(0));
	  	}
	  	return rules;
  	}
	
}		
	
