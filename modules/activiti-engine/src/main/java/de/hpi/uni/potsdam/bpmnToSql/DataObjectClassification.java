package de.hpi.uni.potsdam.bpmnToSql;

import org.activiti.engine.impl.bpmn.parser.BpmnParse;

public class DataObjectClassification {

	public static Boolean isMainDataObject(DataObject dataObj, String scopeName) {
		if (dataObj.getName().equalsIgnoreCase(BpmnParse.getScopeInformation().get(scopeName))) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean isDependentDataObject(DataObject dataObj, String scopeName) {
		//annahme: FkList ist  für jedes dep. do nicht leer
		if(!dataObj.getName().equalsIgnoreCase(BpmnParse.getScopeInformation().get(scopeName)) && dataObj.getIsCollection()!=true){
//			boolean fkCheck = true;
//			for (String fk : dataObj.getFkeys()) {
//				if (fk == null) {
//					fkCheck = false;
//				}
//			}
//			return fkCheck;
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean isDependentDataObjectWithUnspecifiedFK(DataObject dataObj, String scopeName) {
		if(!dataObj.getName().equalsIgnoreCase(BpmnParse.getScopeInformation().get(scopeName))){
			boolean fkNull = false;
			for (String fk : dataObj.getFkeys()) {
				if (fk == null) {
					fkNull = true;
				}
			}
			return fkNull;
		} else {
			return false;
		}
	}
	
	public static Boolean isMIDependentDataObject(DataObject dataObj, String scopeName) {
		if(!dataObj.getName().equalsIgnoreCase(BpmnParse.getScopeInformation().get(scopeName)) && dataObj.getIsCollection()==true){
//			boolean fkCheck = true;
//			for (String fk : dataObj.getFkeys()) {
//				if (fk == null) {
//					fkCheck = false;
//				}
//					
//			}
//			return fkCheck;
			return true;
		} else {
			return false;
		}
	}
	
	
//	public static Boolean isMIDependentDataObjectWithUnspecifiedFK(DataObject dataObj, String scopeName) {
//		if(!dataObj.getName().equalsIgnoreCase(BpmnParse.getScopeInformation().get(scopeName)) && dataObj.getIsCollection()==true){
//			boolean fkNull = false;
//			for (String fk : dataObj.getFkeys()) {
//				if (fk == null) {
//					fkNull = true;
//				}
//			}
//			return fkNull;
//		} else {
//			return false;
//		}
//	}
	
	public static Boolean isNumberingInformationDataObject(DataObject dataObj) {
		if (dataObj.getPkey().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
