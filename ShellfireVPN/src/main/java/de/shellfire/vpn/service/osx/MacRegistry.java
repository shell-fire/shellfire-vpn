package de.shellfire.vpn.service.osx;

import java.io.File;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.service.IVpnRegistry;

public class MacRegistry implements IVpnRegistry {

    private String launchAgentPath = "";
    
    public MacRegistry() {
        launchAgentPath = System.getProperty("user.home") + "/Library/LaunchAgents/ShellfireVPN.plist";
    }
    
	@Override
	public void enableAutoStart() {
            String nl = "\n";
            String launchAgent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" 
            +"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">" 
            +"<plist version=\"1.0\">" 
            +"<dict>" + nl
            +"<key>Label</key>" +  nl
            +"<string>ShellfireVPN</string>" +  nl
            +"<key>ProgramArguments</key>" +  nl
            +"<array>" +  nl
            +"<string>/usr/bin/open</string>" +  nl
            +"<string>" + com.apple.eio.FileManager.getPathToApplicationBundle() + "</string>" +  nl
            +"</array>" +  nl
            +"<key>ProcessType</key>" +  nl
            +"<string>Interactive</string>" +  nl
            +"<key>RunAtLoad</key>" +  nl
            +"<true/>" +  nl
            +"<key>KeepAlive</key>" +  nl
            +"<false/>" +  nl
            +"</dict>" +  nl
            +"</plist>";
		
            Util.stringToFile(launchAgent, launchAgentPath);
	}

	@Override
	public void disableAutoStart() {
            File f = new File(launchAgentPath);
            if (f.exists())
            	f.delete();
	}

	@Override
	public boolean autoStartEnabled() {
            File f = new File(launchAgentPath);
            return f.exists();
	}

	@Override
	public void disableSystemProxy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableSystemProxy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean autoProxyConfigEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAutoProxyConfigPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
