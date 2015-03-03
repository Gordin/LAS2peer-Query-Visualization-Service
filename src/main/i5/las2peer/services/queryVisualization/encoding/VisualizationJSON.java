package i5.las2peer.services.queryVisualization.encoding;

import i5.las2peer.security.Context;

import java.sql.Date;
import java.sql.Time;
import java.sql.Types;
import java.util.Iterator;


/**
 * VisualizationJSON.java
 * 
 * Transforms/Converts a methodResult into a JSON-String.
 * The data types of each column is denoted as String as used in JavaScript, e.g. "number", "string", "date", etc.
 * 
 */
public class VisualizationJSON extends Visualization {

	public VisualizationJSON() {
		super(VisualizationType.JSON);
	}
	
	public String generate(MethodResult methodResult, String[] visualizationParameters){
		try {
			if(methodResult == null) {
				throw new Exception("Tried to transform an invalid (row list) result set into JSON!");
			}
			
			String[] columnNames = methodResult.getColumnNames();
			Integer[] columnTypes = methodResult.getColumnDatatypes();
			Iterator<Object[]> iterator = methodResult.getRowIterator();
			int columnCount = columnTypes.length;
			
			// column names
			String jsonString = "[ \n[";
			for (int i = 0; i < columnCount; i++) {
				if(i>0) {
					jsonString += ", ";
				}
				
				jsonString += "\"" + columnNames[i]+ "\"";
			}
			jsonString += " ]";
			
			// column types as one row
			jsonString += ", \n[ ";
			for (int i = 0; i < columnCount; i++) {
				
				if(i>0) {
					jsonString += ", ";
				}
				
				String columnTypeString = "string";
				switch(columnTypes[i]) {
					case Types.BOOLEAN:
						columnTypeString = "boolean";
						break;
					case Types.DATE:
						columnTypeString = "date";
						break;
					case Types.TIME:
					case Types.TIMESTAMP:
						columnTypeString = "datetime";
						break;
					case Types.BIGINT:
					case Types.DECIMAL:
					case Types.DOUBLE:
					case Types.FLOAT:
					case Types.INTEGER:
					case Types.NUMERIC:
					case Types.REAL:
					case Types.SMALLINT:
						columnTypeString = "number";
						break;
					default:
						// do nothing, just treat it as string
						break;
				};
				
				jsonString += "\"" + columnTypeString+ "\"";
			}
			
			jsonString += " ] ";
			
	
			// add the individual rows
			while(iterator.hasNext()) {
				jsonString += ", \n";
				
				jsonString += "[ ";
				
				Object[] currentRow = iterator.next();
				for(int i = 0; i < columnCount; i++) {
					if(i>0) jsonString += ", ";
					switch(columnTypes[i]) {
						case Types.DATE:
							//TODO: this is wrong, it starts counting the month at 0...								
							try {
								jsonString += "\"" + ((Date) currentRow[i]).getTime() + "\"";
							} catch (Exception e) {
								jsonString += "null";
							}
							break;
						case Types.TIME:
						case Types.TIMESTAMP:
							try {
								jsonString += "\"" + ((Time) currentRow[i]).getTime() + "\"";
							} catch (Exception e) {
								jsonString += "null";
							}
							break;
						case Types.BOOLEAN:
						case Types.BIGINT:
						case Types.DECIMAL:
						case Types.NUMERIC:
						case Types.DOUBLE:
						case Types.REAL:
						case Types.FLOAT:
						case Types.INTEGER:
						case Types.SMALLINT:
							jsonString += currentRow[i];
							break;
						default:
							// filter the values, so that the client side does not get in trouble...
							String value = (String) currentRow[i];
							value = value.replace('\n', ' ');
							value = value.replace('\r', ' ');
							value = value.replace("\\", "\\\\");
							value = value.replace("\"", "\\\"");
							jsonString += "\"" + value + "\"";
							break;
					};
				}
				jsonString += "]";
			}
			jsonString += " \n]";
			
			return jsonString;
		} 
		catch (Exception e) {
			Context.logMessage(this, e.getMessage());
			try {
				return  super.visualizationException.generate(e, "Encoding into JSON format failed.");
			}
			catch(Exception ex) {
				Context.logMessage(this, ex.getMessage());
				return "Unknown/handled error occurred!";
			}
		}
	}
	
	//Always true
	public boolean check(MethodResult methodResult, String[] visualizationParameters) {
		return true;
	}
	
}
