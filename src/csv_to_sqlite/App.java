package csv_to_sqlite;

/**
 * 
 * @author Kevin
 *	Run this
 */
public class App {

	//Target directory path. Must be a valid existing directory
	static String directory="C://sqlite/db/";
	//Path of the csv you want to convert
	static String csvPath="C:/Users/Kevin/eclipse-workspace/MS3/src/ms3Interview.csv";
	
	public static void csvToSQLite(String directory, String csvPath) {
		CsvToSQLite.convert(directory, csvPath);
	}
	
	public static void main(String[] args) {
    	App.csvToSQLite(directory, csvPath);
    	System.exit(0);
	}
	
}
