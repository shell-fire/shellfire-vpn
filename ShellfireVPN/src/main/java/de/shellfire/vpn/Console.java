package de.shellfire.vpn;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author bettmenn
 */
public class Console implements IConsole {

    private static Console instance;
    private StringBuffer content = new StringBuffer();
    private StringBuffer history = new StringBuffer();
    private static Logger log = LoggerFactory.getLogger(Console.class.getCanonicalName());
    
    private Console() {
    }

    public static IConsole getInstance() {
      if (instance == null)
        instance = new Console();
      
      return instance;
    }

    public void append(String lastLine) {
      
    	log.info(lastLine);
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
