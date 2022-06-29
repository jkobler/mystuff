package kobler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KleenIt {


	String inFileName, outFileName, removeRegex, replaceWith;
	char delimiter = ' ';
	int wordCount = 200;
	boolean rewrite = false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inFileName=".", outFileName=null, removeRegex="", replaceWith="";
		char delimiter= ' ' ;
		int wordCount=200;
		KleenIt k;
		boolean rewrite = false;
		
		try{
			
			for(int i=0;i<args.length;i++) {
				
				if (args[i].equalsIgnoreCase("-in") )
					inFileName = args[++i];
				else if (args[i].equalsIgnoreCase("-out") )
					outFileName = args[++i];
				else if (args[i].equalsIgnoreCase("-block") )
					wordCount = Integer.valueOf(args[++i]);
				else if (args[i].equalsIgnoreCase("-delimiter") )
					delimiter = args[++i].charAt(0);
				else if (args[i].equalsIgnoreCase("-search") )
					removeRegex = args[++i];
				else if (args[i].equalsIgnoreCase("-replace") )
					replaceWith = args[++i];
				else if (args[i].equalsIgnoreCase("-rewrite") )
					rewrite = true;
				else if (args[i].equalsIgnoreCase("-help")||args[i].equalsIgnoreCase("-?") ) {
					help();
					System.exit(0);
				}
				
			
			}
			
			
			k = new KleenIt(inFileName,outFileName,wordCount,delimiter,removeRegex,replaceWith,rewrite);
			k.run();
		
		}
		catch(Exception exc) {
			exc.printStackTrace();
			help();
			System.exit(1);
		}
	}
	
	public static void help() {
		System.out.println("KleenIt -in \"in file or directory\" -out \"out file or directory\" -block wordCount -delimiter \"delimiter\" -search \"removeRegex\" -replace \"replaceWith\" -rewrite");
		System.out.println("-in \"in file or directory\" required");
		System.out.println("-out \"out file or directory\" required");
		System.out.println("-block wordCount number, 1+ default is 200");
		System.out.println("-delimiter \"delimiter\" What defines your words... ex: \'>\' ends a tag. Default is \' \'");
		System.out.println("-search \"removeRegex\" required, what you want searched for or removed. ");
		System.out.println("-replace \"replaceWith\" ");
		System.out.println("-rewrite will rewrite the file regardless of whether a match happens in replace mode (default is false)");
	}

	public KleenIt(String inFileName, String outFileName, int wordCount, char delimiter, String removeRegex, String replaceWith, boolean rewrite) {
		super();
		this.inFileName = inFileName;
		this.outFileName = outFileName;
		this.removeRegex = removeRegex;
		this.wordCount = wordCount;
		this.replaceWith = replaceWith;
		this.delimiter = delimiter;
		this.rewrite = rewrite;
	}
	
	void run() throws Exception {
		File in = new File(this.inFileName);
		File out = new File(outFileName);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		System.out.println("Starting at " + sdf.format(new Date()));
		System.out.println(in.getAbsolutePath());
		System.out.println(out.getAbsolutePath());
		String[] fileNames;
		StringBuilder sbOfFileList = new StringBuilder();
		if (in.isDirectory()) { 
			fileNames = in.list();
			System.out.println("In directory contains "+fileNames.length+" files.");
		}
		else {
			fileNames = new String[1];
			fileNames[0] = inFileName;
			System.out.println("Processing "+inFileName);
		}
			
		File inFile;
		PrintWriter outPrint=  null;
		
		if (!out.isDirectory()) 
			outPrint = new PrintWriter(out);
		
		for (int i=0; i < fileNames.length; i++) {
			if (in.isDirectory())
				inFile =  new File(in,fileNames[i]);
			else
				inFile = in;
			
			if (out.isDirectory()) {
				try {
					outPrint.flush();
					outPrint.close();
				}
				catch (Exception c) {/*don't care*/}
				outPrint = new PrintWriter(new File(out,fileNames[i])); 
			}
			
			
			
			if (!(replaceWith == null || "".equals(replaceWith))) {
				System.out.println("Search and replace :"+fileNames[i]);
				if (readAndReplace(new BufferedReader(new FileReader(inFile)), outPrint)) {
					sbOfFileList.append(fileNames[i]).append("\n");
					System.out.println("match:"+fileNames[i]);
				}
			}
			else {
				System.out.println("Find :"+fileNames[i]);
				//outPrint.println(fileNames[i]);
				if (find(new BufferedReader(new FileReader(inFile)), outPrint)) {
					outPrint.println("match:"+fileNames[i]);
				}
			}
		}
		try {
			outPrint.flush();
			outPrint.close();
		}
		catch (Exception c) {/*don't care*/}
		
		if (sbOfFileList.length() > 0) {
			//only for replace.
		
			outPrint = new PrintWriter(new File(out.getParent(),sdf.format(new Date())+"-listOfFiles.txt")); 
			outPrint.println(sbOfFileList.toString());
			try {
				outPrint.flush();
				outPrint.close();
			}
			catch (Exception c) {/*don't care*/}
			sbOfFileList = null;
		}
		System.out.println("Finished at " + sdf.format(new Date()));
	}
	
	void println(String message) throws Exception {
		
		
		
	}
		
		
	
	boolean readAndReplace(BufferedReader in, PrintWriter out) throws Exception {	
		StringBuilder row = new StringBuilder();
		StringBuilder data = new StringBuilder();
		boolean didFind = false;
		long rowCount=0;
		char c;
		int cint, colIdx=1;
		while((cint = in.read()) > 0) {
			c = (char) cint;
    /*
     * This next bit ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     * 
     */
			if ((c == 0x9) ||
                (c == 0xA) ||
                (c == 0xD) ||
                ((c >= 0x20) && (c <= 0xD7FF)) ||
                ((c >= 0xE000) && (c <= 0xFFFD)) ||
                ((c >= 0x10000) && (c <= 0x10FFFF))) {
            	row.append(c);
            }
            else {
            	continue;
            }
			if (c == delimiter) {
				colIdx++;
				if (colIdx >= wordCount) {
					data.append(row.toString().replaceAll(removeRegex, replaceWith));
					if (row.toString().matches(".*"+removeRegex+".*")) {
						didFind = true;
					}
					rowCount++;
					row = new StringBuilder();
					colIdx = 1;
					
				}
			}
			
		} //while
	
		//the last bit
		data.append(row.toString().replaceAll(removeRegex, replaceWith));
		if (row.toString().matches(".*"+removeRegex+".*")) {
			didFind = true;
		}
		
		rowCount++;
		row = new StringBuilder();
		colIdx = 1;
		if (didFind || rewrite) {

			out.print(data.toString());
		}
		else {
			out.close();
		}

	
		System.out.println(rowCount);
		
		in.close();
		return(didFind);
	}
	
	boolean find(BufferedReader in, PrintWriter out) throws Exception {	
		StringBuilder row = new StringBuilder();
		boolean didFind = false;
		long rowCount=0;
		char c;
		int cint, colIdx=1, wordIdx = 1;
		while((cint = in.read()) > 0) {
			c = (char) cint;
			row.append(c);
			colIdx++;
			if (c == delimiter) { 
				if (wordIdx >= wordCount) {
					if (row.toString().matches(removeRegex)) {
						out.println(colIdx + ","+rowCount+":" + row.toString() + "\n");
						System.out.println(colIdx + ","+rowCount+":Found");
						didFind = true;
					}
					wordIdx = 0;
					row = new StringBuilder();
				}
				//System.out.println(colIdx + ","+rowCount);
				wordIdx++;
			}
			if (c == '\n') {
				colIdx = 1;
				rowCount++;
			}
		} //while
		if (row.toString().matches(removeRegex)) {
			out.println(colIdx + ","+rowCount+":" + row.toString() + "\n");
		}
	
		in.close();
		return(didFind);
	}
}
