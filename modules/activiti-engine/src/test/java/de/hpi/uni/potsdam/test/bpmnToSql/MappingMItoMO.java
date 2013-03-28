package de.hpi.uni.potsdam.test.bpmnToSql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class MappingMItoMO implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		
		ArrayList<String> moIds = new ArrayList<String>();
		ArrayList<String> miIds = new ArrayList<String>();
		HashMap<String, String> matchingMap = new HashMap<String,String>();
		
		moIds = dbConnectionSelect("SELECT `moid` FROM `material order` WHERE `state` = \"created\"");
		miIds = dbConnectionSelect("SELECT `miid` FROM `material item` WHERE `state` = \"partitioned\"");
		
		for (String materialItem : miIds) {
			Double fragment = 100./moIds.size();
			Long randValue = Math.round(Math.random()*100);
			int randomIndex = -1;
			for (int i=1; i<=moIds.size(); i++){
				if (randValue <= (i*fragment)){
					randomIndex = i-1;
					break;
				}
			}
			
			matchingMap.put(materialItem, moIds.get(randomIndex));
		}
		
		String query = "INSERT INTO `productdb`(`miid`, `moid`) VALUES";
		
		for (String matchingItem : matchingMap.keySet()) {
			query = query + "(\""+matchingItem+"\" , \""+matchingMap.get(matchingItem) +"\"),";
		}
		
		query = query.substring(0, (query.length()-1)); //the last comma is deleted 
		
		this.dbConnectionInsert(query);
		
		

	}
	
	public ArrayList<String> dbConnectionSelect(String query) {
		  Connection con = null;
	      Statement st = null;
	      ResultSet rs = null;
	      ArrayList<String> result = new ArrayList<String>();

	      String url = "jdbc:mysql://localhost:3306/testdb";
	      String user = "testuser";
	      String password = "test623";

	      try {
	          con = DriverManager.getConnection(url, user, password);
	          st = con.createStatement();
	          rs = st.executeQuery(query);
	          
	          while (rs.next()) {
	            result.add(rs.getString(1));
	        }

	      } catch (SQLException ex) {
	          System.out.println(ex.getMessage());

	      } finally {
	          try {
	              if (st != null) {
	                  st.close();
	              }
	              if (con != null) {
	                  con.close();
	              }
	          } catch (SQLException ex) {
	        	  System.out.println(ex.getMessage());
	          }
	      }
	      return result;
	  }
	
	public void dbConnectionInsert(String query) {
		  Connection con = null;
	      Statement st = null;

	      String url = "jdbc:mysql://localhost:3306/testdb";
	      String user = "testuser";
	      String password = "test623";

	      try {
	          con = DriverManager.getConnection(url, user, password);
	          st = con.createStatement();
	          System.out.println(query);
	          st.executeUpdate(query);

	      } catch (SQLException ex) {
	    	  System.out.println(ex.getMessage());

	      } finally {
	          try {
	              if (st != null) {
	                  st.close();
	              }
	              if (con != null) {
	                  con.close();
	              }
	          } catch (SQLException ex) {
	        	  System.out.println(ex.getMessage());
	          }
	      }
	  }

}
