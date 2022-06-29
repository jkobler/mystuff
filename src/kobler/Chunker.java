package kobler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;



public class Chunker {

	public static void main(String[] args) {
		ArrayList<String> filenames = new ArrayList<String>();
		
		try {
			String mode = args[0];
			String listOfFiles = args[1];
			String password = null;
			String filepath = System.getProperty("user.dir");
			String filename = "temp.zip";
			Chunker ch = new Chunker();
			if ("s".equalsIgnoreCase(mode)) {
				int maxSizeInMb = 1024;
				String fileNamePrefix = args[2];
				for(int i = 3; i<args.length; i++) {
					if (args[i++].equalsIgnoreCase("-p")) 
						password = args[i];
					else if (args[i++].equalsIgnoreCase("-s")) 
						maxSizeInMb = Integer.parseInt(args[i]);
					else if (args[i++].equalsIgnoreCase("-o")) 
						filepath = args[i];
				}
					StringTokenizer st = new StringTokenizer(listOfFiles, ",");
					while (st.hasMoreTokens()) {
						filenames.add(st.nextToken());
				}
				
				ch.split(filepath, fileNamePrefix, filenames, password, maxSizeInMb);
			}
			else {
				for(int i = 2; i<args.length; i++) {
					if (args[i++].equalsIgnoreCase("-p")) 
						password = args[i];
					else if (args[i++].equalsIgnoreCase("-o")) 
						filepath = args[i];
					else if (args[i++].equalsIgnoreCase("-z")) 
						filename = args[i];
				}
				
				ch.join(filename, filenames, filepath, password);
			}
		}
		catch (Exception exc) {
			exc.printStackTrace();
			showHelp();
		}

	}
	
	
	
	public static void showHelp() {
		System.out.println("java -jar Chunker.jar s \"comma delimited list of files|directory\" \"filenamePrefix\" [-o outputDir] [-p \\\"password\\\"] [-s chunk size in mb]"  );
		System.out.println("java -jar Chunker.jar j \"comma delimited list of files|directory\" [-z \"zip filename out\"][-o outputDir] [-p \"password\"]   ");
	}
	
	
	
	//is encoding then splitting
		//zip
		//base64
	    //seperate
	
	
	//is combining then decoding.
	  	//combine
		//decode from base64
		//unzip
	
	
	
	public void split(String filepath, String fileNamePrefix, ArrayList<String> filenames,String password, int maxSizeInMb) throws Exception {
		StringBuffer sb = new StringBuffer(fileNamePrefix);
		sb.append(".zip");
		byte[] buf = new byte[1024]; 
//		String encryptedAllFile; 
//		byte[] allBytes;
//		byte[] encryptedAllFile;
		File newFile = new File(filepath,sb.toString());
		String filename, noPathFilename;
		Base64 b64 = new Base64();
//		FileOutputStream bout;
		FileInputStream in;
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(newFile,true)); 
		Iterator<String> fileIter = filenames.iterator();
		System.out.println("Zipping files...");
		while(fileIter.hasNext()) {
			filename = fileIter.next();
			File addFile = new File(filename);
			if (addFile.isDirectory()) continue;
			noPathFilename = addFile.getName();
			System.out.println("...Adding "+filename);
			Path path = Paths.get(addFile.getParent(), addFile.getName());
			if (null != password && !password.equals("")) {
//				noPathFilename = addFile.getName()+".enc";
//				filename = filename+".enc";
				out.putNextEntry(new ZipEntry(noPathFilename)); 
//				bout = new FileOutputStream(filename, false);
				out.write(this.doPBEWithMD5AndDES(password, Files.readAllBytes(path), Cipher.ENCRYPT_MODE));
//				bout.flush();
//				bout.close();				
//				addFile = new File(filename);
	
			}
			else {
				in = new FileInputStream(addFile);
				out.write(Files.readAllBytes(path));
				in.close(); 
			}
			out.closeEntry(); 

			// Transfer bytes from the file to the ZIP file 
//			int len; 
//			while ((len = in.read(buf)) > 0) { 
//				out.write(buf, 0, len); 
//				System.out.print("   .");
//			} // Complete the entry out.closeEntry(); in.close(); 		
			System.out.println("");
		}		
		out.close();
		System.out.println("Finished compressing file.\n");

