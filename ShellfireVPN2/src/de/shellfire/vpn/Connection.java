/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.shellfire.vpn.gui.IConsole;
import de.shellfire.vpn.gui.Util;
import de.shellfire.vpn.gui.Util.ExceptionThrowingReturningRunnable;
import de.shellfire.vpn.gui.VpnConsole;
import de.shellfire.vpn.rmi.IOpenVpnProcessHost;
import de.shellfire.vpn.rmi.IVpnRegistry;
import de.shellfire.vpn.rmi.OpenVpnProcessHost;

/**
 * 
 * @author bettmenn
 */
public class Connection extends Thread {
	private Server server;
	private final Controller controller;
	private static IOpenVpnProcessHost processHost;
	private static IVpnRegistry registry = Util.getRegistry();
	
	private static boolean init;
	private IConsole vpnConsole = VpnConsole.getInstance();
	private ConnectionState initialConnectionState;
	
  /*
  static {
    try {
      initRmi();
    } catch (Exception e) {
      Util.handleException(e);
    }
  }
  */
	public Connection(Controller controller, Reason reason) throws RemoteException, NotBoundException, MalformedURLException {
		this.controller = controller;
	}

	public static void initRmi() throws MalformedURLException, RemoteException, NotBoundException {
		if (!init) {
			final Registry registry = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Registry>() {

				public Registry run() throws Exception {
					System.out.println("LocateRegistry.getRegistry(127.0.0.1, OpenVpnProcessHost.SHELLFIRE_REGISTRY_PORT)");
					return LocateRegistry.getRegistry("127.0.0.1", OpenVpnProcessHost.SHELLFIRE_REGISTRY_PORT);
				}
				
			}, 5, 50);
		
			processHost = (IOpenVpnProcessHost) Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Remote>() {
			  
				public Remote run() throws Exception {
					System.out.println("registry.lookup(OpenVpnProcessHost.SHELLFIRE_OPEN_VPN_PROCESS_HOST);");
					return registry.lookup(OpenVpnProcessHost.SHELLFIRE_OPEN_VPN_PROCESS_HOST);
				}
				
			}, 5, 50);
			
			Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
				public Void run() throws Exception {
				  System.out.println("processHost.setAppDataFolder(Util.getConfigDir());");
					processHost.setAppDataFolder(Util.getConfigDir());
					
					return null;
				}
			}, 10, 50);
			
		}
	}
	
	public void setVpn(Vpn vpn) {
		this.server = vpn.getServer();
	}

	public void disconnect(final Reason reason) {
		Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
			public Void run() throws Exception {
				processHost.disconnect(reason);
				
				return null;
			}
		}, 10, 50);

	}

	public ConnectionState getConnectionState() throws RemoteException {
		ConnectionState newState = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<ConnectionState>() {
			public ConnectionState run() throws Exception {
				return processHost.getConnectionState();
			}
		}, 10, 50);

		if (initialConnectionState == null) {
			initialConnectionState = newState;
			setConnectionState(newState, Reason.NoConnectionYet);
		}
		
		return newState;
	}

	public void setParametersForOpenVpn(final String params) {
		Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
			public Void run() throws Exception {
				processHost.setParametersForOpenVpn(params);
				
				return null;
			}
		}, 10, 50);

	}

	@Override
	public void run() {
		Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
			public Void run() throws Exception {
				processHost.connect();
				
				return null;
			}
		}, 10, 50);

		while (true) {
			Boolean stateChanged = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Boolean>() {
				public Boolean run() throws Exception {
					return processHost.getConnectionStateChanged();
				}
			}, 10, 50);
			
			
			if (stateChanged == true) {
				ConnectionState state = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<ConnectionState>() {
					public ConnectionState run() throws Exception {
						return processHost.getConnectionState();
					}
				}, 10, 50);
				
				Reason reason = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Reason>() {
					public Reason run() throws Exception {
						return processHost.getReasonForStateChange();
					}
				}, 10, 50);
				
				
				try {
          this.setConnectionState(state, reason);
        } catch (RemoteException e) {
          Util.handleException(e);
        }
			}

			StringBuffer newLines = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<StringBuffer>() {
				public StringBuffer run() throws Exception {
					return processHost.getNewConsoleLines();
				}
			}, 10, 50);
			
			vpnConsole.append(newLines);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public Reason getReasonForStateChange() {
		
		Reason result = Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Reason>() {
			public Reason run() throws Exception {
				return processHost.getReasonForStateChange();
			}
		}, 10, 50);
	
		if (result == null) {
			result = Reason.None;
		}
		
		return result;
	}

	Server getServer() {
		return this.server;
	}

	public void setConnecting() {
		Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
			public Void run() throws Exception {
				processHost.setConnecting();
				
				return null;
			}
		}, 10, 50);
	}

	public void pushConnectionState(final ConnectionState newState, final Reason reason) {
		Util.runWithAutoRetry(new ExceptionThrowingReturningRunnable<Void>() {
			public Void run() throws Exception {
				processHost.setConnectionState(newState, reason);
				
				return null;
			}
		}, 10, 50);
		
	}

	public void setConnectionState(ConnectionState newState, Reason reason) throws RemoteException {
		this.controller.connectionStateChanged();
	}

  public static void addVpnToAutoStart() throws RemoteException {
     registry.addVpnToAutoStart();
  }

  public static void removeVpnFromAutoStart() throws RemoteException {
    registry.removeVpnFromAutoStart();
  }

  public static boolean vpnAutoStartEnabled() throws RemoteException {
    return registry.vpnAutoStartEnabled();
  }

  public static void disableSystemProxy() throws RemoteException {
    registry.disableSystemProxy();
  }

  public static void enableSystemProxy() throws RemoteException {
    registry.enableSystemProxy();
  }

  public static boolean isAutoProxyConfigEnabled() throws RemoteException {
    return registry != null && registry.isAutoProxyConfigEnabled();
  }

  public static String getAutoProxyConfigPath() throws RemoteException {
    return registry.getAutoProxyConfigPath();
  }
  
}
