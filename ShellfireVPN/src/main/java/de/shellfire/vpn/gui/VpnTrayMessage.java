package de.shellfire.vpn.gui;

import java.awt.event.ActionListener;

import de.shellfire.vpn.Util;
import net.java.simpletraynotify.ScreenPositionHorizontal;
import net.java.simpletraynotify.ScreenPositionVertical;
import net.java.simpletraynotify.SimpleNotifyFrame;
import net.java.simpletraynotify.TrayNotifier;

public class VpnTrayMessage implements Runnable {

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
    

    @Override
    public void run() {
        TrayNotifier trayNotifier = new TrayNotifier(frame);
        trayNotifier.setNumPixelsFromScreenHorizontal(30);
        trayNotifier.setNumPixelsFromScreenVertical(30);

        if (Util.isWindows()) {
        	// tray items in windows are usually on the lower right side of the screen
        	trayNotifier.setBaseHorizontal(ScreenPositionHorizontal.Bottom);
            trayNotifier.setBaseVertical(ScreenPositionVertical.Right);
        } else {
        	// and in macos, on top!
            trayNotifier.setBaseHorizontal(ScreenPositionHorizontal.Top);
            trayNotifier.setBaseVertical(ScreenPositionVertical.Right);
        	
        }
        trayNotifier.setShowDuration(10F);
        trayNotifier.run();
    }
}
