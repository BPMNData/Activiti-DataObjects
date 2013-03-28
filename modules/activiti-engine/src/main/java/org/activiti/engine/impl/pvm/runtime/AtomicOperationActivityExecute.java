/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.pvm.runtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmException;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

import de.hpi.uni.potsdam.bpmnToSql.DataObject;
import de.hpi.uni.potsdam.bpmnToSql.DataObjectClassification;


/**
 * @author Tom Baeyens
 */
public class AtomicOperationActivityExecute implements AtomicOperation {
  
  private static Logger log = Logger.getLogger(AtomicOperationActivityExecute.class.getName());

  public boolean isAsync(InterpretableExecution execution) {
    return false;
  }

  public void execute(InterpretableExecution execution) {
    ActivityImpl activity = (ActivityImpl) execution.getActivity();
    
    ActivityBehavior activityBehavior = activity.getActivityBehavior();
    if (activityBehavior==null) {
      throw new PvmException("no behavior specified in "+activity);
    }

    log.fine(execution+" executes "+activity+": "+activityBehavior.getClass().getName());
    
    // TODO: BPMN_SQL start
    
    //get id of scope or process depending whether activity is part of a scope (i.e., subprocess) or the process itself
    String dataObjectID = execution.getDataObjectID();
    
    
    System.out.println("====================" + activity.getProperty("type"));
    
    if(BpmnParse.getInputData().containsKey(activity.getId())) { //true if activity reads a data object
    	HashMap<String,ArrayList<DataObject>> dataObjectMap = new HashMap<String, ArrayList<DataObject>>();
    	//data object with same name are part of same list .. one list for each different data object read by activity
    	for (DataObject item : BpmnParse.getInputData().get(activity.getId())) {
			if(dataObjectMap.containsKey(item.getName())) {
				ArrayList<DataObject> al = dataObjectMap.get(item.getName());
				al.add(item);
				dataObjectMap.put(item.getName(), al);
			} else {
				ArrayList<DataObject> al = new ArrayList<DataObject>();
				al.add(item);
				dataObjectMap.put(item.getName(), al);
			}
		}
    	
    	
    	//create SQL queries based on identified pattern 
    	HashMap<String,Integer> queryMap = new HashMap<String, Integer>();
    	
    	for (ArrayList<DataObject> dataObjectList : dataObjectMap.values()) {
    		String q  = new String();
			int r;
			//create SQL query with respect to type of data object (main, dependent, dependent_MI, external_input) 
			if(DataObjectClassification.isMainDataObject(dataObjectList.get(0), activity.getParent().getId().split(":")[0])) {
				q = createSqlQuery(dataObjectList, dataObjectID);
				r=1;
    		} else if(DataObjectClassification.isDependentDataObjectWithUnspecifiedFK(dataObjectList.get(0), activity.getParent().getId().split(":")[0])) {
    			
    			//get primary key of case object because of assumption that all dependent DOs relate to main data object
    			String caseObjPk = new String();
    			String caseObjName = BpmnParse.getScopeInformation().get(activity.getParent().getId().split(":")[0]);
    			for (DataObject caseObj : BpmnParse.getInputData().get(execution.getActivity().getId())){
    				if (caseObj.getName().equalsIgnoreCase(caseObjName)) {
    					caseObjPk = caseObj.getPkey();
    					break;
    				}
    			}
    			
    			//provide case object of the scope to enable JOINALL; case object is in a map called getScopeInformation which has as key the scope (e.g., process, sub-process) name
    			q = createSqlQuery(dataObjectList, dataObjectID, BpmnParse.getScopeInformation().get(activity.getParent().getId().split(":")[0]), caseObjPk, "dependent_WithoutFK"); 
    			r = 1;
    		} else if(DataObjectClassification.isDependentDataObject(dataObjectList.get(0), activity.getParent().getId().split(":")[0])) {
    			//provide case object of the scope to enable JOINALL; case object is in a map called getScopeInformation which has as key the scope (e.g., process, sub-process) name
    			q = createSqlQuery(dataObjectList, dataObjectID, BpmnParse.getScopeInformation().get(activity.getParent().getId().split(":")[0]), "dependent"); 
    			r = 1;
    		} else if(DataObjectClassification.isMIDependentDataObject(dataObjectList.get(0), activity.getParent().getId().split(":")[0])) {
    			q = createSqlQuery(dataObjectList, dataObjectID, BpmnParse.getScopeInformation().get(activity.getParent().getId().split(":")[0]), "dependent_MI"); 
    			r = numberOfMultipleInstanceInTable(dataObjectList.get(0), dataObjectID, BpmnParse.getScopeInformation().get(activity.getParent().getId().split(":")[0])); //has to be defined
    		} else {
    			//non existent data object type identified
    			q = null;
    			r = 0;
    		}
    	
			queryMap.put(q,r);
    		
    		
    		
//    		for (String query : queryList) {
//    			//while(!dbConnection(query).equalsIgnoreCase(item.getState())) { //wait for correct data state
//    			while(dbConnection(query) < 1) {
//                	Thread waiter = new Thread();
//                	waiter.start();
//                	try {
//    					waiter.sleep(10000);
//    				} catch (InterruptedException e) {
//    					// TODO Auto-generated catch block
//    					e.printStackTrace();
//    				}
//                }
//			}
		}
    	
    	//check existence of all input data objects in the correct data states .. if all satisfy the check, the activity can be executed; otherwise, the check is restarted until all satisfy the check
		Boolean check = false;
		while(!check) {
			check = true;
			for (String query : queryMap.keySet()) {
				if(dbConnection(query) < queryMap.get(query)) {
					
					System.out.println("waiting for "+query);
					
					check = false;
					Thread waiter = new Thread();
                	waiter.start();
                	try {
    					waiter.sleep(10000);
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                	break; //stop if one input data object fails check
				}
			}
		}
    }
    
    //if process is an MI-sub-process, the activiti:collection variable is set with the collection of PKs of the given scope object
    // and set sub process key once the sub process is initialized with PK of case object
    if(execution.isScope() && activity.getProperty("type").equals("subProcess")) {
    	DataObject dataObj = new DataObject();
    	ArrayList<DataObject> dataObjectList = BpmnParse.getInputData().get(activity.getId());
    	for (DataObject dataObject : dataObjectList) {
			if(dataObject.getName().equalsIgnoreCase(BpmnParse.getScopeInformation().get(activity.getId()))) {
				dataObj = dataObject; // the dataObj is selected which has the same name as the caseObj
				break;
			}
		}
    	HashMap<String, Object> m = (HashMap<String, Object>) execution.getVariables();
    	//if(!execution.hasVariables()) ////Sub-Process Instance has usually no variables besides it is a started multi-sub-process instance by a scope execution
    	if(!execution.getVariableNames().contains("loopCounter")) { //loopCounter only exists in the second and later subprocess instance 
        	String query = new String();
        	//create query to select the list of PKs of the scopeObject for that process instance
        	query = "SELECT `" + dataObj.getPkey() + "` FROM `" + dataObj.getName() + "` WHERE `" + dataObj.getFkeys().get(0) + "` = \"" + dataObjectID + "\"";
        	ArrayList<String> miList = dbConnection3(query);
        	
        	if (activity.getProperties().containsKey("multiInstance")){ //check whether the current sub-process is MI-instance
        		//if MI-instance, the activiti:collection variable is set with the list of PKs
        		execution.setVariable(dataObj.getName(), miList);
        	}
        	
        	execution.setDataObjectID(miList.get(0)); //current sub-process instance gets the first primary key of the return ArrayList (in case of simple sub-process, is it only one)
    	} else {
    		execution.setDataObjectID((String)execution.getVariable(dataObj.getPkey()));
    	}
//    	if(dataObjectList != null) {
//    	}
    	
    }
    // TODO: BPMN_SQL end

    log.fine(execution+" executes "+activity+": "+activityBehavior.getClass().getName() + " after logging");
    
    try {
      activityBehavior.execute(execution);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PvmException("couldn't execute activity <"+activity.getProperty("type")+" id=\""+activity.getId()+"\" ...>: "+e.getMessage(), e);
    }
  }
  
  // TODO: BPMN_SQL added
  //main data object
  private String createSqlQuery(ArrayList<DataObject> dataObjectList, String instanceId) {
	  // TODO our stuff
	  String query;
	  String state = new String();
	  
	  for (DataObject dataObject : dataObjectList) {
		  if(state.isEmpty()) {
			  state = "\"" + dataObject.getState() + "\"";
		  } else {
			  state = state + (" OR " + "\"" + dataObject.getState() + "\"");
		  }
	  }
	  
	  query = "SELECT COUNT(`" + dataObjectList.get(0).getPkey() + "`) FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getPkey() + "` =\"" + instanceId + "\" and `state` =(" + state + ")";
	  
//	  if (type == "main") {
//		query = "SELECT COUNT(`" + dataObjectList.get(0).getPkey() + "`) FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getPkey() + "` =" + instanceId + " and `state` =(" + state + ")";
//	  } else if(type == "dependent") {
//		  //SELECT P.pid FROM `Product` P INNER JOIN `Order` O USING (oid) WHERE O.oid = 4
//		  query = "SELECT COUNT(`" + dataObjectList.get(0).getPkey() + "`) FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getFkeys().get(0) + "` =" + instanceId + " and `state` =(" + state + ")";
//	  } else {
//		  query = "SELECT `state` FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getPkey() + "` =" + instanceId;
//	  }

	  return query;
  }
  
  // TODO: BPMN_SQL added
  //dependent data object
  private String createSqlQuery(ArrayList<DataObject> dataObjectList, String instanceId, String caseObject, String type) {
	  // TODO our stuff
	  String query;
	  String state = new String();
	  
	  for (DataObject dataObject : dataObjectList) {
		  if(state.isEmpty()) {
			  state = "\"" + dataObject.getState() + "\"";
		  } else {
			  state = state + (" OR " + "\"" + dataObject.getState() + "\"");
		  }
	  }
	  
	  if (type == "dependent") {
		 // "SELECT COUNT(`" + dataObjectList.get(0).getPkey() + "`) FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getFkeys().get(0) + "` =" + instanceId + " and `state` =(" + state + ")";
		//SELECT P.pid FROM `Product` P INNER JOIN `Order` O USING (oid) WHERE O.oid = 4
		  query = "SELECT COUNT(D." + dataObjectList.get(0).getFkeys().get(0) + ") FROM `" + dataObjectList.get(0).getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObjectList.get(0).getFkeys().get(0) + ") WHERE M." + dataObjectList.get(0).getFkeys().get(0) + "= \"" + instanceId + "\" and D.state =(" + state + ")";
		  //does not work anymore as soon as several foreign keys are allowed, i.e., when we extend the data object chain to more than 2 in length
	  } else if(type == "dependent_MI") {
		  query = "SELECT COUNT(D." + dataObjectList.get(0).getFkeys().get(0) + ") FROM `" + dataObjectList.get(0).getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObjectList.get(0).getFkeys().get(0) + ") WHERE M." + dataObjectList.get(0).getFkeys().get(0) + "=\"" + instanceId + "\" and D.state =(" + state + ")";
	  } else { //wrong type
		  query = null;
	  }

	  	  
//	  if (type == "main") {
//		query = "SELECT COUNT(`" + dataObjectList.get(0).getPkey() + "`) FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getPkey() + "` =" + instanceId + " and `state` =(" + state + ")";
//	  } else if(type == "dependent") {
//		  //SELECT P.pid FROM `Product` P INNER JOIN `Order` O USING (oid) WHERE O.oid = 4
//		  query = "SELECT COUNT(`" + dataObjectList.get(0).getPkey() + "`) FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getFkeys().get(0) + "` =" + instanceId + " and `state` =(" + state + ")";
//	  } else {
//		  query = "SELECT `state` FROM `" + dataObjectList.get(0).getName() + "` WHERE `" + dataObjectList.get(0).getPkey() + "` =" + instanceId;
//	  }

	  return query;
  }
  
  // TODO: BPMN_SQL added
  //dependent data object with null foreign key
  private String createSqlQuery(ArrayList<DataObject> dataObjectList, String instanceId, String caseObject, String caseObjectPk, String type) {
	  // TODO our stuff
	  String query;
	  String state = new String();
	  
	  for (DataObject dataObject : dataObjectList) {
		  if(state.isEmpty()) {
			  state = "\"" + dataObject.getState() + "\"";
		  } else {
			  state = state + (" OR " + "\"" + dataObject.getState() + "\"");
		  }
	  }
	  
	  query = "SELECT COUNT(" + dataObjectList.get(0).getPkey() + ") FROM `" + dataObjectList.get(0).getName()+ "` WHERE `" + caseObjectPk +"` IS NULL " + " and `state`= (" + state + ")";

	  return query;
  }
  
  // TODO: BPMN_SQL added
  private int numberOfMultipleInstanceInTable(DataObject dataObj, String instanceId, String caseObject) {
	  int numberOfMI = 0;
	  String query;
	  
	  query = "SELECT COUNT(D." + dataObj.getFkeys().get(0) + ") FROM `" + dataObj.getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObj.getFkeys().get(0) + ") WHERE M." + dataObj.getFkeys().get(0) + "=\"" + instanceId + "\"";
	  numberOfMI = dbConnection(query);
	  
	  return numberOfMI;
  }
  
  // TODO: BPMN_SQL added
  public int dbConnection(String query) {
	  Connection con = null;
      Statement st = null;
      ResultSet rs = null;
      int count = 0;
      

      String url = "jdbc:mysql://localhost:3306/testdb";
      String user = "testuser";
      String password = "test623";

      try {
          con = DriverManager.getConnection(url, user, password);
          st = con.createStatement();
          rs = st.executeQuery(query);
          if (rs.next()) {
              System.out.println(rs.getString(1));
              count = Integer.parseInt(rs.getString(1));
          }

      } catch (SQLException ex) {
          log.log(Level.SEVERE, ex.getMessage(), ex);

      } finally {
          try {
              if (st != null) {
                  st.close();
              }
              if (con != null) {
                  con.close();
              }
          } catch (SQLException ex) {
        	  log.log(Level.WARNING, ex.getMessage(), ex);
          }
      }
      return count;
  }
  
  // TODO: BPMN_SQL added
  public String dbConnection2(String query) {
	  Connection con = null;
      Statement st = null;
      ResultSet rs = null;
      String result = new String();

      String url = "jdbc:mysql://localhost:3306/testdb";
      String user = "testuser";
      String password = "test623";

      try {
          con = DriverManager.getConnection(url, user, password);
          st = con.createStatement();
          rs = st.executeQuery(query);
          
          if (rs.next()) {
            result = rs.getString(1);
        }

      } catch (SQLException ex) {
          log.log(Level.SEVERE, ex.getMessage(), ex);

      } finally {
          try {
              if (st != null) {
                  st.close();
              }
              if (con != null) {
                  con.close();
              }
          } catch (SQLException ex) {
              log.log(Level.WARNING, ex.getMessage(), ex);
          }
      }
      return result;
  }
  
//TODO: BPMN_SQL added
 public ArrayList<String> dbConnection3(String query) {
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
         log.log(Level.SEVERE, ex.getMessage(), ex);

     } finally {
         try {
             if (st != null) {
                 st.close();
             }
             if (con != null) {
                 con.close();
             }
         } catch (SQLException ex) {
             log.log(Level.WARNING, ex.getMessage(), ex);
         }
     }
     return result;
 }
}
