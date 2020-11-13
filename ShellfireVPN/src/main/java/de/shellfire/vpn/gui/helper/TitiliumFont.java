/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.helper;

import java.awt.Font;
import java.io.InputStream;

/**
 *
 * @author bettmenn
 */
public class TitiliumFont {

  public static Font getFont() {
    Font font = null;
    String fName = "/fonts/Titillium.ttf";

    try {

      InputStream is = TitiliumFont.class.getResourceAsStream(fName);
      if (is == null)
        throw new Exception("is not available");

      font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(new Float(14)).deriveFont(Font.ITALIC);

    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(fName + " not loaded.  Using serif font.");
      font = new Font("serif", Font.PLAIN, 24);
    }
    return font;
  }
}
