package kobler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.StringTokenizer;

public class RemoveAWord {

	public static void main(String[] args) {
		
		System.out.println("Remove-A-Word by Jonathan Kobler, 2018");
		try {
			String mode = args[0];
			String inFilename = args[1];
			String outFilename = args[2];
			String target = args[3];
			String replacement="";
			String delimiter = "|";
			
			for (int i=4;i<args.length; i++) {
				if (args[i].equals("-r")) 
					replacement=args[++i];
				else if (args[i].equals("-d")) 
					delimiter=args[++i];
				
			}
			System.out.println(" inFilename:"+inFilename);
			System.out.println("outFilename:"+outFilename);
			System.out.println("     target:"+target);
			System.out.println("replacement:"+replacement);
			System.out.println("  delimiter:"+delimiter);

			if ("all".equalsIgnoreCase(mode))
				RemoveAWord.cleanThisShit(inFilename, outFilename, delimiter, target, replacement);
			else 
				RemoveAWord.cleanExactWord(inFilename, outFilename, delimiter, target, replacement);
			System.out.println("Done.");
		
		}
		catch (Exception exc) {
			System.out.println("RemoveAWord all|exact \"infilename\" \"outfilename\" \"word to remove\" [-r \"replacement\"] [-d \"delimiter\"]  ");
			System.out.println(exc.getMessage());
			exc.printStackTrace();
		}
		
		
		
	}
	
	
	
	public static void cleanThisShit(String inFilename, String outFilename, String delimiter, String target, String replacement) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(inFilename));
		PrintStream out = new PrintStream(new File(outFilename));
		
		String line;
		
		while ((line = in.readLine()) != null) {
			out.println(line.replaceAll(target, replacement));
		}
		in.close();
		out.flush();
		out.close();
	}
	
	public static void cleanExactWord(String inFilename, String outFilename, String delimiter, String target, String replacement) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(inFilename));
		PrintStream out = new PrintStream(new File(outFilename));
		StringTokenizer st;
		String line, entry;
		while ((line = in.readLine()) != null) {
			st = new StringTokenizer(line,delimiter);
			while(st.hasMoreTokens()) {
				entry = st.nextToken();
				out.print(entry.replaceAll("^"+target+"$", replacement));
				if (st.hasMoreTokens() ) out.print(delimiter);
			}
			out.println();			
		}
		in.close();
		out.flush();
		out.close();
		
		
		
		
	}

}
