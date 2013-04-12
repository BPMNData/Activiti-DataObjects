package de.hpi.uni.potsdam.test.bpmnToSql;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;
import java.sql.Statement;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class SelectSupplier implements JavaDelegate {

	public void execute(DelegateExecution execution) throws Exception {
		
		String[] suppliers = {"A", "B", "C", "D", "E", "F"};
		
		
			Double fragment = 100./suppliers.length;
			Long randValue = Math.round(Math.random()*100);
			int randomIndex = -1;
			for (int i=1; i<=suppliers.length; i++){
				if (randValue <= (i*fragment)){
					randomIndex = i-1;
					break;
				}
			}
			
			String selectedSupplier = suppliers[randomIndex];
			String caseObjID = execution.getDataObjectID();
		
		
		String query = "UPDATE `CP` SET `supplier` = \"" + selectedSupplier + "\" WHERE `cpid` = \"" + caseObjID + "\""; //MI-Varaible
		
		this.dbConnectionInsert(query);
		
		

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
