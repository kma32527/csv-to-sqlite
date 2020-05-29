# csv-to-sqlite

## Summary
This is an application to migrate data from a csv file into a SQLite database table.

## Getting started
1) Clone the project to a local repository.
2) Open App.java.
3) Set the variable "directory" to a valid target directory for the SQLite database to be created in.
4) Set the "csvPath" variable to a valid path to a csv file.
5) Run App.java.
6) If successful, a prompt will pop up with a list of extracted column names. Next to each column name, select the type of each column from the drop down box and close the prompt when done.

If successful, the directory should contain three files with names dependent on \<input-filename\>.csv.
1) \<input-filename\>.db: A database containing a single table with the name \<input-filename\> containing successfully inserted records from the input csv file.
2) \<input-filename\>.log: A log with an entry containing the number of records received, number of records successfully inserted into the database, and number of records failed.
3) \<input-filename\>-bad.csv: A csv file containing the records that failed to be inserted.

## Overview
This project consists of 6 classes
- App: A runnable, non-instantiable class.
- CsvToSQLite: A non-instantiable class for converting a csv to a SQLite database table. 
  - The static method convert(String directory, String csvPath) creates a database in the specified directory containing a table with successfully inserted records from the specified csv file.
