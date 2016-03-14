package de.shellfire.vpn.gui.helper;

import java.awt.KeyboardFocusManager;
import java.util.Arrays;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class Max4CharDocument extends PlainDocument {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
    if (str == null)
      return;

    str = str.toUpperCase();
    
    String out = "";
    String[] valids = new String[] {"A","B","C","D","E","F","G","H","I", "K","L","M","N","O", "P","Q", "R","S","T","U", "V", "W","X","Y", "Z","0", "1", "2","3","4","5","6","7", "8","9"};
    
    for (int i = 0; i < str.length(); i++) {
        String curChar = str.substring(i, i+1);
        if (Arrays.asList(valids).contains(curChar)) {
          
            out += curChar;
        }
    }
    
    
    if ((getLength() + str.length()) <= 4) {
      super.insertString(offset, out, attr);
    }
    
    if (getLength() == 4 && str.length() > 0 && offset == 3) {
      KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      manager.focusNextComponent();      
    }
      
    
  }

}
