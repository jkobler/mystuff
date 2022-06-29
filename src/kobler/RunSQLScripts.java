package kobler;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.HashMap;
import java.util.Iterator;

import com.qna.utils.DataHandlingUtils;
import com.qna.web.controllers.DataController;

public class RunSQLScripts implements Runnable {
	
	String scriptFileName;
	int linesPerRead = 0;
	String jdbcUrl;
	String username;
	String password;
	String driver;
	int dbtype;
	int skiplines=0,readto=100;
	String propsFilename = "propsRunSQLScripts.ini";
	PrintStream out = System.out;
	boolean doCommit = false,printLineNumbers = false,useSemicolon = true;
	String sqlDateFormat = "dd-MMM-yyyy";
	Connection connection;
	String mode = "INSERT";
	String endOfStatement = "CRLF";
	String outDelimiter = "|";
	String removeTheseCharsRegex = "";

	public static void main(String[] args) {
		try {
			if ("?".equals(args[0]) || "HELP".equalsIgnoreCase(args[0])) RunSQLScripts.showHelp();
			else {
				RunSQLScripts rss = new RunSQLScripts(args[0]);
				if (rss.getLoginPrompt())
					rss.run();
			}
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public boolean getLoginPrompt() throws Exception {
		boolean canLogin = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("\nUsername:");
        username = br.readLine();		
        System.out.print("\nPassword:");
        password = br.readLine();		
		return (canLogin);
		
	}
	
	public RunSQLScripts(String propsFilename) throws Exception {
		super();
		if (propsFilename != null && !"".equals(propsFilename)) 
			this.propsFilename = propsFilename;
		readprops();
		Class.forName(driver);
	}
	
	public void establishConnection() throws Exception {
//		if (connection == null || connection.isClosed()) {
			HashMap<String,String> dcVals = new HashMap<String,String>();
			dcVals.put("username", username);
			dcVals.put("password", password);
			String jdbcUrlComplete = DataHandlingUtils.buildStringFromSet(dcVals, dcVals, jdbcUrl);
			connection = DriverManager.getConnection(jdbcUrlComplete);
			System.out.println("connected");
//		}
	}
	
	public void closeConnection() throws Exception {
		if (connection != null && !connection.isClosed()) {
			connection.close();
			connection = null;
		}
	}   

	//dbType - SQLSERVER = 1, ORACLE = 2, MYSQL = 3;

	boolean readprops() throws Exception {
		BufferedReader bin = new BufferedReader(new FileReader(propsFilename));
		String line, name, value;
		while((line= bin.readLine())!= null) {
			System.out.println(line);
			if (line.startsWith("--")) continue;
			name = line.substring(0,line.indexOf("="));
			value =  line.substring(line.indexOf("=")+1);
			if ("jdbcUrl".equalsIgnoreCase(name)) jdbcUrl = value;
			else if ("scriptFileName".equalsIgnoreCase(name)) scriptFileName = value;
			else if ("logFileName".equalsIgnoreCase(name)) out = new PrintStream(new File(value)); 
			else if ("linesPerRead".equalsIgnoreCase(name)) linesPerRead = Integer.valueOf(value);
//			else if ("username".equalsIgnoreCase(name)) username = value;
//			else if ("password".equalsIgnoreCase(name)) password = value;
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
			else if ("mode".equalsIgnoreCase(name)) mode = value;
			else if ("endOfStatement".equalsIgnoreCase(name)) endOfStatement = value;
			else if ("outDelimiter".equalsIgnoreCase(name)) outDelimiter = value;
			else if ("removeTheseCharsRegex".equalsIgnoreCase(name)) removeTheseCharsRegex = value;
			else if ("skiplines".equalsIgnoreCase(name)) skiplines = Integer.valueOf(value);
			else if ("readto".equalsIgnoreCase(name)) readto = Integer.valueOf(value);
			else if ("printLineNumbers".equalsIgnoreCase(name)) printLineNumbers = "true".equalsIgnoreCase(value);
			else if ("useSemicolon".equalsIgnoreCase(name)) useSemicolon = "true".equalsIgnoreCase(value);
			
		}
		bin.close();
		return(true);
		
	}

	public static void showHelp() {
		System.out.println("Squeel is for reading the first few lines of a large data file or doing batch inserts of the same, as long as it's all INSERT statements. ");
		System.out.println("command line systax:\n\tjava -jar squeel.jar \"[name_of_ini_file.ini]\" ");
		System.out.println("The ini file can contain the following: ");
		System.out.println("jdbcUrl: is the JDBCURL. example\n\tjdbc:oracle:thin:[username_re]/[password_re]@servername.domain.net:1521:MYSSID;");
		System.out.println("scriptFileName: the file wherein INSERT statements are held.");
		System.out.println("logFileName: name and path of log output file. Default is stdout."); 
		System.out.println("linesPerRead: records to insert per batch.");
		System.out.println("username: db username if using replaceable values in JDBCURL");
		System.out.println("password: db password if using replaceable values in JDBCURL. \n\tWARNING, THIS IS NOT ENCRYPTED SO USE AN ACCOUNT WITH THE LEAST POSSIBLE PERMISSIONS NEEDED.");
		System.out.println("driver: driver class for JDBC Connection. oracle.jdbc.driver.OracleDriver or com.microsoft.sqlserver.jdbc.SQLServerDriver are currently supported");
		System.out.println("dbtype: corresponds with driver. SQLSERVER or ORACLE");
		System.out.println("doCommit: TRUE or FALSE. For Oracle. Default is TRUE.");
		System.out.println("sqlDateFormat: not used at this time.");
		System.out.println("mode: INSERT or PRINT. PRINT just prints x number of lines to the log output.");
		System.out.println("endOfStatement: To be implemented");
		System.out.println("outDelimiter: To be implemented");
		System.out.println("removeTheseCharsRegex: Used in INSERT mode, A REGEX statement to search for and remove those pesky special characters that blow your shit up. ");
		System.out.println("skiplines: Used in INSERT mode to skip lines 0-n to avoid headers, brag tags, etc...");
		System.out.println("readto: Used in PRINT mode to dictate how many lines to output. Default is 100 ");
		System.out.println("printLineNumbers: TRUE or FALSE. Used in PRINT mode to deteremine if line numbers are printed with lines. Default is false.");
		
		
		
	}

	


	public void run() {
		FileReader in = null;
	    try {
	    	establishConnection();
	    	String cleanLine, statement;
	    	int c;
	    	StringBuilder sb_statement = new StringBuilder();
	    	in = new FileReader(scriptFileName); 
	    	ArrayList<String> statements = new ArrayList<String>();
	    	ArrayList<String> headers = new ArrayList<String>();
	    	ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
	    	while((c = in.read()) != -1) {
	    		sb_statement.append((char) c);
	    		if (sb_statement.toString().endsWith(";")) {
	    			statement = sb_statement.toString();
		    		if (! useSemicolon) statement = statement.replace(';', '\0');
			    	if (statement.startsWith("INSERT") || statement.startsWith("insert")) {
			    		statement = statement.trim().replaceAll(removeTheseCharsRegex,"");
			    		statements.add(statement);
		    			//out.println(this.execPreparedStatment(statement));
			    	}
		    		else if (statement.startsWith("SELECT")||statement.startsWith("select")) {
		    			out.println(statement);
		    			result = this.execQuery(sqlDateFormat, statement);
		    			for(String heading :result.get(0).keySet()) {
		    				headers.add(heading);
		    				out.print(heading);
		    				out.print(this.outDelimiter);
		    			}
	    				out.println();
		    			for(HashMap<String,String> record:result) {
		    				for(String heading :result.get(0).keySet()) {
		    					String value = record.get(heading);
			    				out.print(value.replaceAll(this.outDelimiter, ""));
			    				out.print(this.outDelimiter);
		    				}
		    				out.println();
		    			}
			    	} 
			    	sb_statement.delete(0, sb_statement.length());
	    		}
	    	}
			if (!statements.isEmpty()) {
	    		out.println("INSERTING");
				if (doCommit) statements.add("commit");
				out.println(this.execBatchStatements( statements, true));
				out.println("FINISHED");
			}
	    	this.closeConnection();
	    }
	    catch (Exception exc) {
	    	exc.printStackTrace(out);
	    }
	}


	/**
	 * Executes a list of statements.
	 * @param dataSourceName
	 * @param inserts
	 * @param ignoreErrors
	 * @return
	 * @throws Exception
	 */
	public String execBatchStatements(ArrayList<String> inserts,boolean forceClose) throws Exception {
		if (forceClose) {
			this.closeConnection();
		}
		this.establishConnection();
		Statement statement = connection.createStatement();
		Iterator<String> itr = inserts.iterator();
		StringBuilder results=new StringBuilder("");
		while(itr.hasNext()) {
			String insert = itr.next();
			try {
				statement.addBatch(insert);
			}
			catch (Exception exc) {
					throw exc;
			}
		}
		try {
			int[] ires = statement.executeBatch();
			SQLWarning w = statement.getWarnings();
			while( w!=null){
				results.append(w.getMessage());
				results.append("\n");
				w=w.getNextWarning();
			}
//			results.append("\nBatch Affected Files...\n");
//			for (int j=0; j < ires.length; j++) {
//				results.append(ires[j]);
//				results.append(": ");
//				results.append(inserts.get(j));
//				results.append("\n");
//			}
		}
		catch (Exception exc) {
			itr = inserts.iterator();
			out.println("The following batch has an error:");
			while(itr.hasNext()) {
				out.println(itr.next());
			}
			exc.printStackTrace(out);
		}
		finally {
			statement.close();
			statement = null;
			this.closeConnection();
		}
		return(results.toString());
	}


	
	/**
	 * Execute full SQL statements.
	 * @param dataSourceName
	 * @param sqlStatment
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String,String>> execQuery(String dateFormat,String sqlStatment) throws Exception {
		ArrayList<HashMap<String,String>> results = new ArrayList<HashMap<String,String>>();
		SimpleDateFormat sdf = null;
		if (dateFormat != null && "".equals(dateFormat)) 
			sdf = new SimpleDateFormat(dateFormat);
		else 
			sdf = new SimpleDateFormat(getSqlDateFormat());
		
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
	
	
	
	
	public String getScriptFileName() {
		return scriptFileName;
	}



	public void setScriptFileName(String scriptFileName) {
		this.scriptFileName = scriptFileName;
	}



	public int getLinesPerRead() {
		return linesPerRead;
	}



	public void setLinesPerRead(int linesPerRead) {
		this.linesPerRead = linesPerRead;
	}



	public String getJdbcUrl() {
		return jdbcUrl;
	}



	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}



	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public String getPassword() {
		return password;
	}



	public void setPassword(String password) {
		this.password = password;
	}



	public String getDriver() {
		return driver;
	}



	public void setDriver(String driver) {
		this.driver = driver;
	}



	public int getDbtype() {
		return dbtype;
	}



	public void setDbtype(int dbtype) {
		this.dbtype = dbtype;
	}



	public String getPropsFilename() {
		return propsFilename;
	}



	public void setPropsFilename(String propsFilename) {
		this.propsFilename = propsFilename;
	}



	public boolean isDoCommit() {
		return doCommit;
	}



	public void setDoCommit(boolean doCommit) {
		this.doCommit = doCommit;
	}



	public String getSqlDateFormat() {
		return sqlDateFormat;
	}



	public void setSqlDateFormat(String sqlDateFormat) {
		this.sqlDateFormat = sqlDateFormat;
	}


}
