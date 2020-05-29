package csv_to_sqlite;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Table {

	private Connection conn;
	private String tableName;
	private Variable[] cols;
	private PreparedStatement istmt;
	
	public Table(Connection conn, String tableName, Variable[] cols) {
		this.conn = conn;
		this.tableName = tableName;
		this.cols = cols;
		this.istmt = makeInsertStatement(tableName, cols);
	}

	/**
	 * Creates an SQL insert statement as a PreparedStatement
	 * @param tableName: the name of the table
	 * @param cols: an array of Variable objects representing the columns of the table
	 * @return the corresponding insert statement
	 */
	private PreparedStatement makeInsertStatement(String tableName, Variable[] cols) {
		String insert = "INSERT INTO";
		String tableVars = tableName + "(";
		String values = "VALUES(";
		for(int i = 0; i < cols.length; i++) {
			tableVars += cols[i].getName();
			values += "?";
			if(i < cols.length - 1) {
				tableVars += ",";
				values += ",";
			}
			else {
				tableVars += ")";
				values += ")";
			}
		}
		String sql = insert + " " + tableVars + " " + values;
		try{
			return conn.prepareStatement(sql);
		}
		catch(SQLException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * Closes the PreparedStatement associated with the table
	 * Call this function when done with the Table instance
	 */
	public void close(){
		try{
			istmt.close();
		}
		catch (SQLException e) {
			
		}

	}
	
	/**
	 * 
	 * @param values: an array of strings to be inserted into the table
	 * @throws SQLException upon failure to pass the type check
	 */
	public void insert(String[] values) throws SQLException {
		if(values.length != this.cols.length) {
			System.out.println(Arrays.deepToString(values));
			throw new SQLException("Invalid length of input for insert statement.");
		}
		for(int i = 0; i < cols.length; i++) {
			//Sets null value if empty string
			if(values[i].length() == 0) {
				istmt.setObject(i+1, null);;
			}
			else {
				String type=cols[i].getType();
				String strVal = values[i];
				//If type is REAL, removes all characters that are not digits nor "." from the string
				//Attempts to insert the value as a double into the PreparedStatement
				//Throws an error if failure to insert as double
				if(type.toUpperCase().equals("REAL")) {
					String parsedString=values[i].replaceAll("[^\\d.-]", "");
					try {
						istmt.setDouble(i + 1, Double.parseDouble(parsedString));
					}
					catch(SQLException e) {
						throw e;
					}
				}
				//Does not insert anything if the string is neither "true" nor "false"
				//Throws an error later
				else if(type.equalsIgnoreCase("BOOLEAN")) {
					if(strVal.equalsIgnoreCase("true"))
						istmt.setBoolean(i + 1, true);
					else if(strVal.equalsIgnoreCase("false"))
						istmt.setBoolean(i + 1, false);
				}
				//Parses a base64 PNG representation from the string and inserts as a byte array
				//Fails if the string "png;base64," is not a substring preceding a valid PNG64 hexadecimal string representation
				else if(type.equalsIgnoreCase("PNG64")) {
					try{
						istmt.setBytes(i + 1, Base64.getDecoder().decode(strVal.substring(strVal.indexOf("png;base64,")+11)));
					}
					catch(SQLException e) {
						throw e;
					}
				}
				//Parses an integer from string
				//Fails if the string is not a valid integer
				else if(type.equalsIgnoreCase("INTEGER")) {
					try {
						istmt.setInt(i + 1, Integer.parseInt(strVal.strip()));
					}
					catch (SQLException e) {
						throw e;
					}
				}
				//Otherwise, inputs the string as a text value
				else {
					istmt.setString(i + 1, strVal);
				}
			}
		}
		//attempts to execute the update
		istmt.executeUpdate();
		//clear parameters after execution
		istmt.clearParameters();

	}

	/**
	 * Prints records from table up to the limit specified
	 */
	public void selectAll(int limit) {
		String sql = "SELECT * FROM " + tableName;
		if(limit > 0)
			sql += " LIMIT " + limit;
		try(Statement stmt = conn.createStatement();
				ResultSet rs    = stmt.executeQuery(sql)){
			while(rs.next()) {
				for(int i = 0; i < cols.length; i++) {
					String varName = cols[i].getName();
					String type= cols[i].getType();
					if(type.toUpperCase().contains("INT"))
						System.out.print(rs.getInt(varName) + "\t");
					else if(type.toUpperCase().contains("DECIMAL") || type.toUpperCase().contains("NUMERIC"))
						System.out.print(rs.getDouble(varName) + "\t");
					else if(type.equalsIgnoreCase("BOOLEAN"))
						System.out.print(rs.getBoolean(varName) + "\t");
					else
						System.out.print(rs.getString(varName) + "\t");
				}
				System.out.print("\n");
			}
		}
		catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}


	public static void main(String[]args) {
		Variable[] attrs=new Variable[10];
		attrs[0]=new Variable("A", "TEXT");
		attrs[1]=new Variable("B", "TEXT");
		attrs[2]=new Variable("C", "TEXT");
		attrs[3]=new Variable("D", "TEXT");
		attrs[4]=new Variable("E", "PNG64");
		attrs[5]=new Variable("F", "TEXT");
		attrs[6]=new Variable("G", "REAL");
		attrs[7]=new Variable("H", "BOOLEAN");
		attrs[8]=new Variable("I", "BOOLEAN");
		attrs[9]=new Variable("J", "TEXT");
		

	}
	
}
