package kobler;

import java.io.StringWriter;
import java.util.Scanner;

public class BlackQuartz  {
	public String key = "sphinxofblackqu rtz.j,dgemyv?w1234567890!";
	public String ord = "abcdefghijklmnopqrstuvwxyz 1234567890.,?!";
	
	/**
	 * [encrypted phrase|unencrypted phrase]
	 * -d DECRYPT|-e ENCRYPT
	 * [start] 0-9
	 * [increment] 0-9
	 * 
	 * -? show help
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String phrase,results;
		int start, increment;
		String mode;
		Scanner sc = new Scanner(System.in);
		try {
			if (args.length==1 && args[0].equals("-?")) {
				BlackQuartz.showHelp();
			}
			if (args.length==4) {
				phrase = args[0].toLowerCase();
				mode = args[1];
				start = Integer.parseInt(args[2]);
				increment = Integer.parseInt(args[3]);
			}
			else {
				System.out.println("Phrase:");
				phrase = sc.nextLine().toLowerCase();
				System.out.println("Mode (-d or -e):");
				mode = sc.nextLine();
				System.out.println("Start point (integer between 0-9):");
				start = sc.nextInt();
				System.out.println("Increment by (integer between 0-9):");
				increment = sc.nextInt();
				sc.close();
			}
			BlackQuartz bq = new BlackQuartz();
					
			if (mode.equalsIgnoreCase("-d")) 
				results = bq.decrypt(phrase, start, increment);
			else 
				results = bq.encrypt(phrase, start, increment);
			
			System.out.println(results);
			
		}
		catch (Exception exc) {
			System.out.println(exc.getMessage());
			BlackQuartz.showHelp();
		
		}
		
	}
	
	public BlackQuartz() {
		super();
	}
	
	public static void showHelp() {
		System.out.println(" [encrypted phrase|unencrypted phrase] [-d|-e] [start] [increment] ");
		System.out.println(" 	 -d DECRYPT"
				+ "      -e ENCRYPT\n" + 
				  "      start      0-9\n" + 
				  "      increment  0-9\n");
		
	}
	
	public String encrypt(String unencrypted, int start_i, int increment) {
		StringWriter encrypted = new StringWriter();
		char uc;
		int uci, eci;
		//System.out.println("uc uci os eci ec");
		for (int i=0;i<unencrypted.length();i++) {
			uc = unencrypted.charAt(i);
			//System.out.print(uc+" ");
			uci = ord.indexOf(uc);
			//System.out.print(uci+" ");
			//System.out.print((start_i)%10+" ");
			eci = (uci+((start_i)%10))%(key.length());
			start_i+=increment;
			encrypted.write(key.charAt(eci));
			//System.out.print(eci+" ");
			//System.out.println(key.charAt(eci));
		}
		return(encrypted.toString().toUpperCase());
	}

	public String decrypt(String encrypted, int start_i, int increment) {
		StringWriter unencrypted = new StringWriter();
		char ec;
		int uci, eci;
		//System.out.println("ec eci os uci uc");
		for (int i=0;i<encrypted.length();i++) {
			ec = encrypted.charAt(i);
			//System.out.print(ec+" ");
			eci = key.indexOf(ec);
			//System.out.print(eci+" ");
			//System.out.print((start_i)%10+" ");
			if (eci >= (start_i)%10) 
				uci = eci-(start_i)%10;
			else 
				uci = (ord.length()+eci)-(start_i)%10;
			//System.out.print(uci + " ");
				
			start_i+=increment;
			unencrypted.write(ord.charAt(uci));
			//System.out.println(ord.charAt(uci));
		}
		return(unencrypted.toString().toUpperCase());
	}
	
	
}