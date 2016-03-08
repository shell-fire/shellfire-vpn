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
public class OpenSansFont {

    public static Font getFont() {
        Font font = null;
        String fName = "resources/OpenSans-Semibold.ttf";
        
        try {

        
            InputStream is = OpenSansFont.class.getResourceAsStream(fName);
            if (is == null)
                throw new Exception("is not available");
            
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(new Float(12));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(fName + " not loaded.  Using serif font.");
            font = new Font("serif", Font.PLAIN, 24);
        }
        return font;
    }
}
