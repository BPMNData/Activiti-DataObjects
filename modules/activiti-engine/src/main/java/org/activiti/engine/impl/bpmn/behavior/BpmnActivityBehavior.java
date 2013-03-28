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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

import de.hpi.uni.potsdam.bpmnToSql.DataObject;
import de.hpi.uni.potsdam.bpmnToSql.DataObjectClassification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Helper class for implementing BPMN 2.0 activities, offering convenience
 * methods specific to BPMN 2.0.
 * 
 * This class can be used by inheritance or aggregation.
 * 
 * @author Joram Barrez
 */
public class BpmnActivityBehavior {

  private static Logger log = Logger.getLogger(BpmnActivityBehavior.class.getName());

  /**
   * Performs the default outgoing BPMN 2.0 behavior, which is having parallel
   * paths of executions for the outgoing sequence flow.
   * 
   * More precisely: every sequence flow that has a condition which evaluates to
   * true (or which doesn't have a condition), is selected for continuation of
   * the process instance. If multiple sequencer flow are selected, multiple,
   * parallel paths of executions are created.
   */
  public void performDefaultOutgoingBehavior(ActivityExecution activityExceution) {
    performOutgoingBehavior(activityExceution, true, false, null);
  }

  /**
   * Performs the default outgoing BPMN 2.0 behavior (@see
   * {@link #performDefaultOutgoingBehavior(ActivityExecution)}), but without
   * checking the conditions on the outgoing sequence flow.
   * 
   * This means that every outgoing sequence flow is selected for continuing the
   * process instance, regardless of having a condition or not. In case of
   * multiple outgoing sequence flow, multiple parallel paths of executions will
   * be created.
   */
  public void performIgnoreConditionsOutgoingBehavior(ActivityExecution activityExecution) {
    performOutgoingBehavior(activityExecution, false, false, null);
  }

