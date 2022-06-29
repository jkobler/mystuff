package kobler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.qna.utils.DataHandlingUtils;
import com.qna.web.controllers.DataController;

public class SQueeL {
	String jdbcUrl;
	String username;
	String password;
	String driver;
	int dbtype;
	String propsFilename = "squeel.ini";
	String outFilename = null;
	PrintStream lout = System.out;
	boolean doCommit = false,printLineNumbers = false;
	String sqlDateFormat = "dd-MMM-yyyy";
	Connection connection;
	String endOfStatement = "CRLF";
	String outDelimiter = "|";
	SimpleDateFormat sdf_log = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

 
	public static void main(String[] args) {
		try {
			//if (args.length> 0 && ("?".equals(args[0]) || "HELP".equalsIgnoreCase(args[0]))) RunSQLScripts.showHelp();
			//else {
				SQueeL sql = new SQueeL();
				if (sql.getLoginPrompt()) 
					sql.run();
			//}
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public SQueeL() throws Exception {
		super();
		readprops();
		Class.forName(driver);
	}
	
	//dbType - SQLSERVER = 1, ORACLE = 2, MYSQL = 3;

	boolean readprops() throws Exception {
		BufferedReader bin = new BufferedReader(new FileReader(propsFilename));
		String line, name, value;
		while((line= bin.readLine())!= null) {
			System.out.println(line);
			if (line.startsWith("--") || "".equals(line)) continue;
			else {
				name = line.substring(0,line.indexOf("="));
				value =  line.substring(line.indexOf("=")+1);
				if ("jdbcUrl".equalsIgnoreCase(name)) jdbcUrl = value;
				else if ("driver".equalsIgnoreCase(name)) driver = value;
				else if ("dbtype".equalsIgnoreCase(name)) {
					if ("SQLSERVER".equalsIgnoreCase(value)) dbtype = DataController.dbType_SQLSERVER;
					else if ("ORACLE".equalsIgnoreCase(value)) {
						dbtype = DataController.dbType_ORACLE;
						doCommit = true;
					}
					else if ("MYSQL".equalsIgnoreCase(value)) dbtype = DataController.dbType_MYSQL;
				}
				else if ("doCommit".equalsIgnoreCase(name)) doCommit = "true".equalsIgnoreCase(value);
				else if ("sqlDateFormat".equalsIgnoreCase(name)) sqlDateFormat = value;
				else if ("endOfStatement".equalsIgnoreCase(name)) endOfStatement = value;
				else if ("outDelimiter".equalsIgnoreCase(name)) outDelimiter = value;
				else if ("printLineNumbers".equalsIgnoreCase(name)) printLineNumbers = "true".equalsIgnoreCase(value);
				else if ("outFilename".equalsIgnoreCase(name)) {
					outFilename = value;
					lout = new PrintStream(new FileOutputStream(outFilename));
				}
			}
			
		}
		bin.close();
		return(true);
		
	}
	
	
	public boolean getLoginPrompt() throws Exception {
		boolean canLogin = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\nUsername:");
        username = br.readLine();		
        System.out.print("Password:");
        password = br.readLine();		
		return (canLogin);
		
	}
	
	public String getStatementPrompt() throws Exception {
		StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line="";
        while(! line.equalsIgnoreCase("y")) {
	        System.out.print("\nStatment (\'/Z\' when done):\n>");
	        line = br.readLine();
	        while (! line.equalsIgnoreCase("/z")) {
	        	sb.append(line);
	        	sb.append("\n");
	        	System.out.print(">");
	        	line = br.readLine();
	        	
	        }
	        System.out.print("\nIs this correct?\n ");
	        System.out.print(sb.toString());
	        System.out.print("\nY or N? ");
	        line = br.readLine();
        }
		return (sb.toString());
		
	}
	
	private int run() throws Exception {
		writeToLog("Started");
		//StringBuilder sb = new StringBuilder();
		String statement = getStatementPrompt();
		ArrayList<HashMap<String,String>> results;
		try {
			if (statement.startsWith("select") || statement.startsWith("SELECT")) {
				results=this.execQuery(statement);
				HashMap<String,String> row;
				Iterator<HashMap<String,String>> it = results.iterator();
				String col;
				for(int j = 1;it.hasNext();j++) {
					row = it.next();
					Set<String> columnNames = row.keySet();
					Iterator<String> itColumnNames = columnNames.iterator();
					StringBuilder sbRow = new StringBuilder();
					if (printLineNumbers) {
						sbRow.append(j);
						sbRow.append(this.outDelimiter);
					}
					while(itColumnNames.hasNext()) {
						col = itColumnNames.next();
						if (j == 1) {
							lout.print(col);
							lout.print(this.outDelimiter);
						}
						
						sbRow.append(row.get(col));
						sbRow.append(this.outDelimiter);
					}
					lout.println(sbRow.toString());
				}
			}
			else {
				statement = execPreparedStatment(statement);
				lout.println(statement);
			}
		
		}
		catch (Exception exc) {
			exc.printStackTrace(lout);	
			throw exc;
		}
		writeToLog("Finished");
	//declare victory \o/
		return(0);
		
	}
	
	/**
	 * Execute full SQL statements.
	 * @param dataSourceName
	 * @param sqlStatment
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> execQuery(String sqlStatment) throws Exception {
		ArrayList<HashMap<String,String>> results = new ArrayList<HashMap<String,String>>();
		SimpleDateFormat sdf = null;
		sdf = new SimpleDateFormat(sqlDateFormat);
		
		this.establishConnection();
		//if (this.properties.isDebug()) System.out.println(sqlStatment);
		Statement statement  = null;
		ResultSet rs = null;
		try{
			statement = connection.createStatement();
			
			if (sqlStatment.startsWith("SELECT") || sqlStatment.startsWith("select")) {
				rs = statement.executeQuery(sqlStatment);
				ResultSetMetaData rsmd = rs.getMetaData();
				
				while (rs.next()) {
					results.add(new HashMap<String,String>());
					for (int i=1; i <= rsmd.getColumnCount(); i++) {
						String name = rsmd.getColumnLabel(i);
						String value = rs.getString(i);
						String type = rsmd.getColumnTypeName(i);
						String classname = rsmd.getColumnClassName(i);
						if (type.matches(".*(date|DATE).*") && rs.getDate(i) != null  )
							value = sdf.format(rs.getDate(i));
						results.get(results.size()-1).put(name, value);
					}
				} 
				rsmd = null;
			}
			else {
				int affectedRows = statement.executeUpdate(sqlStatment);
				results.add(new HashMap<String,String>());
				results.get(results.size()-1).put("Affected Rows", Integer.toString(affectedRows));
			}
		}finally {
			if(rs != null){
				try{
					rs.close();
				}catch (SQLException sqlEx){
					// its usually ok to swallow these exceptions
				}
			}
			if(statement != null){
				try{
					statement.close();
				}catch (SQLException sqlEx){
					// its usually ok to swallow these exceptions
				}
			}
		}
		statement = null;
		return(results);
	}
	
	public String execPreparedStatment(String sqlStatment) throws Exception {
		StringBuilder results=new StringBuilder("");
		this.establishConnection();
		PreparedStatement cs = null;
		try{
			cs = connection.prepareStatement(sqlStatment);
			cs.execute();
	
			SQLWarning w = cs.getWarnings();
			while( w!=null){
				results.append(w.getMessage());
				results.append("\n");
				w=w.getNextWarning();
			}
		}finally {
			if(cs != null){
				try{
					cs.close();
				}catch (Exception sqlEx){
					// its usually ok to swallow these exceptions
				}
			}
		}
		
		return(results.toString());
	}
	
	public void establishConnection() throws Exception {
		if (connection == null || connection.isClosed()) {
			HashMap<String,String> dcVals = new HashMap<String,String>();
			dcVals.put("username", username);
			dcVals.put("password", password);
			String jdbcUrlComplete = DataHandlingUtils.buildStringFromSet(null, dcVals, jdbcUrl);
			connection = DriverManager.getConnection(jdbcUrlComplete);
		}
	}
	
	public void closeConnection() throws Exception {
		if (connection != null && !connection.isClosed()) {
			connection.close();
			connection = null;
		}
	}   

	//dbType - SQLSERVER = 1, ORACLE = 2, MYSQL = 3;
	
	public void writeToLog(String message) {
		lout.print(sdf_log.format(new Date()));
		lout.print(":");
		lout.println(message);
	}

}
