# csv-to-sqlite

## Summary
This is an application to migrate data from a csv file into an SQLite database table. This app supports TEXT, INTEGER, REAL, BOOLEAN, and PNG base 64 data types, with type checking performed when inserting records into the SQLite table.

## Getting started
To run this application:

1) Clone the project to a local repository.
2) Open App.java.
3) Set the variable "directory" to a valid target directory for the SQLite database to be created in.
4) Set the variable "csvPath" to a valid path to a csv file.
5) Run App.java.
6) If successful, a prompt will pop up with a list of extracted column names. Next to each column name, select the type of each column from the drop down box and close the prompt when done.


If successful, the directory should contain three files with names dependent on \<input-filename\>.csv.
1) \<input-filename\>.db: A database containing a single table with the name \<input-filename\> containing successfully inserted records from the input csv file.
2) \<input-filename\>.log: A log with an entry containing the number of records received, number of records successfully inserted into the database, and number of records failed.
3) \<input-filename\>-bad.csv: A csv file containing the records that failed to be inserted.

### Why is the application not doing anything and telling me that the table already exists?
This occurs when a table with the name \<input-filename\> already exists in the database. Delete the table from the database (you can use the Database method dropTable()) and this application should run correctly. To guarantee everything runs as intended, make sure to delete \<input-filename\>.log and \<input-filename\>-bad.csv from the directory as well if they already exist.

## General overview

### Requirements on the input csv file
The input csv file should satisfy the following requirements:
1) The first row of the csv should contain the column names with no missing values.
2) Values containing internal commas must be enclosed by double quotes.
   - e.g. the string "\\"This,contains,commas\\"" is parsed as a single value, while the string "This,contains,commas" is parsed as 3 distinct values.

### Type checking on insertion
While SQLite support dynamic typing and allows for insertion of values independent of column type, this application performs a type check during insertion into a table. If an entry does not pass the type check, it fails to be inserted and is treated as a bad entry. Missing values and values consisting exclusively of whitespace characters are stored as null.

The following 5 types are supported by this application:
1) TEXT: A generic type for storing text data. This inserts a string as-is.
2) INTEGER: Stores integer values
   - On insertion, the application calls Integer.parseInt(String str) on the value and inserts the result. If this fails to parse an integer, then the entry fails.
3) REAL: Stores decimal values
   - On insertion, the application strips all characters that are neither digits, '.', nor '-'. If the remaining string can be parsed into a double by Double.parseDouble(String str), then the value is inserted. Otherwise, the entry fails.
4) BOOLEAN: Stores boolean values
   - The value must equal (ignoring case) either "True" or "False" and is inserted as such. Otherwise, the entry fails. "F", "T", "0", and "1" will all fail.
5) PNG64: Stores a PNG image encoded in Base64 as a byte array.
   - The value must contain the substring "png;base64," as a substring, followed by a valid base64 representation of a PNG image.

## Code Overview
This project consists of 6 classes

### App
A runnable, non-instantiable class.

### CsvToSQLite
A non-instantiable class for converting a csv to an SQLite database table. 

- The static method convert(String directory, String csvPath) creates the following files in the specified directory:
   1) A database in the specified directory containing a table with successfully inserted records from the specified csv file.
   2) A csv file in the specified directory containing the records that failed to insert into the created table
   3) A log file detailing the number of records found in the csv, the number of records successfully inserted into the table, and the number of records that failed to be inserted
- This class contains various other additional helper methods.
  
### Database
An instantiable class that connects to an SQLite database via JDBC.
- Attributes:
  - String directory: The path of the parent directory of the database.
  - String dbName: The name of the database ending in .db.
  - Connection conn: The Connection instance that connects to the database via JDBC.
- The constructor creates a Connection instance and has the following parameters:
  - String directory: The path of the parent directory of the database.
  - String dbName: The name of the database ending in .db.
- Autocommit is set to off; to commit changes to the database, call commit().
- To create a new table in the database, use the method createNewTable(String tableName, Variable[] cols) to create a new table. This method returns a corresponding Table object. If a table with the name tableName already exists, this method does nothing and returns null.
- To select an existing table in the database, use the method selectTable(String tableName). If the table exists in the database, this returns a corresponding Table object, and null otherwise.
- To delete an existing table from the database, use the method dropTable(String tableName) to delete the named table.
- When done with a Database instance, call close() on the Database to close the JDBC connection. While garbage collection should automatically close this connection, every now and then this connection may not close automatically and require restarting your computer to reset it.
  
### Table
An instantiable class that provides a Java representation of an SQLite table. In general, Table objects should be created through a Database instance.
- Attributes:
  - Connection conn: The Connection instance connecting to the parent database.
  - String tableName: The name of the table.
  - Variable[] cols: An array of Variable objects containing the name and type of each column.
  - PreparedStatement istmt: A PreparedStatement object representing a precompiled insert statement for this table.
- The constructor generates the insertion PreparedStatement and has the following parameters:
  - Connection conn: The Connection instance connecting to the parent database.
  - String tableName: The name of the table.
  - Variable[] cols: An array of Variable objects containing the name and type of each column.
- To insert a string array into a table, use the method insert(String[] values).
  - Throws a SQLException without inserting anything into the table in the following cases:
    - The array is incorrectly sized
    - At least one value in the array failed to pass the type check
- To print values from the table, either call selectTable() to print all records in the table or selectTable(int limit) to print up to the specified number of records.
- When done with a Table instance, call close() to close the PreparedStatement.

### TypePrompt
A prompt that pops up when executing CsvToSQLite.convert(). The object method launchApp() Creates a JDialog that pauses execution of the program and allows the user to input the desired types of each column for each variable from a list of valid column types. This thn returns the selected types to be used to create an array of Variable objects. 

### Variable
An instantiable class containing a column's name and type.
- Attributes:
  - String name: The name of a column
  - String type: The type of a column
- Contains a String array of valid column types
