package de.shellfire.vpn.gui;

import de.shellfire.vpn.rmi.IVpnRegistry;

import java.io.File;

public class MacRegistry implements IVpnRegistry {

    private String launchAgentPath = "";
    
    public MacRegistry() {
        launchAgentPath = System.getProperty("user.home") + "/Library/LaunchAgents/ShellfireVPN.plist";
    }
    
	@Override
	public void addVpnToAutoStart() {
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
	public void removeVpnFromAutoStart() {
            File f = new File(launchAgentPath);
            if (f.exists())
            	f.delete();
	}

	@Override
	public boolean vpnAutoStartEnabled() {
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
	public boolean isAutoProxyConfigEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getAutoProxyConfigPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
