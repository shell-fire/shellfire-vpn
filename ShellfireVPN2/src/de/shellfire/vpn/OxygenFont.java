/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn;

import java.awt.Font;
import java.io.InputStream;

/**
 *
 * @author bettmenn
 */
public class OxygenFont {

	private static Font regular = null;
	private static Font largeBold = null;
	
    public static Font getFont() {
    	if (regular == null) {
            String fName = "resources/Oxygen.ttf";
            
            try {
                InputStream is = OxygenFont.class.getResourceAsStream(fName);
                if (is == null)
                    throw new Exception("is not available");
                
                regular = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(new Float(12));
                
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println(fName + " not loaded.  Using serif font.");
                regular = new Font("serif", Font.PLAIN, 24);
            }   		
    	}

        return regular;
    }
    
    public static Font getFontLargeBold() {
    	if (largeBold == null) {
    		largeBold  = getFont().deriveFont(Font.BOLD, 16);
    	}
    	
        return largeBold;
    }
}