  /**
   * Actual implementation of leaving an activity.
   * 
   * @param execution
   *          The current execution context
   * @param checkConditions
   *          Whether or not to check conditions before determining whether or
   *          not to take a transition.
   * @param throwExceptionIfExecutionStuck
   *          If true, an {@link ActivitiException} will be thrown in case no
   *          transition could be found to leave the activity.
   */
  protected void performOutgoingBehavior(ActivityExecution execution, 
          boolean checkConditions, boolean throwExceptionIfExecutionStuck, List<ActivityExecution> reusableExecutions) {

    if (log.isLoggable(Level.FINE)) {
      log.fine("Leaving activity '" + execution.getActivity().getId() + "'");
    }
    
    // TODO: BPMN_SQL start
    
    //get id of scope or process depending whether activity is part of a scope (i.e., subprocess) or the process itself    
//    if(execution.getParentId()==null) {
//    	ScopeInstanceID = execution.getProcessInstanceId();
//    }else if (execution.isScope()){
//    	ScopeInstanceID = execution.getId();
//    } else{
//    	ScopeInstanceID = execution.getParentId();
//    }
//    
    String dataObjectID = execution.getDataObjectID();
    
    
    if(BpmnParse.getOutputData().containsKey(execution.getActivity().getId())) { //true if activity writes a data object
    	//ArrayList<DataObject> test = BpmnParse.getOutputData().get(execution.getActivity().getId());
    	for (DataObject item : BpmnParse.getOutputData().get(execution.getActivity().getId())){
    		String query = new String();
    		
    		ArrayList<String> stateList = new ArrayList<String>();
    		//get state of the input object when it exists, that it can be used for the UPDATE-statements
//    		String a = execution.getActivity().getId(); 
//    		Map<String, ArrayList<DataObject>> b = BpmnParse.getInputData();
//    		Set<String> c = b.keySet();
//    		if(BpmnParse.getInputData().keySet().contains(execution.getActivity().getId())) {
//    			String d = "abc";
//    		}
    		
    		for (DataObject dataObj : getMatchingInputDataObject(item, execution.getActivity().getId())) {
				stateList.add(dataObj.getState());
			}
			
			//check whether the state of the object is a process variable
			if (item.getState().startsWith("$")){
				item.setState((String)execution.getVariable(item.getState().substring(1)));
			}
    		
    		//create SQL query with respect to type of data object (main, dependent, dependent_MI, dependent_WithoutFK) 
    		if(DataObjectClassification.isMainDataObject(item, execution.getActivity().getParent().getId().split(":")[0])) {
    			if(!getMatchingInputDataObject(item,execution.getActivity().getId()).isEmpty()) {
//    			if(!stateList.isEmpty()) {
    				//input data object exists
    				query = createSqlQuery(item, stateList, dataObjectID);
    			} else {
    				//no input data object
    				query = createSqlQuery(item, dataObjectID);
    			}
    		} else {
//    			DataObject matchingInputDataObj = new DataObject();
//    			matchingInputDataObj = null;
//    			for (DataObject inputDo : BpmnParse.getInputData().get(execution.getActivity().getId())){
//    				if (inputDo.getName().equalsIgnoreCase(item.getName())){
//    					matchingInputDataObj = inputDo;
//    				}
//    			}
    			String expression = null;
    			if(!getMatchingInputDataObject(item,execution.getActivity().getId()).isEmpty()) {
//    			if(!stateList.isEmpty()) {
    				// TODO: make available for OR statement
    				if(DataObjectClassification.isDependentDataObjectWithUnspecifiedFK(getMatchingInputDataObject(item,execution.getActivity().getId()).get(0), execution.getActivity().getParent().getId().split(":")[0])){
    					if(item.getProcessVariable() != null) {
    	    				expression = (String) execution.getVariable(item.getProcessVariable());
    	    			}
    					//Update-Query for data objects where the fk was not yet specified in the data base, e.g. when the object was received by another organization
    					query = createSqlQuery(item, dataObjectID, BpmnParse.getScopeInformation().get(execution.getActivity().getParent().getId().split(":")[0]), stateList, expression, "dependent_WithoutFK");
    				}
    			}
    			if(query.isEmpty()) {
    				if(DataObjectClassification.isDependentDataObject(item, execution.getActivity().getParent().getId().split(":")[0])) {
    	    			//provide case object of the scope to enable JOINALL; case object is in a map called getScopeInformation which has as key the scope (e.g., process, sub-process) name
    					if(!getMatchingInputDataObject(item,execution.getActivity().getId()).isEmpty()) {
//    					if(!stateList.isEmpty()) {
    	    				//input data object exists
    						query = createSqlQuery(item, dataObjectID, BpmnParse.getScopeInformation().get(execution.getActivity().getParent().getId().split(":")[0]), stateList, expression, "dependent");
    	    			} else {
    	    				//no input data object
    	    				query = createSqlQuery(item, dataObjectID, BpmnParse.getScopeInformation().get(execution.getActivity().getParent().getId().split(":")[0]), "dependent");
    	    			}
	    			} else if(DataObjectClassification.isMIDependentDataObject(item, execution.getActivity().getParent().getId().split(":")[0])) {
		    			//provide case object of the scope to enable JOINALL; case object is in a map called getScopeInformation which has as key the scope (e.g., process, sub-process) name
		    			int numberOfItems = -1;
		    			if(item.getPkType().equals("new")) {
		    				numberOfItems = Integer.parseInt((String)execution.getVariable(item.getProcessVariable()));
		    			}
		    			
		    			if(!getMatchingInputDataObject(item,execution.getActivity().getId()).isEmpty()) {
//		    			if(!stateList.isEmpty()) {
		    				//input data object exists
		    				query = createSqlQuery(item, dataObjectID, BpmnParse.getScopeInformation().get(execution.getActivity().getParent().getId().split(":")[0]), stateList, "dependent_MI", numberOfItems);
		    			} else {
		    				//no input data object
		    				query = createSqlQuery(item, dataObjectID, BpmnParse.getScopeInformation().get(execution.getActivity().getParent().getId().split(":")[0]), "dependent_MI", numberOfItems);
		    			}
	    			} else {
	    				System.out.println("Output data object does not match the requirements for firing SQL-UPDATE Statement");
	    			}
    			}
    		}
    		dbConnection(query);
       }
    }
    // TODO: BPMN_SQL end
    
    log.fine("Leaving activity '" + execution.getActivity().getId() + " completely");

    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
    List<PvmTransition> transitionsToTake = new ArrayList<PvmTransition>();

    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    for (PvmTransition outgoingTransition : outgoingTransitions) {
      if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
        Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        if (condition == null || !checkConditions || condition.evaluate(execution)) {
          transitionsToTake.add(outgoingTransition);
        }
      }
    }

