package csv_to_sqlite;

public class Variable {
	final static String[] validTypes = {"TEXT", "INTEGER", "REAL", "NUMERIC", "BOOLEAN", "PNG64"};
	private String name;
	private String type;
	
	public Variable(String name, String type){
		this.name=name;
		this.type=type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
}
