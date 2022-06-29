package kobler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class TestDataGenerator {

	public static void main(String[] args) {
		
		
		try {
			String outputFilename = args[0];
			
			long rows = Long.parseLong(args[1]);
			
			int columns = Integer.parseInt(args[2]);
			TestDataGenerator.generate(outputFilename, rows, columns);
		}
		catch(Exception exc) {
			exc.printStackTrace();
			System.out.println("TestDataGenerator \"outputFilename\" rows columns ");
		}

	}
	
	
	
	
	
	static void generate(String outputFilename, long rows, int columns) throws Exception {
		Random rngesus = new Random();
		String[] rint = {"0","1","2","3","4","5","6","7","8","9"};
		String[] rfloat = {".","0","1","2","3","4","5","6","7","8","9"};
		String[] alphanum = {"0","1","2","3","4","5","6","7","8","9"," ","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
		String[] alpha = {"_","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
		PrintStream out = new PrintStream(outputFilename);
		ArrayList<Integer> columnWidths = new ArrayList<Integer>();
		int charlen;
		for(int i=0; i<columns;i++ ) {
			charlen = rngesus.nextInt(10)+5;
			for(int j=0;j<charlen;j++ ) {
				out.print(alpha[rngesus.nextInt(alpha.length)]);
			}
			out.print("|");
			if (i == 4||i == 13)
				columnWidths.add(rngesus.nextInt(1000)+1);
			else 
				columnWidths.add(rngesus.nextInt(199)+1);
				
		}
		
		out.println("");
		
		
		
		for(long r=0; r<rows;r++ ) {		
			for(int i=0; i<columns;i++ ) {
				if (i == 0 || i == 2 || i == 5 || i == 14) {
					charlen = rngesus.nextInt(3)+3;
					for(int j=0;j<charlen;j++ ) {
						out.print(rfloat[rngesus.nextInt(rfloat.length)]);
					}
				
				}
				else if (i == 3 || i ==6 || i == 12 || i == 15) {
					charlen = rngesus.nextInt(3)+1;
					for(int j=0;j<charlen;j++ ) {
						out.print(rint[rngesus.nextInt(rint.length)]);
					}
				
				}
				else {
					charlen = rngesus.nextInt(columnWidths.get(i));
					for(int j=0;j<charlen;j++ ) {
						out.print(alphanum[rngesus.nextInt(alpha.length)]);
					}
				}
				out.print("|");
			}
			out.println("");
		}
		
		out.flush();
		out.close();
		
	}
}
