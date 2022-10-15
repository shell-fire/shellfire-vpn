package de.shellfire.vpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import ch.qos.logback.classic.Logger;

public class LogStreamReader implements Runnable {

	private InputStreamReader reader;
	private boolean errorReader;
	private InputStream inputStream;
	private static Logger log = Util.getLogger(LogStreamReader.class.getCanonicalName());
	
	public LogStreamReader(InputStream is, boolean errorReader) {
		this.inputStream = is;
		// this.reader = (new InputStreamReader(is));
		this.errorReader = errorReader;
	}

	public void run() {
		try {
			
			for (int ch; (ch = inputStream.read()) != -1; ) {
			    System.out.print((char)ch);
			}
			
			/*
			String line = reader.readLine();
			while (line != null) {
				if (errorReader) {
					log.error(line);
				} else {
					log.debug(line);
				}
				line = reader.readLine();
			}
			*/
			//reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}