package csv_to_sqlite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * 
 * @author Kevin
 * Class to insert a csv into a SQLite database
 */
public class CsvToSQLite {

	/**
	 * Converts a comma-delimited string to an array of strings
	 * Values containing commas should be surrounded by ""
	 * Values containing exclusively whitespace characters will be converted to empty strings
	 * @param row
	 * @return a corresponding array of strings
	 */
	public static String[] readRow(String row) {
		//buffer the beginning with a space so string split treats initial missing value as a column
		if(row.startsWith(","))
			row=" "+row;
		//Handling internal commas
		String[] quoteSplit=row.split("\"");
		ArrayList<String> entryList=new ArrayList<String>();
		boolean endcomma=false;
		boolean prevcomma;
		for(String multicol:quoteSplit) {
			prevcomma=endcomma;
			multicol=multicol.strip();
			if(multicol.endsWith(",")) {
				multicol=multicol.substring(0,multicol.length()-1);
				endcomma=true;
			}
			else
				endcomma=false;
			if(multicol.startsWith(",")) 
				multicol=multicol.substring(1);
			if(prevcomma==true)
				entryList.add(multicol);
			else {
				String[] colarray=multicol.split(",");
				for(String col:colarray) {
					if(col.matches("\\w{1,}"))
						entryList.add("");
					else
						entryList.add(col);

				}
			}
		}
		String[] entry=new String[entryList.size()];
		for(int i = 0; i < entry.length; i++)
			entry[i] = entryList.get(i);
		return entry;
	}
	
	
	public static void csvToTable(Database db, String tableName, String csvPath) {
		String row;
		Table table = null;
		String[] header = null;
		try (BufferedReader csvReader = new BufferedReader(new FileReader(csvPath))){
			row=csvReader.readLine();
			header=row.split(",");
			TypePrompt prompt=new TypePrompt(header);
			String[] varTypes = prompt.launchApp();
			Variable[] cols=new Variable[header.length];
			for(int i=0; i < cols.length; i++)
				cols[i] = new Variable(header[i], varTypes[i]);
			
			table=db.createNewTable(tableName, cols);
			
			int numReceived = 0;
			int numGood = 0;
			int numBad = 0;
			List<String[]> badEntries = new ArrayList<String[]>();
			while ((row = csvReader.readLine()) != null) {
				numReceived++;
				String[] entry = readRow(row);
				//if number of columns in entry is a match, insert into database
				if(entry.length==header.length) {
					try{
						table.insert(entry);
						numGood++;
					}
					catch (Exception e) {
						badEntries.add(entry);
						numBad++;
					}
				}
				//otherwise, write entry to error csv
				else {
					badEntries.add(entry);
					numBad++;
				}
			}
			System.out.println(db.commit());
			
			//Writes bad entries to a separate csv file
			writeToCsv(db.getDirectory() + tableName + "-bad.csv", badEntries);
			//Creates a log to record statistics
			logStats(db.getDirectory() + tableName + ".log", numReceived, numGood, numBad);
		}
		catch(Exception e) {
			System.out.println(e);
		}
		finally {
			if(table != null)
				table.close();
		}
	}
	
	//Writes a list of records to a specified csv file
	public static void writeToCsv(String fileName, List<String[]> records) throws IOException {
		File file = new File(fileName);
		FileWriter writer = new FileWriter(file);
		for(String[] entry: records) {
			for(int i=0; i<entry.length; i++)
				if(entry[i].indexOf(',') != -1)
					entry[i] = "\"" + entry[i] + "\"";
			writer.append(String.join(",", entry));
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}

	//Parses out a filename from an absolute or relative path
	public static String parseFileName(String filePath) {
		while(filePath.length() > 0 && filePath.endsWith("/"))
			filePath=filePath.substring(0, filePath.length()-1);
		String[] split = filePath.split("/");
		String[] dotsplit = split[split.length-1].split("\\.");
		return dotsplit[0];
	}
	
	//Method to log statistics
	private static void logStats(String filePath, int numReceived, int numGood, int numBad) throws SecurityException, IOException {
        boolean append = true;
        FileHandler handler = new FileHandler(filePath, append);
        Logger logger = Logger.getLogger(CsvToSQLite.class.getName());
        logger.addHandler(handler);
        logger.info("Number of records received: " + numReceived + "\n"
        			+ "Number of records successfully inserted: " + numGood + "\n"
        			+ "Number of records failed: " + numBad);
        handler.close();
	}
	
	/**
	 * Creates and connects to a database with the same name as an input csv file in the given directory
	 * If a database with the given name already exists, connects to it.
	 * Creates a table with the same name as the input csv file in the database and uploads records into the table.
	 * Terminates if a table with the same name already exists in the database.
	 */
	public static void convert(String directory, String csvFile) {
		String fileName = parseFileName(csvFile);
		Database db = new Database(directory, fileName + ".db");
		if(db.containsTable(fileName)) {
			System.out.println("The table " + fileName + " already exists in " + fileName);
			if(db != null)
				db.close();
			return;
		}
		csvToTable(db, fileName, csvFile);
		db.close();
	}
	
	public static void main(String[] args) {
    	String directory="C://sqlite/db/";
    	String csvPath="C:/Users/Kevin/eclipse-workspace/MS3/src/ms3Interview.csv";
		convert(directory, csvPath);
		//Table table = db.selectTable("tabletest");
		//System.out.println(Arrays.deepToString(db.getColumnTypes("tabletest")));
		//System.out.println(db.containsTable("tabletest"));
		//table.selectAll(5);
	}
}
