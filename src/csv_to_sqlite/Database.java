package csv_to_sqlite;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class Database {
	
	private String directory;
	private String dbName;
	private Connection conn;
	
	public Database(String directory, String dbName) {
		this.directory=directory;
		this.dbName=dbName;
		conn=this.connect(directory, dbName);
	}
	
	public String getDirectory() {
		return directory;
	}
	
	/**
	 * Closes the connection to the database
	 * Call this function when done with the database
	 */
	public void close(){
		try{
			conn.close();
		}
		catch (SQLException e) {
			
		}
	}
	
	/**
	 * commits queries
	 * returns true on successful commit
	 */
	public boolean commit() {
		try{
			conn.commit();
			return true;
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	 /**
	  * @param directory: name of directory containing the database
	  * @param dbName: name of database
	  * Connect to the database dbName in given directory
	  * If database with name dbName does not exists, creates a new database with the name
	  * @return the Connection object
	  */
	 private Connection connect(String directory, String dbName) {
		 // appends '/' onto end of directory name if not already there
		 directory=(directory.endsWith("/") ? directory : directory + "/");
		 // appends ".db" onto end of database name if not already there
		 dbName = (dbName.endsWith(".db") ? dbName : dbName + ".db");
		 // SQLite connection string
		 String url = "jdbc:sqlite:" + directory + dbName;
		 Connection conn = null;
		 try {
			 File file=new File(directory + dbName);
			 if(file.exists()) {
				 System.out.println("Database " + dbName + " already exists");
			 }
			 else {
				 System.out.println("Creating new database " + dbName);
			 }
			 conn = DriverManager.getConnection(url);
			 conn.setAutoCommit(false);
			 System.out.println("Successfully connected to " + dbName + ".");
		 } catch (SQLException e) {
			 System.out.println(e.getMessage());
		 }
		 return conn;
	 }
	
	 /**
	  * Creates a table in the database and returns the corresponding Table object
	  * Does nothing and returns null if the table already exists in the database
	  * @param tableName: the name of the table to be created
	  * @param cols: an array of Variable objects representing the name and type of each column
	  * @return a corresponding Table object
	  */
	 public Table createNewTable(String tableName, Variable[] cols) {
		 if(containsTable(tableName)) {
			 System.out.println("Error: the table " + tableName + " already exists in " + dbName);
			 return null;
		 }
		 // constructs an SQLite query using the tableName and column names+types
		 String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n"
				 + "	id integer PRIMARY KEY,\n";
		 for(int i=0; i<cols.length; i++) {
			 if(i<cols.length-1) {
				 sql += "	" + cols[i].getName() + " " + cols[i].getType() + ",\n"; 
			 }
			 else
				 sql += "	" + cols[i].getName() + " " + cols[i].getType() + "\n";
		 }
		 sql+=");";
		 System.out.println(sql);
		 try (Statement stmt = conn.createStatement()) {
			 // create a new table
			 stmt.execute(sql);
			 System.out.println("Table " + tableName + " has been created");
			 // returns a corresponding Table object
			 return new Table(conn, tableName, cols);
		 } catch (SQLException e) {
			 System.out.println(e.getMessage());
			 //returns null on failure
			 return null;
		 }
	 }
	 
	 /**
	  * 
	  * @param tableName: the name of the table
	  * @return a boolean representing the existence of the table in the database
	  */
	 public boolean containsTable(String tableName) {
		 String sql="SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
		 try(Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql)){
			return rs.next();
		 }
		 catch (SQLException e) {
//			 System.out.println(e.getMessage());
			 return false;
		 }
	 }
	 
	 /**
	  * 
	  * @param tableName: name of the table
	  * @return an array of strings containing the name of each column in order
	  */
	 public String[] getColumnNames(String tableName) {
		 String sql = "SELECT * FROM " + tableName + " LIMIT 1";
		 String[] columnNames=null;
		 try (Statement stmt  = conn.createStatement();
				 ResultSet rs    = stmt.executeQuery(sql)){

			 // loop through the result set
			ResultSetMetaData rm = rs.getMetaData();
			columnNames=new String[rm.getColumnCount()];
			for(int i=0; i<columnNames.length; i++) {
				columnNames[i]=rm.getColumnName(i+1);
			}
					
		 } catch (SQLException e) {
			 System.out.println(e.getMessage());
		 }
		 return columnNames;
	 }
	 
	 /**
	  * 
	  * @param tableName: name of table
	  * @return an array of strings containing the type of each column in order
	  */
	 public String[] getColumnTypes(String tableName) {
		 String sql = "SELECT * FROM " + tableName + " LIMIT 1";
		 String[] columnTypes=null;
		 try (Statement stmt  = conn.createStatement();
				 ResultSet rs    = stmt.executeQuery(sql)){

			 // loop through the result set
			ResultSetMetaData rm = rs.getMetaData();
			columnTypes=new String[rm.getColumnCount()];
			for(int i=0; i<columnTypes.length; i++) {
				columnTypes[i]=rm.getColumnTypeName(i+1);
			}
					
		 } catch (SQLException e) {
			 System.out.println(e.getMessage());
		 }
		 return columnTypes;
	 }

	 /**
	  * 
	  * @param tableName: name of table
	  * @return the corresponding Table object representing an existing table in the database
	  * 		if the table does not exist in the database, returns null
	  */
	 public Table selectTable(String tableName) {
		 if(!containsTable(tableName)) {
			 System.out.println("The table " + tableName + " does not exist in " + dbName);
			 return null;
		 }
		 String[] columnNames = getColumnNames(tableName);
		 String[] columnTypes = getColumnTypes(tableName);
		 Variable[] columns = new Variable[columnNames.length];
		 for(int i=0; i<columns.length; i++) {
			 columns[i]=new Variable(columnNames[i], columnTypes[i]);
		 }
		 return new Table(conn, tableName, columns);
	 }
	 
	 /**
	  * Drops the named table from the database
	  */
	 public void dropTable(String tableName) {
		 String sql = "DROP TABLE IF EXISTS " + tableName;
		 try (Statement stmt = conn.createStatement()){
			 stmt.executeQuery(sql);
		 }
		 catch (SQLException e) {
			 System.out.println(e.getMessage());
		 }
	 }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	String directory="C://sqlite/db/";
    	String fileName="test.db";
    	Database db=new Database(directory, fileName);
    	
    	Variable[] attrs=new Variable[10];
		attrs[0]=new Variable("A", "TEXT");
		attrs[1]=new Variable("B", "TEXT");
		attrs[2]=new Variable("C", "TEXT");
		attrs[3]=new Variable("D", "TEXT");
		attrs[4]=new Variable("E", "TEXT");
		attrs[5]=new Variable("F", "TEXT");
		attrs[6]=new Variable("G", "DECIMAL(10, 2)");
		attrs[7]=new Variable("H", "BOOLEAN");
		attrs[8]=new Variable("I", "BOOLEAN");
		attrs[9]=new Variable("J", "TEXT");
		//Table table = db.createNewTable("tabletest", attrs);
		
		//table.insert(new String[] {"a", "b", "c", "d", "e", "f", "$4.236", "false", "true", "j"});
		//table.insert(new String[] {"A", "b", "c", "d", "e", "f", "$2.23", "false", "true", "j"});
		//db.insert("tabletest", "Bob", 5, "TRUE");
		//db.insert("tabletest", "Sob", 6, "FALSE");
		//db.insert("tabletest", "poop", -1, "TRUE");
		Table table=db.selectTable("tabletest");
		table.selectAll(2);
		table.close();
		db.close();
    }
}
