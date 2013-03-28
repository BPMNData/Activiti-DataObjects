package de.hpi.uni.potsdam.bpmnToSql;

import java.util.ArrayList;

public class DataObject {

	private String name;
	private String state;
	private String pkey;
	private String pkType;
	private ArrayList<String> fkeys;
	private Boolean isCollection = false;
	
	//attributed which can be set as arc expression
	private String processVariable;
	private String regExpression;
	
	public ArrayList<String> getFkeys() {
		return fkeys;
	}
	public void setFkeys(ArrayList<String> fkeys) {
		this.fkeys = fkeys;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getPkey() {
		return pkey;
	}
	public void setPkey(String pkey) {
		this.pkey = pkey;
	}
	public String getPkType() {
		return pkType;
	}
	public void setPkType(String pkType) {
		this.pkType = pkType;
	}
	public Boolean getIsCollection() {
		return isCollection;
	}
	public void setIsCollection(Boolean isCollection) {
		this.isCollection = isCollection;
	}

	public String getProcessVariable() {
		return processVariable;
	}
	public void setProcessVariable(String processVariable) {
		this.processVariable = processVariable;
	}
	public String getRegExpression() {
		return regExpression;
	}
	public void setRegExpression(String regExpression) {
		this.regExpression = regExpression;
	}
}
