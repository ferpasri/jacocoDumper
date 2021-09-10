package org.testar.main.statemodel;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

public class StateModelMetrics {

	private StateModelMetrics() {}

	/**
	 * Use the OrientDB session to extract the state and actions metrics. 
	 *  
	 * @param db
	 * @return
	 */
	public static String extractStateModelMetrics() {
		OrientDB orientDB;
		ODatabaseSession db;
		try {
			orientDB = new OrientDB("remote:localhost/", "testar", "testar", OrientDBConfig.defaultConfig());
			db = orientDB.open("testar", "testar", "testar");
		} catch (Exception e) {
			e.printStackTrace();
			return "Auth error";
		}

		String resultAbstractStates = "AbstractStates " + queryStatement(db, "select count(*) from AbstractState");
		String resultAbstractActions = "AbstractActions " + queryStatement(db, "select count(*) from AbstractAction");
		String resultUnvisitedActions = "UnvisitedActions " + queryStatement(db, "select count(*) from UnvisitedAbstractAction");
		String resultConcreteStates = "ConcreteStates " + queryStatement(db, "select count(*) from ConcreteState");
		String resultConcreteActions = "ConcreteActions " + queryStatement(db, "select count(*) from ConcreteAction");

		db.close();
		orientDB.close();

		// Prepare and write the state model metrics information
		return (resultAbstractStates +
				" | " + resultAbstractActions +
				" | " + resultUnvisitedActions +
				" | " + resultConcreteStates +
				" | " + resultConcreteActions);
	}

	/**
	 * Execute the desired query. 
	 * 
	 * @param db
	 * @param stmt
	 * @return
	 */
	private static String queryStatement(ODatabaseSession db, String stmt) {
		try {
			OResultSet resultSet = db.query(stmt);
			while(resultSet.hasNext()) {
				OResult result = resultSet.next();
				return extractNumber(result.toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
			return "EXC";
		}
		return "0";
	}

	private static String extractNumber(final String str) {                
		if(str == null || str.isEmpty()) return "NaN";

		StringBuilder sb = new StringBuilder();
		boolean found = false;
		for(char c : str.toCharArray()){
			if(Character.isDigit(c)){
				sb.append(c);
				found = true;
			} else if(found){
				// If we already found a digit before and this char is not a digit, stop looping
				break;                
			}
		}

		return sb.toString();
	}

}