    if (transitionsToTake.size() == 1) {
      
      execution.take(transitionsToTake.get(0));

    } else if (transitionsToTake.size() >= 1) {

      execution.inactivate();
      if (reusableExecutions == null || reusableExecutions.isEmpty()) {
        execution.takeAll(transitionsToTake, Arrays.asList(execution));
      } else {
        execution.takeAll(transitionsToTake, reusableExecutions);
      }

    } else {

      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          execution.take(defaultTransition);
        } else {
          throw new ActivitiException("Default sequence flow '" + defaultSequenceFlow + "' could not be not found");
        }
      } else {
        
        Object isForCompensation = execution.getActivity().getProperty(BpmnParse.PROPERTYNAME_IS_FOR_COMPENSATION);
        if(isForCompensation != null && (Boolean) isForCompensation) {
          
          InterpretableExecution parentExecution = (InterpretableExecution) execution.getParent();
          ((InterpretableExecution)execution).remove();
          parentExecution.signal("compensationDone", null);            
          
        } else {
          
          if (log.isLoggable(Level.FINE)) {
            log.fine("No outgoing sequence flow found for " + execution.getActivity().getId() + ". Ending execution.");
          }
          execution.end();
          
          if (throwExceptionIfExecutionStuck) {
            throw new ActivitiException("No outgoing sequence flow of the inclusive gateway '" + execution.getActivity().getId()
                  + "' could be selected for continuing the process");
          }
        }
        
      }
    }
  }
  
  // TODO: BPMN_SQL added
  private ArrayList<DataObject> getMatchingInputDataObject(DataObject item, String id) {
	  ArrayList<DataObject> dataObjects = new ArrayList<DataObject>();
	  if(BpmnParse.getInputData().get(id) != null) {
		  for (DataObject inputDo : BpmnParse.getInputData().get(id)){
			  if (inputDo.getName().equalsIgnoreCase(item.getName())){
				  dataObjects.add(inputDo);
			  }
		  }  
	  }
	  
	  return dataObjects;
  }

  // TODO: BPMN_SQL added
  //Creation of SQL-Queries(insert, update, delete) only for main data object
  private String createSqlQuery(DataObject dataObj, String scopeInstanceId) {
	  String query ="";
	  
	 if (dataObj.getPkType().equals("new")){
    	  query = "INSERT INTO `"+dataObj.getName()+"`(`"+dataObj.getPkey()+"`, `state`) VALUES ("+scopeInstanceId+",\""+dataObj.getState()+"\")";
      } else if(dataObj.getPkType().equals("delete")) {
    	  query = "DELETE FROM `"+dataObj.getName()+"` WHERE "+dataObj.getPkey()+"=\""+scopeInstanceId + "\"";
      }
      else {
    	  query = "UPDATE `"+dataObj.getName() + "` SET `state`=\""+dataObj.getState() + "\" WHERE `"+dataObj.getPkey() + "`=\"" + scopeInstanceId + "\"";
      }
	  
	  return query;
  }
  
