package de.shellfire.vpn.gui;

import java.awt.event.ActionListener;

import de.shellfire.vpn.Util;
import net.java.simpletraynotify.ScreenPositionHorizontal;
import net.java.simpletraynotify.ScreenPositionVertical;
import net.java.simpletraynotify.SimpleNotifyFrame;
import net.java.simpletraynotify.TrayNotifier;

public class VpnTrayMessage {

    private final String caption;
    private final String text;
    private SimpleNotifyFrame frame;
    
    public VpnTrayMessage(String caption, String text) {

        this.caption = caption;
        this.text = text;
        
        frame = new SimpleNotifyFrame()
                .enableHeader(this.caption)
                .enableContent(this.text)
                .disableIcon()
                ;
        if (Util.isMacOs()) {
        	frame = frame.enableMacOsStyle();
        }
    }
    
    public VpnTrayMessage(String caption, String text, String buttonText, ActionListener listener) {
        this(caption, text);
        
        frame.enableActionButton(buttonText, listener, true);
    }

    public String getCaption() {
      return this.caption;
    }

    public String getText() {
      return this.text;
    }

}
