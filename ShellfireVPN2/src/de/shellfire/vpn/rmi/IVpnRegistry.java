package de.shellfire.vpn.rmi;


public interface IVpnRegistry {

	void addVpnToAutoStart();

	void removeVpnFromAutoStart();

	boolean vpnAutoStartEnabled();

	void disableSystemProxy();

	void enableSystemProxy();

	boolean isAutoProxyConfigEnabled();

	String getAutoProxyConfigPath();

}