//TODO: BPMN_SQL added
 //Creation of SQL-Queries(insert, update, delete) only for main data object
 private String createSqlQuery(DataObject dataObj, ArrayList<String> stateList, String scopeInstanceId) {
	  String query ="";
	  String state = new String();
	  
	  for (String s : stateList) {
		  if(state.isEmpty()) {
			  state = "\"" + s + "\"";
		  } else {
			  state = state + (" OR " + "\"" + s + "\"");
		  }
	  }
	  
	 if (dataObj.getPkType().equals("new")){
   	  query = "INSERT INTO `"+dataObj.getName()+"`(`"+dataObj.getPkey()+"`, `state`) VALUES ("+scopeInstanceId+",\""+dataObj.getState()+"\")";
     } else if(dataObj.getPkType().equals("delete")) {
   	  query = "DELETE FROM `"+dataObj.getName()+"` WHERE "+dataObj.getPkey()+"=\""+scopeInstanceId + "\"";
     }
     else {
   	  query = "UPDATE IGNORE `"+dataObj.getName() + "` SET `state`=\"" + dataObj.getState() + "\" WHERE `" + dataObj.getPkey() + "`=\"" + scopeInstanceId + "\" and `state` = (" + state + ")";
     }
	  
	  return query;
 }
  
  // TODO: BPMN_SQL added
  private String createSqlQuery(DataObject dataObj, String scopeInstanceId, String caseObject, String type) {
	  String query ="";
	  UUID uuid = UUID.randomUUID(); //primary key for dependent data objects
	  	  
	  if(type == "dependent") {
		  if (dataObj.getPkType().equals("new")){
        	  query = "INSERT INTO `"+dataObj.getName()+"`(`" + dataObj.getPkey() + "`, `" +dataObj.getFkeys().get(0)+"`, `state`) VALUES (\""+ uuid + "\"," 
        			  +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+"\"),\""+dataObj.getState()+"\")";
        	  
          } else if(dataObj.getPkType().equals("delete")) {
        	  //join in from statement not allowed
        	  String q = "SELECT D."+dataObj.getPkey()+" FROM `" + dataObj.getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObj.getFkeys().get(0) + ")";
        	  String pkey = dbConnection2(q);
        	  query = "DELETE FROM `"+dataObj.getName()+"` WHERE `"+dataObj.getPkey()+"`= \""+ pkey + "\" AND `state` = \"" + dataObj.getState() + "\""; //has to be checked
          }
          else {
        	  //join in from statement not allowed
        	  String q = "SELECT D."+dataObj.getPkey()+" FROM `" + dataObj.getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObj.getFkeys().get(0) + ") WHERE M." + dataObj.getFkeys().get(0) + "=\"" + scopeInstanceId + "\"";
        	  String pkey = dbConnection2(q);
        	  query = "UPDATE `"+dataObj.getName()+"` SET `state`=\""+dataObj.getState()+"\" WHERE `"+dataObj.getPkey()+"`= \"" + pkey + "\"";
          }
	  } else {
		  System.out.println("wrong type");
	  }
	  return query;
  }
  
