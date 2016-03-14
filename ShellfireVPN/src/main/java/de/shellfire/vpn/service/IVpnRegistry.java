package de.shellfire.vpn.service;


public interface IVpnRegistry {

	void enableAutoStart();

	void disableAutoStart();

	boolean autoStartEnabled();

	void disableSystemProxy();

	void enableSystemProxy();

	boolean autoProxyConfigEnabled();

	String getAutoProxyConfigPath();

}