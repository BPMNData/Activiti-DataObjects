package de.hpi.uni.potsdam.test.bpmnToSql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class QueryProductDB implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		
		ArrayList<String> dbResults = new ArrayList<String>();
		System.out.println("moid = "+execution.getVariableLocal("moid"));
		dbResults = dbConnectionSelect("SELECT `miid` FROM `ProductDB` WHERE `moid` = \"" + execution.getVariableLocal("moid") + "\"");
		
		System.out.println("RESULT: "+dbResults);
		
		String matchingMIs = "(`miid` = \"";
		
		for (String result : dbResults) {
			matchingMIs = matchingMIs + result +"\" OR `miid` = \"";
		}
		
		System.out.println(matchingMIs);
		
		matchingMIs = matchingMIs.substring(0, matchingMIs.lastIndexOf("OR")-1) + ")";
		
		execution.setVariable("matchingMIs", matchingMIs);

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

}