//TODO: BPMN_SQL added
 private String createSqlQuery(DataObject dataObj, String scopeInstanceId, String caseObject, ArrayList<String> stateList, String expression, String type) {
	  String query ="";
	  UUID uuid = UUID.randomUUID(); //primary key for dependent data objects
	  String state = new String();
	  
	  for (String s : stateList) {
		  if(state.isEmpty()) {
			  state = "\"" + s + "\"";
		  } else {
			  state = state + (" OR " + "\"" + s + "\"");
		  }
	  }
	  	  
	  if(type == "dependent") {
		  if (dataObj.getPkType().equals("new")){
       	  query = "INSERT INTO `"+dataObj.getName()+"`(`" + dataObj.getPkey() + "`, `" +dataObj.getFkeys().get(0)+"`, `state`) VALUES (\""+ uuid + "\"," 
       			  +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+"\"),\""+dataObj.getState()+"\")";
       	  
         } else if(dataObj.getPkType().equals("delete")) {
       	  //join in from statement not allowed
       	  String q = "SELECT D."+dataObj.getPkey()+" FROM `" + dataObj.getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObj.getFkeys().get(0) + ")";
       	  String pkey = dbConnection2(q);
       	  query = "DELETE FROM `"+dataObj.getName()+"` WHERE `"+dataObj.getPkey()+"`= \""+ pkey + "\" AND `state` = \"" + dataObj.getState() + "\""; //has to be checked
         }
         else {
       	  //join in from statement not allowed
       	  String q = "SELECT D."+dataObj.getPkey()+" FROM `" + dataObj.getName() + "` D INNER JOIN `" + caseObject + "` M USING (" + dataObj.getFkeys().get(0) + ") WHERE M." + dataObj.getFkeys().get(0) + "=\"" + scopeInstanceId + "\"";
       	  String pkey = dbConnection2(q);
       	  query = "UPDATE IGNORE `"+dataObj.getName()+"` SET `state`=\""+dataObj.getState()+"\" WHERE `"+dataObj.getPkey()+"`= \"" + pkey + "\" and `state` = (" + state + ")";
         }
	  } else if(type == "dependent_WithoutFK"){
			//TODO: has to be checked
		  if(expression != null){
			  query = "UPDATE IGNORE `" + dataObj.getName() + "` SET `" + dataObj.getFkeys().get(0) + "` =" +"(SELECT `" + dataObj.getFkeys().get(0) + "` FROM `" + caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+ "\"), `state`=\""+dataObj.getState()+"\" WHERE `"
			  			+ dataObj.getFkeys().get(0) + "` IS NULL and `state` = (" + state + ") AND " + expression; 
			  } else{
				  query = "UPDATE IGNORE `" + dataObj.getName() + "` SET `" + dataObj.getFkeys().get(0) + "` =" +"(SELECT `" + dataObj.getFkeys().get(0) + "` FROM `" + caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+ "\"), `state`=\""+dataObj.getState()+"\" WHERE `"
				  			+ dataObj.getFkeys().get(0) + "` IS NULL and `state` = (" + state + ")"; 
			  }		  
	  	  
	  	  //SELECT COUNT( `rid` ) FROM `Receipt` WHERE `oid` IS NULL AND state = "approved"
	  } else {
		  System.out.println("wrong type");
	  }
	  return query;
 }
  
  // TODO: BPMN_SQL added
  private String createSqlQuery(DataObject dataObj, String scopeInstanceId, String caseObject, String type, int count) {
	  String query ="";
	  UUID uuid = UUID.randomUUID(); //primary key for dependent data objects
	  	  
	 if(type == "dependent_MI") {
		  if (dataObj.getPkType().equals("new")){
        	  query = "INSERT INTO `"+dataObj.getName()+"`(`" + dataObj.getPkey() + "`, `" +dataObj.getFkeys().get(0)+"`, `state`) VALUES "; 
        	  for(int i=1; i<count; i++){
        		query = query + "(\""+ uuid + "\"," +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+"\"),\""+dataObj.getState()+"\"),"; 
        		uuid = UUID.randomUUID(); //set new UUID for next collection data item
        	  }
        	  query = query + "(\""+ uuid + "\"," +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= "+scopeInstanceId+"),\""+dataObj.getState()+"\")";	
        	  
          } else if(dataObj.getPkType().equals("delete")) {
        	  query = "DELETE FROM `"+dataObj.getName()+"` WHERE `" + dataObj.getFkeys().get(0) + "` =" +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= "+scopeInstanceId+ 
        			  ") AND `state` = \"" + dataObj.getState()+"\""; 
          }
          else {
        	  query = "UPDATE `"+dataObj.getName()+"` SET `state`=\""+dataObj.getState()+"\" WHERE `" + dataObj.getFkeys().get(0) + "` =" +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+ "\")";
          }
	  }  else {
		  System.out.println("wrong type");
	  }
	  return query;
  }
  
 //TODO: BPMN_SQL added
 private String createSqlQuery(DataObject dataObj, String scopeInstanceId, String caseObject, ArrayList<String> stateList, String type, int count) {
	  String query ="";
	  UUID uuid = UUID.randomUUID(); //primary key for dependent data objects
	  String state = new String();
	  
	  for (String s : stateList) {
		  if(state.isEmpty()) {
			  state = "\"" + s + "\"";
		  } else {
			  state = state + (" OR " + "\"" + s + "\"");
		  }
	  }
	  	  
	 if(type == "dependent_MI") {
		  if (dataObj.getPkType().equals("new")){
       	  query = "INSERT INTO `"+dataObj.getName()+"`(`" + dataObj.getPkey() + "`, `" +dataObj.getFkeys().get(0)+"`, `state`) VALUES "; 
       	  for(int i=1; i<count; i++){
       		query = query + "(\""+ uuid + "\"," +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+"\"),\""+dataObj.getState()+"\"),"; 
       		uuid = UUID.randomUUID(); //set new UUID for next collection data item
       	  }
       	  query = query + "(\""+ uuid + "\"," +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= "+scopeInstanceId+"),\""+dataObj.getState()+"\")";	
       	  
         } else if(dataObj.getPkType().equals("delete")) {
       	  query = "DELETE FROM `"+dataObj.getName()+"` WHERE `" + dataObj.getFkeys().get(0) + "` =" +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= "+scopeInstanceId+ 
       			  ") AND `state` = \"" + dataObj.getState()+"\""; 
         }
         else {
       	  query = "UPDATE IGNORE `"+dataObj.getName()+"` SET `state`=\""+dataObj.getState()+"\" WHERE `" + dataObj.getFkeys().get(0) + "` =" +"(SELECT `"+dataObj.getFkeys().get(0)+"` FROM `"+ caseObject + "` WHERE `"+dataObj.getFkeys().get(0)+"`= \""+scopeInstanceId+ "\") AND `state` = (" + state + ")";
         }
	  }  else {
		  System.out.println("wrong type");
	  }
	  return query;
 }
  
  // TODO: BPMN_SQL added
  public void dbConnection(String query) {
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
}
