package de.shellfire.vpn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class LogStreamReader implements Runnable {

  private BufferedReader reader;
  private PrintStream stream;

  public LogStreamReader(InputStream is, boolean errorReader) {
    this.reader = new BufferedReader(new InputStreamReader(is));
    if (errorReader)
      stream = System.err;
    else
      stream = System.out;
  }

  public void run() {
    try {
      String line = reader.readLine();
      while (line != null) {
        stream.println(line);
        line = reader.readLine();
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}