		filename = b64.encodeFile(newFile.getAbsolutePath());
		splitTheFile(filename, filepath, fileNamePrefix, maxSizeInMb);
		
		System.out.println("Finished.\n");
		
		
	}
	
	
	
	
	public void join(String filename, ArrayList<String> filenames, String unzipPath, String password ) throws Exception {
		Base64 b64 = new Base64();
		String zipFilename;
		StringBuffer sb_filepath;
		
		//assemble
		
		assembleTheFile(filename,filenames);
		
		//decode
		
		zipFilename = b64.decodeFile(filename, true);
		
		//unzip
		System.out.println("Unzipping "+zipFilename);
		ZipInputStream in = new ZipInputStream(new FileInputStream(zipFilename));
		ZipEntry ze = null;
		ByteArrayOutputStream bout = null;
		FileOutputStream fout = null;
		
		byte[] b = new byte[4096];
		int readSize=0;
		File unzipDir = new File(unzipPath);
		if (! unzipDir.exists()) unzipDir.mkdir();
		while ((ze = in.getNextEntry()) != null ) {
			if (ze.isDirectory()) continue;
			sb_filepath = new StringBuffer(unzipPath);
			sb_filepath.append(ze.getName());
			readSize=0;

			System.out.print("..."+sb_filepath.toString());
			fout = new FileOutputStream(sb_filepath.toString());
			bout = new ByteArrayOutputStream();
			while ((readSize =  in.read(b)) > -1) {
				bout.write(b,0,readSize);
				System.out.print(".");
			}
			System.out.println("");

			if (password != null && !"".equals(password)) {
				fout.write(this.doPBEWithMD5AndDES(password, bout.toByteArray(), Cipher.DECRYPT_MODE));
			}
			else {
				fout.write(bout.toByteArray());
			}
		
			fout.flush();
			fout.close();
		
		}
		System.out.println("Finished");
	}
	
	
	public void assembleTheFile(String filename, ArrayList<String> filenames) throws Exception {
		FileInputStream in;
		FileOutputStream out = new FileOutputStream(filename);
		String infile;
		byte[] buf = new byte[1024]; 
		int amountRead = 0;
		
		Iterator<String> fileIter = filenames.iterator();
		System.out.println("Assembling files to "+filename+"...");
		while(fileIter.hasNext()) {
			infile = fileIter.next();
			System.out.println("...Adding "+infile);
			File addFile = new File(infile);
			in = new FileInputStream(infile); 
			
			while ((amountRead = in.read(buf)) > 0) {
				out.write(buf, 0, amountRead);
			}
			out.flush();
			in.close();
		}
		out.flush();
		out.close();
		
		System.out.println("done");
	}
	
	public void splitTheFile(String filename,String filepath, String fileNamePrefix, int maxSizeInMb) throws Exception {
		byte[] buf = new byte[1024]; 
		int counter = 1;
		int amountRead = 0;
		StringBuffer sb = new StringBuffer(filepath);
		File inFile = new File(filename);
		FileInputStream in = new FileInputStream(inFile);
		String outFilePrefix;
		System.out.print("Splitting files into ");
		System.out.print(filepath);
		System.out.println("...");
		
		if (! filepath.endsWith(File.pathSeparator)) sb.append(File.pathSeparator);
		sb.append(fileNamePrefix);
		sb.append("-");
		outFilePrefix = sb.toString();
		sb = new StringBuffer(outFilePrefix);
		sb.append(this.leftPad(counter, 4, true));
		sb.append(".rpt");
		
		System.out.print("...");
		System.out.println(sb.toString());
		FileOutputStream out = new FileOutputStream(sb.toString());
		long maxSizeInB = maxSizeInMb*1048576;
		long ctrSizeInB = 0L; 
		
		while ((amountRead = in.read(buf)) > 0) {
			ctrSizeInB += amountRead;
			if (ctrSizeInB > maxSizeInB) {
				out.flush();
				out.close();
				
				sb = new StringBuffer(outFilePrefix);
				sb.append(this.leftPad(++counter, 4, true));
				sb.append(".rpt");
				System.out.print("...");
				System.out.println(sb.toString());
				out = new FileOutputStream(sb.toString());
				ctrSizeInB = amountRead; 
			}
			out.write(buf);
		}
		
		try { 
			out.flush();
			out.close();
		}
		catch (Exception exc) {
			//don't care
		}
		
	}

	
	public String leftPad(int number,int spaces,boolean useZeros) {
		StringBuffer sb = new StringBuffer();
		
		String numberToStr = Integer.toString(number);
		
		for(int i=0; i< spaces-numberToStr.length(); i++) {
			if(useZeros) sb.append("0");
			else sb.append(" ");
		}
		sb.append(numberToStr);
		return(sb.toString());		
	}
	
	
	public byte[] doPBEWithMD5AndDES(String p, byte[] s, int opmode) throws Exception {
		//StringBuffer endresult = new StringBuffer();
	    byte[] salt = {
	        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
	        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
	    };
	    int count = 20;

	    PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
	    PBEKeySpec pbeKeySpec = new PBEKeySpec(p.toCharArray());
	    SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
	    SecretKey key = keyFac.generateSecret(pbeKeySpec);
	    
		Cipher cP = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
		cP.init(opmode, key, pbeParamSpec);

		return(cP.doFinal(s));
	}

	
	public class Base64 {
		
		Hashtable base64Code;
		int encodeSize = 0;
		int decodeSize = 0;
//		public PrintWriter FDEBUG = null;
		public PrintStream FDEBUG = System.out;
		public boolean DEBUG = false;

		public Base64() {
			base64Code = new Hashtable();
			base64Code.put("000000","A");
			base64Code.put("000001","B");
			base64Code.put("000010","C");
			base64Code.put("000011","D");
			base64Code.put("000100","E");
			base64Code.put("000101","F");
			base64Code.put("000110","G");
			base64Code.put("000111","H");
			base64Code.put("001000","I");
			base64Code.put("001001","J");
			base64Code.put("001010","K");
			base64Code.put("001011","L");
			base64Code.put("001100","M");
			base64Code.put("001101","N");
			base64Code.put("001110","O");
			base64Code.put("001111","P");
			base64Code.put("010000","Q");
			base64Code.put("010001","R");
			base64Code.put("010010","S");
			base64Code.put("010011","T");
			base64Code.put("010100","U");
			base64Code.put("010101","V");
			base64Code.put("010110","W");
			base64Code.put("010111","X");
			base64Code.put("011000","Y");
			base64Code.put("011001","Z");
			base64Code.put("011010","a");
			base64Code.put("011011","b");
			base64Code.put("011100","c");
			base64Code.put("011101","d");
			base64Code.put("011110","e");
			base64Code.put("011111","f");
			base64Code.put("100000","g");
			base64Code.put("100001","h");
			base64Code.put("100010","i");
			base64Code.put("100011","j");
			base64Code.put("100100","k");
			base64Code.put("100101","l");
			base64Code.put("100110","m");
			base64Code.put("100111","n");
			base64Code.put("101000","o");
			base64Code.put("101001","p");
			base64Code.put("101010","q");
			base64Code.put("101011","r");
			base64Code.put("101100","s");
			base64Code.put("101101","t");
			base64Code.put("101110","u");
			base64Code.put("101111","v");
			base64Code.put("110000","w");
			base64Code.put("110001","x");
			base64Code.put("110010","y");
			base64Code.put("110011","z");
			base64Code.put("110100","0");
			base64Code.put("110101","1");
			base64Code.put("110110","2");
			base64Code.put("110111","3");
			base64Code.put("111000","4");
			base64Code.put("111001","5");
			base64Code.put("111010","6");
			base64Code.put("111011","7");
			base64Code.put("111100","8");
			base64Code.put("111101","9");
			base64Code.put("111110","+");
			base64Code.put("111111","/");
			base64Code.put("PPPPPP","=");
			base64Code.put("A","000000");
			base64Code.put("B","000001");
			base64Code.put("C","000010");
			base64Code.put("D","000011");
			base64Code.put("E","000100");
			base64Code.put("F","000101");
			base64Code.put("G","000110");
			base64Code.put("H","000111");
			base64Code.put("I","001000");
			base64Code.put("J","001001");
			base64Code.put("K","001010");
			base64Code.put("L","001011");
			base64Code.put("M","001100");
			base64Code.put("N","001101");
			base64Code.put("O","001110");
			base64Code.put("P","001111");
			base64Code.put("Q","010000");
			base64Code.put("R","010001");
			base64Code.put("S","010010");
			base64Code.put("T","010011");
			base64Code.put("U","010100");
			base64Code.put("V","010101");
			base64Code.put("W","010110");
			base64Code.put("X","010111");
			base64Code.put("Y","011000");
			base64Code.put("Z","011001");
			base64Code.put("a","011010");
			base64Code.put("b","011011");
			base64Code.put("c","011100");
			base64Code.put("d","011101");
			base64Code.put("e","011110");
			base64Code.put("f","011111");
			base64Code.put("g","100000");
			base64Code.put("h","100001");
			base64Code.put("i","100010");
			base64Code.put("j","100011");
			base64Code.put("k","100100");
			base64Code.put("l","100101");
			base64Code.put("m","100110");
			base64Code.put("n","100111");
			base64Code.put("o","101000");
			base64Code.put("p","101001");
			base64Code.put("q","101010");
			base64Code.put("r","101011");
			base64Code.put("s","101100");
			base64Code.put("t","101101");
			base64Code.put("u","101110");
			base64Code.put("v","101111");
			base64Code.put("w","110000");
			base64Code.put("x","110001");
			base64Code.put("y","110010");
			base64Code.put("z","110011");
			base64Code.put("0","110100");
			base64Code.put("1","110101");
			base64Code.put("2","110110");
			base64Code.put("3","110111");
			base64Code.put("4","111000");
			base64Code.put("5","111001");
			base64Code.put("6","111010");
			base64Code.put("7","111011");
			base64Code.put("8","111100");
			base64Code.put("9","111101");
			base64Code.put("+","111110");
			base64Code.put("/","111111");
			base64Code.put("=","PPPPPP");
		}
			
		public int getEncodeSize() { return(encodeSize); }
		public int getDecodeSize() { return(decodeSize); }
/*
		public static void main(String[] args) 	{
			String str = "com.kobler.Base64 {-debug} {-e|-d} \"{string}\"";
			Base64 b = new Base64();
			int l = 0; 
			int i = 0;
			byte[] buffer;
			
			
			
			if (args[0].equals("-debug")) {
				b.DEBUG = true;
				System.out.println("DEBUG MODE");
				i++;
			}
			
			if (args[i].equals("-d")) {
				i++;
				l = (int) Math.round(args[i].length() * 1.33);
				buffer = b.decode(args[i],l);
				str = new String(buffer,0,b.decodeSize);
			}
			else if (args[i].equals("-e")) {
				i++;
				buffer = args[i].getBytes();
				l = buffer.length;
				str = b.encode(buffer,0,l);
			}
			
			System.out.println(str);
		}
	*/	

		public String encodeFile(String infile) throws IOException {
			DataOutputStream out = null;
			DataInputStream in = null;
			byte[] buffer = new byte[3];
			StringBuffer sb_temp = new StringBuffer(infile); 
			sb_temp.append(".base64");
			String filename = sb_temp.toString();
			in = new DataInputStream(new FileInputStream(infile));
			out = new DataOutputStream(new FileOutputStream(filename));
			StringBuffer sb_hold = new StringBuffer(); 
			String temp = "";
			int sy,len;
			int i=0;
			int charcount=0;
			int octetcount=0;
			int idx1,idx2;
			if (DEBUG) FDEBUG.println("base64: ");
			do {
				sb_temp = new StringBuffer();
				len = in.read(buffer,0,3);
				if (DEBUG) FDEBUG.println("len: "+len);
				
				for (i=0;i<len;i++) {
					sy = (int)buffer[i];
					if (Integer.toBinaryString(sy).length() < 8) {
						temp = padOctet(Integer.toBinaryString(sy));
					}
					else if (Integer.toBinaryString(sy).length() > 8) {
						temp = Integer.toBinaryString(sy).substring(Integer.toBinaryString(sy).length()-8);
					}
					else temp = Integer.toBinaryString(sy);
					sb_temp.append(temp); 
					if (DEBUG) FDEBUG.println(octetcount+":"+temp);
					octetcount++;
				}
				
				if (i == 1) sb_temp.append("0000PPPPPPPPPPPP");
				else if (i == 2) sb_temp.append("00PPPPPP");
					
				temp = sb_temp.toString();
				
				
				for (i=0;i<4;i++) {
					idx1 = i*6;
					if (idx1 > temp.length()) break;
					idx2 = (i+1)*6;
					if (idx2 > temp.length()) idx2 = temp.length();
					if (DEBUG) FDEBUG.println((String) base64Code.get(temp.substring(idx1,idx2)));
					out.writeBytes((String) base64Code.get(temp.substring(idx1,idx2)));
					if (charcount == 76) {
						out.writeBytes("\r\n");
						charcount = 0;
					}
					else charcount++;
				}
				if (DEBUG) FDEBUG.println();
			} while (len == 3);
			in.close();
			out.close();
			return(filename);
		}
		
		public String decodeFile(String infile,boolean debug) throws IOException {
			DEBUG = debug;
			if (debug) System.out.println("DEBUG MODE");
			return(decodeFile(infile,null));
		}
		public String decodeFile(String infile, String outfile,boolean debug) throws IOException {
			DEBUG = debug;
			if (debug) System.out.println("DEBUG MODE");
			return(decodeFile(infile,outfile));
		}
		public String decodeFile(String infile, String outfile) throws IOException {
			FileOutputStream out = null;
			DataInputStream in = null;
			String temp = "";
			if (outfile == null) outfile = infile.substring(0,infile.lastIndexOf("."));
			
			out = new FileOutputStream(outfile);
			in = new DataInputStream(new FileInputStream(infile));
			byte[] buffer = new byte[3];
			StringBuffer sb_temp;
			boolean go = true;
			int i=0;
			int k=0;
			char c = ' ';
			int idx1,idx2;
			if (DEBUG) FDEBUG.println("base64: ");
			do {
				sb_temp = new StringBuffer();
				idx1 = 0;
				for (i=0;i<4;i++) {
					try { 
						c = (char) in.readByte(); 
						
						if ((c == '\r')||(c == '\n')) i--;
						else {
							idx1++;
							if (DEBUG) FDEBUG.println(c);	
							sb_temp.append((String) base64Code.get(String.valueOf(c)));
						}
					}
					catch (EOFException e) { 
						if (DEBUG) FDEBUG.println(c);
						go = false;
						i++;
						break;				
					}
				}
				if (DEBUG) FDEBUG.println();
				if ((idx1 != 4)&&(idx1>0)) throw new IOException("File \'"+infile +"\' may be be truncated." + idx1);
				for (i=0;i<3&&go;i++) {
					temp = sb_temp.toString().substring((i*8),((i+1)*8));
					if (DEBUG) FDEBUG.println(k+":"+temp);
					k++;
					if (temp.indexOf("PP") == -1) {
						try { buffer[i] = (byte) Integer.parseInt(temp,2); }
						catch (NumberFormatException e) { 
							buffer[i] = 0;
							if (DEBUG) e.printStackTrace(FDEBUG);
						}
					}
					else {
						go = false;
						break;
					}
				}
				if (DEBUG) FDEBUG.println();
				out.write(buffer,0,i);
				sb_temp = new StringBuffer();
			} while (go);
			if (DEBUG) FDEBUG.println();
			in.close();
			out.close();
			return(outfile);
		}
			
		public String encode(byte buffer[],int off,int len) {
			int i,j,k;
			int l=0;
			int idx1,idx2;
			StringBuffer sb_temp;
			StringBuffer sb_hold = new StringBuffer(); 
			String temp = "";
			if (DEBUG) FDEBUG.println("base64: ");
			if (DEBUG) FDEBUG.println("len: "+len);
			j=1;
			int sy;
			sb_temp = new StringBuffer();
			for (i=off;i<len+off;i++) {
				sy = (int)buffer[i];
				if (Integer.toBinaryString(sy).length() < 8) {
					temp = padOctet(Integer.toBinaryString(sy));
				}
				else if (Integer.toBinaryString(sy).length() > 8) {
					temp = Integer.toBinaryString(sy).substring(Integer.toBinaryString(sy).length()-8);
				}
				else temp = Integer.toBinaryString(sy);
				sb_temp.append(temp); 
				if (DEBUG) FDEBUG.print(sy+"="+temp+"|");
				if (j==3) {
					temp = sb_temp.toString();
					if (DEBUG) FDEBUG.println();
					for (k=0;k<4;k++) {
						idx1 = k*6;
						if (idx1 > temp.length()) break;
						idx2 = (k+1)*6;
						if (idx2 > temp.length()) idx2 = temp.length();
						if (DEBUG) FDEBUG.print(temp.substring(idx1,idx2)+"|");
						sb_hold.append((String) base64Code.get(temp.substring(idx1,idx2)));
						if (++l == 76) {
							l=0;
							sb_hold.append("\r\n");
						}
					}

					j=1;
					sb_temp = new StringBuffer();
				}
				else j++;
			}
				
			if (j == 2) sb_temp.append("0000PPPPPPPPPPPP");
			else if (j == 3) sb_temp.append("00PPPPPP");
			temp = sb_temp.toString();
			if (DEBUG) FDEBUG.println(temp);
			for (k=0;k<4&&j>1;k++) {
				idx1 = k*6;
				if (idx1 > temp.length()) break;
				idx2 = (k+1)*6;
				if (idx2 > temp.length()) idx2 = temp.length();
				if (DEBUG) FDEBUG.print(temp.substring(idx1,idx2)+"|");
				sb_hold.append((String) base64Code.get(temp.substring(idx1,idx2)));
				if (++l == 76) {
					l=0;
					sb_hold.append("\r\n");
				}
			}
				
			encodeSize = sb_hold.toString().length();
			if (DEBUG) FDEBUG.println();
			return(sb_hold.toString());
		}
		public byte[] decode(String str) {
			int bufferSize = (int) (str.length()*.80);
			return(decode(str,bufferSize));
		}
		public byte[] decode(String str,int bufferSize) {
			int i,j,k;
			int l=0;
			StringBuffer sb_temp;
			byte buffer[] = new byte[bufferSize]; 
			String temp;
				
				
			j=1;
			sb_temp = new StringBuffer();
			for (i=0;(i<str.length())&&(l<bufferSize);i++) {
				if ((str.charAt(i) == '\n')||(str.charAt(i) == '\r')) continue;
				sb_temp.append((String) base64Code.get(str.substring(i,i+1))); 
				if (j==4) {
					for (k=0;k<sb_temp.length()/8;k++) {
						temp = sb_temp.toString().substring((k*8),((k+1)*8));
						int idx = temp.indexOf("PP"); 
						if (temp.indexOf("PP") == -1) {
							try { buffer[l++] = (byte) Integer.parseInt(temp,2); }
							catch (NumberFormatException e) {}
						}
						else break;
					}
					j=1;
					sb_temp = new StringBuffer();
				}
				else j++;
			}
			decodeSize = l;

			return(buffer);
		}
			
		private String padOctet(String in) {
			int i;
			char octet[] = new char[8];
			for (i=0;i<8;i++) {
				if (i < 8-in.length()) octet[i] = '0';
				else octet[i] = in.charAt(i-(8-in.length()));
			}
			return(new String(octet));
		}
		private String padSextet(String in) {
			int i;
			char sextet[] = new char[6];
			for (i=0;i<6;i++) {
				if (i < 6-in.length()) sextet[i] = '0';
				else sextet[i] = in.charAt(i-(6-in.length()));
			}
			return(new String(sextet));
		}
		public byte[] fourToThree(char[] c) {
			int i;
			byte[] b;
			if (c.length < 4) return(null);
			int b1 = convert(c[0]);
			int b2 = convert(c[1]);
			int b3 = convert(c[2]);
			int b4 = convert(c[3]);
			b1 =  (b1<<2)|(b2>>>4);
			if (b3 < 64) {
				b2= (b2<<4)|(b3>>>2);
				if (b4 < 64) {
					b3= (b3<<6)|b4;
					b = new byte[3];
					b[0] =(byte) b1;
					b[1] =(byte) b2;
					b[2] =(byte) b3;
				}
				else {
					b = new byte[2];
					b[0] =(byte) b1;
					b[1] =(byte) b2;
				}
			}
			else {
				b = new byte[1];
				b[0] =(byte) b1;
			}
			return(b);
		}

		public char[] threeToFour(byte[] b) {
			int i;
			byte rightSix = 63;
		
			char[] c = new char[4];
			if (b.length < 3) return(null);
			int b1 = b[0]>>>2;
//			int b2 = (b[0]<<6|b[1]>>>2)>>>2;
			int b2 = (b[0]<<4|b[1])&rightSix;
//			int b3 = (b[1]<<4|b[2]>>>4)>>>2;
			int b3 = (b[1]<<2|b[2]>>>6)&rightSix;
//			int b4 = (b[2]<<2)>>>2;
			int b4 = b[2]&rightSix;
			
			c[0] = convert((byte) b1);
			c[1] = convert((byte) b2);
			c[3] = convert((byte) b3);
			c[4] = convert((byte) b4);
			return(c);
		}
		public char convert(byte b) {
			char c;
			switch(b) {
			case 0: c = 'A'; break;
			case 1: c = 'B'; break;
			case 2: c = 'C'; break;
			case 3: c = 'D'; break;
			case 4: c = 'E'; break;
			case 5: c = 'F'; break;
			case 6: c = 'G'; break;
			case 7: c = 'H'; break;
			case 8: c = 'I'; break;
			case 9: c = 'J'; break;
			case 10: c = 'K'; break;
			case 11: c = 'L'; break;
			case 12: c = 'M'; break;
			case 13: c = 'N'; break;
			case 14: c = 'O'; break;
			case 15: c = 'P'; break;
			case 16: c = 'Q'; break;
			case 17: c = 'R'; break;
			case 18: c = 'S'; break;
			case 19: c = 'T'; break;
			case 20: c = 'U'; break;
			case 21: c = 'V'; break;
			case 22: c = 'W'; break;
			case 23: c = 'X'; break;
			case 24: c = 'Y'; break;
			case 25: c = 'Z'; break;
			case 26: c = 'a'; break;
			case 27: c = 'b'; break;
			case 28: c = 'c'; break;
			case 29: c = 'd'; break;
			case 30: c = 'e'; break;
			case 31: c = 'f'; break;
			case 32: c = 'g'; break;
			case 33: c = 'h'; break;
			case 34: c = 'i'; break;
			case 35: c = 'j'; break;
			case 36: c = 'k'; break;
			case 37: c = 'l'; break;
			case 38: c = 'm'; break;
			case 39: c = 'n'; break;
			case 40: c = 'o'; break;
			case 41: c = 'p'; break;
			case 42: c = 'q'; break;
			case 43: c = 'r'; break;
			case 44: c = 's'; break;
			case 45: c = 't'; break;
			case 46: c = 'u'; break;
			case 47: c = 'v'; break;
			case 48: c = 'w'; break;
			case 49: c = 'x'; break;
			case 50: c = 'y'; break;
			case 51: c = 'z'; break;
			case 52: c = '0'; break;
			case 53: c = '1'; break;
			case 54: c = '2'; break;
			case 55: c = '3'; break;
			case 56: c = '4'; break;
			case 57: c = '5'; break;
			case 58: c = '6'; break;
			case 59: c = '7'; break;
			case 60: c = '8'; break;
			case 61: c = '9'; break;
			case 62: c = '+'; break;
			case 63: c = '/'; break;
			default: c = '=';	
			}
			
			return(c);
		}
		
		public byte convert(char c) {
			byte d = 0;
			switch(c) {
				case 'A': d = 0; break;
				case 'B': d = 1; break;
				case 'C': d = 2; break;
				case 'D': d = 3; break;
				case 'E': d = 4; break;
				case 'F': d = 5; break;
				case 'G': d = 6; break;
				case 'H': d = 7; break;
				case 'I': d = 8; break;
				case 'J': d = 9; break;
				case 'K': d = 10; break;
				case 'L': d = 11; break;
				case 'M': d = 12; break;
				case 'N': d = 13; break;
				case 'O': d = 14; break;
				case 'P': d = 15; break;
				case 'Q': d = 16; break;
				case 'R': d = 17; break;
				case 'S': d = 18; break;
				case 'T': d = 19; break;
				case 'U': d = 20; break;
				case 'V': d = 21; break;
				case 'W': d = 22; break;
				case 'X': d = 23; break;
				case 'Y': d = 24; break;
				case 'Z': d = 25; break;
				case 'a': d = 26; break;
				case 'b': d = 27; break;
				case 'c': d = 28; break;
				case 'd': d = 29; break;
				case 'e': d = 30; break;
				case 'f': d = 31; break;
				case 'g': d = 32; break;
				case 'h': d = 33; break;
				case 'i': d = 34; break;
				case 'j': d = 35; break;
				case 'k': d = 36; break;
				case 'l': d = 37; break;
				case 'm': d = 38; break;
				case 'n': d = 39; break;
				case 'o': d = 40; break;
				case 'p': d = 41; break;
				case 'q': d = 42; break;
				case 'r': d = 43; break;
				case 's': d = 44; break;
				case 't': d = 45; break;
				case 'u': d = 46; break;
				case 'v': d = 47; break;
				case 'w': d = 48; break;
				case 'x': d = 49; break;
				case 'y': d = 50; break;
				case 'z': d = 51; break;
				case '0': d = 52; break;
				case '1': d = 53; break;
				case '2': d = 54; break;
				case '3': d = 55; break;
				case '4': d = 56; break;
				case '5': d = 57; break;
				case '6': d = 58; break;
				case '7': d = 59; break;
				case '8': d = 60; break;
				case '9': d = 61; break;
				case '+': d = 62; break;
				case ' ': d = 62; break;
				case '/': d = 63; break;
				default: d = 64;
			}

			return(d);
		}	
	}
}
