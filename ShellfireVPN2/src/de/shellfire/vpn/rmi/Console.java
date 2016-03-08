package de.shellfire.vpn.rmi;

import de.shellfire.vpn.gui.IConsole;


/**
 *
 * @author bettmenn
 */
public class Console implements IConsole {

    private static Console instance;
    private StringBuffer content = new StringBuffer();
    private StringBuffer history = new StringBuffer();
    
    private Console() {
    }

    public static IConsole getInstance() {
      if (instance == null)
        instance = new Console();
      
      return instance;
    }

    public void append(String lastLine) {
    	System.out.println(lastLine);
    	this.content.append(lastLine+"\n");
    }
    
    public StringBuffer getNewAppends() {
      StringBuffer result = this.content;
      this.history.append(this.content);
      this.content = new StringBuffer();
      
      return result;
    }

	@Override
	public void append(StringBuffer newLines) {
		if (newLines != null) {
			this.content.append(newLines);
		}
	}

	@Override
	public void setVisible(boolean b) {
		append("Server component's setVisible was called - not implemented");
	}
}
