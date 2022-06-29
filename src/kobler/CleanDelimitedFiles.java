package kobler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;

public class CleanDelimitedFiles {
	String inFileName, outFileName, removeRegex;
	char delimiter;
	int colCount;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inFileName, outFileName, removeRegex="";
		char delimiter;
		int colCount;
		CleanDelimitedFiles cdf;
		try{
			inFileName = args[0];
			outFileName = args[1];
			delimiter = args[2].charAt(0);
			colCount = Integer.valueOf(args[3]);
			if (args.length > 4) removeRegex = args[4];
			
			cdf = new CleanDelimitedFiles(inFileName,outFileName,delimiter,colCount,removeRegex);
			cdf.run();
		
		}
		catch(Exception exc) {
			exc.printStackTrace();
			System.out.println("CleanDelimitedFiles \"in file\" \"out file\" \"delimiter\" \"col count\" [remove regex]");
			System.out.println("If [remove regex] isn't there then it's clean whitespace characters in column values.");
			
		}
	}

	public CleanDelimitedFiles(String inFileName, String outFileName,
			char delimiter, int colCount, String removeRegex) {
		super();
		this.inFileName = inFileName;
		this.outFileName = outFileName;
		this.delimiter = delimiter;
		this.removeRegex = removeRegex;
		this.colCount = colCount;
	}
	
	void run() throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(this.inFileName));
		PrintStream out = new PrintStream(new File(outFileName));
		
		StringBuilder row = new StringBuilder();
		int colIdx = 1;
		long rowCount=0;
		boolean lastCol = false;
		char c;
		int cint;
		while((cint = in.read()) > 0) {
			c = (char) cint;
			if (c == delimiter) {
				colIdx++;
				lastCol = colIdx >= colCount;
				//System.out.println(colIdx);
//				--out.write(c);
				row.append(c);
			}
			else if (c == '\n') {
				if (lastCol) {
//					--out.write(c);
					if(!removeRegex.equals("")) {
						out.println(row.toString().replaceAll(removeRegex, ""));
					}
					else {
						out.println(row);
					}
					
					rowCount++;
					row = new StringBuilder();
					lastCol = false;
					colIdx = 1;
				}
				else {
//					--out.write(';');
//					--out.write(' ');
					row.append("; ");
					System.out.println(rowCount+":"+colIdx);
				}
			}
			else {
				switch (c) {
				case '\r':
					break;
				case '\t':
					break;
				default:
//					--out.write(c);
					row.append(c);
				}
			}
			
			
		}
		System.out.println("And we\'re done.");
		
		System.out.println(rowCount);
		
		in.close();
		out.flush();
		out.close();
		
	}

}
