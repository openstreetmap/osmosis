package com.bretth.osmosis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


/**
 * Very basic application for reading a file as utf-8 and writing it to a second
 * file. If this fails there will be a utf-8 encoding error immediately after
 * the point at which the second file stops writing.
 * 
 * @author Brett Henderson
 */
public class TestFileForUtf8 {
	/**
	 * The main entry point.
	 * 
	 * @param args
	 *            The program arguments.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws Exception {
		File inFile;
		File outFile;
		FileInputStream inStream;
		FileOutputStream outStream;
		InputStreamReader reader;
		OutputStreamWriter writer;
		int data;
		
		inFile = new File(args[0]);
		outFile = new File(args[1]);
		
		inStream = new FileInputStream(inFile);
		reader = new InputStreamReader(inStream, Charset.forName("UTF-8"));
		
		outStream = new FileOutputStream(outFile);
		writer = new OutputStreamWriter(outStream, Charset.forName("UTF-8"));
		
		do {
			data = reader.read();
			
			writer.write(data);
			
		} while (data >= 0);
		
		reader.close();
		writer.close();
	}
}
