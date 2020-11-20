/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.helper;

import java.awt.Font;
import java.io.InputStream;

import de.shellfire.vpn.Util;

/**
 *
 * @author bettmenn
 */
public class OpenSansFont {

	public static Font getFont() {
		Font font = null;
		String fName = "/fonts/OpenSans-Semibold.ttf";

		try {

			InputStream is = OpenSansFont.class.getResourceAsStream(fName);
			if (is == null)
				throw new Exception("is not available");

			font = Font.createFont(Font.TRUETYPE_FONT, is);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(fName + " not loaded.  Using serif font.");
			font = new Font("serif", Font.PLAIN, 12);
		}
		return font;
	}
}
