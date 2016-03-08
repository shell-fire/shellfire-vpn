package de.shellfire.vpn.rmi;

import java.io.File;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.apple.eawt.AppEvent.SystemSleepEvent;
import com.apple.eawt.Application;
import com.apple.eawt.SystemSleepListener;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import de.shellfire.vpn.ConnectionState;
import de.shellfire.vpn.Reason;
import de.shellfire.vpn.gui.IConsole;
import de.shellfire.vpn.gui.MacRegistry;
import de.shellfire.vpn.gui.Util;
import de.shellfire.vpn.rmi.IVpnRegistry;

public class OpenVpnProcessHost extends UnicastRemoteObject implements IOpenVpnProcessHost {
	private static final long serialVersionUID = -8843768672588937918L;
	public static boolean IS_SERVICE = false;
  private static IVpnRegistry registry;
	private ConnectionState connectionState = ConnectionState.Disconnected;
	private String parametersForOpenVpn;
	private Reason reasonForStateChange;
	private boolean connectionStateChanged;
	private ProcessWrapper inputStreamWorker;
	private ProcessWrapper errorStreamWorker;
	private IConsole console = Console.getInstance();
	private String appData;
	private OpenVpnManagementClient openVpnManagementClient;
	private Timer connectionMonitor;
	//public static final int SHELLFIRE_REGISTRY_PORT = 60313;
	public static final int SHELLFIRE_REGISTRY_PORT = 1099;
	public static final String SHELLFIRE_OPEN_VPN_PROCESS_HOST = "ShellfireOpenVpnProcessHost";
	public static final String SHELLFIREVPN_SERVICE_RMI = "//127.0.0.1:" + SHELLFIRE_REGISTRY_PORT + "/" + SHELLFIRE_OPEN_VPN_PROCESS_HOST;
	
	private Boolean sleepBeingHandled = false;
	private boolean disconnectedDueToSleep;
  
	protected OpenVpnProcessHost() throws RemoteException {
		super();
		initAppleEventHandlers();

	}

	public static void main(String[] args) {
		OpenVpnProcessHost.IS_SERVICE  = true;
		System.out.println("OpenVpnProcessHost starting up");
		
		if (args.length > 0 && args[0].equals("stop")) {
			System.out.println("called with stop command - running mainstop");
			
			mainstop(args);
			return;
		}
		Registry registry = null;
		System.out.println("creating Registry");
		try {
			registry = LocateRegistry.createRegistry(SHELLFIRE_REGISTRY_PORT);
		}

		catch (RemoteException ex) {
			System.out.println(ex.getMessage());
			System.out.println("Could not create registry, shutting down");
			return; // terminate on exception
		}
		try {
			System.out.println("Creating new ProcessHost");
			OpenVpnProcessHost host = new OpenVpnProcessHost();
			System.out.println("binding the processhost using Naming.rebind()");
		
			registry.rebind(SHELLFIRE_OPEN_VPN_PROCESS_HOST, host);
		}  catch (RemoteException ex) {
			System.out.println(ex.getMessage());
		}
		System.out.println("done");
		
	}

	public static void mainstop(String[] args) {
		try {
			System.out.println("mainstop called - getting openvpnprocess host via Naming.lookup");
			IOpenVpnProcessHost o = (IOpenVpnProcessHost) Naming.lookup(OpenVpnProcessHost.SHELLFIRE_OPEN_VPN_PROCESS_HOST);

			
			System.out.println("disconnecting from vpn service");
			// make sure openvpn is disconnected, routes are being removed
			o.disconnect(Reason.ServiceStopped);

			
			// make sure RMI runtime is shut down gracefully
			o.exit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void disconnect(Reason reason) throws RemoteException {
		appendConsole("disconnect(Reason="+reason+")");
		
		if (Util.isWindows()) {
			Kernel32 kernel32 = Kernel32.INSTANCE;
			HANDLE result = kernel32.CreateEvent(null, true, false, "ShellfireVPN2ExitEvent"); // request
																								// deletion
			kernel32.SetEvent(result);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				throw new RemoteException("InterruptedException at OpenVpnProcessHost.disconnect()", e);
			}
			kernel32.PulseEvent(result);

		} else {
			try {
				if (openVpnManagementClient != null)
					openVpnManagementClient.disconnect();

			} catch (IOException e) {
				throw new RemoteException("IOException at OpenVpnProcessHost.disconnect()", e);
			}
			
			try {
				List<String> kextUnloadCmds = new LinkedList<String>();
				kextUnloadCmds.add("/sbin/kextunload");
				kextUnloadCmds.add(getOpenVpnDir() + "tun.kext");

				appendConsole("Unloading tun.kext with command: " + Util.listToString(kextUnloadCmds));
				
				Process kextUnload = new ProcessBuilder(kextUnloadCmds).start();			
				this.bindConsole(kextUnload);
			} catch (IOException e) {
				appendConsole("Unloading tun.kext did not work - ignoring");
			}		
			
		}

		this.setConnectionState(ConnectionState.Disconnected, reason);
		fixTapDevices();
	}

	@Override
	public ConnectionState getConnectionState() throws RemoteException {
		return this.connectionState;
	}

	@Override
	public void setParametersForOpenVpn(String params) throws RemoteException {
		this.parametersForOpenVpn = params;

	}

 	@Override
	public void setConnectionState(ConnectionState newState, Reason reason) throws RemoteException {
 		appendConsole("setConnectionState(ConnectionState newState="+newState+", Reason reason="+reason+")");
		this.connectionState = newState;
		this.reasonForStateChange = reason;
		this.connectionStateChanged = true;
		
		if (newState == ConnectionState.Connected) {
			startConnectionMonitoring();
		} else {
			stopConnectionMonitoring();
		}
		
	}


	private void stopConnectionMonitoring() {
		// if connection monitoring is already active stop it
		appendConsole("stopping connection monitoring");
		if (connectionMonitor != null) {
			connectionMonitor.cancel();
			connectionMonitor = null;
		}
	}
	
	// auto re-connect on Timeout!
	private void startConnectionMonitoring() {
		appendConsole("starting connection monitoring");
		// if connection monitoring is not yet active, start it
		if (this.connectionMonitor == null) {
			this.connectionMonitor = new Timer();
			connectionMonitor.schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						if (Util.isReachableWithTimeoutAutoRetry("www.google.de")) {
              appendConsole("Connection Monitoring: all good");
						} else {
              boolean reconnect = false;
              if (Util.GOOGLE_DE == null) {
                appendConsole("Connection Monitoring Detected Timeout - Google IP not known");
                reconnect = true;
              } else {
                appendConsole("Connection Monitoring Detected Timeout - testing with GOOGLE_DE ip address");
                if (Util.isReachableWithTimeoutAutoRetry(Util.GOOGLE_DE)) {
                  appendConsole("Connection Monitoring with IP only worked: all good");
                } else {
                  appendConsole("Connection Monitoring Detected Timeout - even with IP address. disconnecting & reconnecting");
                  reconnect = true;
                }
                
              }
              
              if (reconnect) {
                disconnect(Reason.ConnectionTimeout);
                appendConsole("Connection Monitoring: disconnected - sleeping 1 second");
                Thread.sleep(1000);
                appendConsole("Connection Monitoring: after sleeping for 1second, reconnecting");
                connect(Reason.ConnectionTimeout);
              }
              
						  
						}
						
						
					} catch (Exception e) {
						appendConsole("Error in connection monitoring");
						appendConsole(e.toString());
						e.printStackTrace();
					}
					
				}}, 5000, 20000);
		}
		
		
	}

	public boolean getConnectionStateChanged() throws RemoteException {
		if (this.connectionStateChanged) {
			this.connectionStateChanged = false;
			return true;
		}

		return false;
	}

	@Override
	public Reason getReasonForStateChange() throws RemoteException {
		return this.reasonForStateChange;
	}

	@Override
	public void connect() throws RemoteException {
		this.connect(this.reasonForStateChange);
	}

	private void connect(Reason reason) throws RemoteException {
		try {
			appendConsole("connect(Reason="+reason+")");
			fixTapDevices();

			String openVpnStartString = this.getOpenVpnStartString();

			if (parametersForOpenVpn == null) {
				this.setConnectionState(ConnectionState.Disconnected, Reason.NoOpenVpnParameters);
				return;
			}

			if (openVpnStartString == null) {
				appendConsole("Aborting connect: could not retrieve openVpnStartString");
				this.setConnectionState(ConnectionState.Disconnected, Reason.OpenVpnNotFound);
				return;
			}

			Runtime runtime = Runtime.getRuntime();

			if (this.getConnectionState() == ConnectionState.Disconnected) {
				appendConsole("Setting connectionState to connecting");
				this.setConnectionState(ConnectionState.Connecting, reason);
			}
				

			Process p = null;
			if (Util.isWindows()) {
				String search = "%APPDATA%\\ShellfireVPN";
				String replace = this.appData;
				// parametersForOpenVpn = parametersForOpenVpn.replace(search,
				// replace).replace("\"", "");
				parametersForOpenVpn = parametersForOpenVpn.replace(search, replace);
        
        if (Util.isWin8OrWin10()) {
          String blockDns = " --block-outside-dns";
          if (parametersForOpenVpn != null && !parametersForOpenVpn.contains(blockDns)) {
            parametersForOpenVpn += blockDns;  
          }
        }
				
				p = runtime.exec(openVpnStartString + " " + this.parametersForOpenVpn, null, new File("."));
				appendConsole(openVpnStartString + " " + parametersForOpenVpn);
			} else {
				// make sure kexts have the proper user rights 
				appendConsole("ServiceTools.protectKext("+System.getProperty("user.dir")+");");
				ServiceTools.protectKext(System.getProperty("user.dir"));
				
				String[] cmdList = parametersForOpenVpn.split(" ");

				List<String> cmds = new LinkedList<String>();
				cmds.add(openVpnStartString);
				String search = "%APPDATA%/ShellfireVPN";

				String replace = this.appData;
				for (String cmd : cmdList) {
					cmd = cmd.replace(search, replace).replace("\"", "");
					cmds.add(cmd);
				}

				this.openVpnManagementClient = new OpenVpnManagementClient(console, this);
				new Thread(openVpnManagementClient).start();

				String vpnDir = getOpenVpnDir();
				String vpnDirForConfig = vpnDir.replace(" ", "\\ ");
				cmds.add("--verb");
				cmds.add("2");
				cmds.add("--up");
				cmds.add(vpnDirForConfig + "client.up.sh");
				cmds.add("--down");
				cmds.add(vpnDirForConfig + "client.down.sh");
				cmds.add("--script-security");
				cmds.add("2");
				cmds.add("--management");
				cmds.add("localhost");
				cmds.add("1399");
				cmds.add("--management-client");
				cmds.add("--management-hold");
				// cmds.add("--daemon");
				
				List<String> kextLoadCmds = new LinkedList<String>();
				kextLoadCmds.add("/sbin/kextload");
				kextLoadCmds.add(vpnDir + "tun.kext");
				appendConsole("Loading tun.kext with command: " + Util.listToString(kextLoadCmds));
				
				Process kextLoad = new ProcessBuilder(kextLoadCmds).start();
				this.bindConsole(kextLoad);
				
				appendConsole("Starting openvpn with command: " +  Util.listToString(cmds));
				
				p = new ProcessBuilder(cmds).start();
			}

			this.bindConsole(p);
			if (Util.isWindows()) {
				appendConsole(openVpnStartString);
				appendConsole(parametersForOpenVpn);
			} else {
				appendConsole(openVpnStartString);
			}

		} catch (IOException ex) {
			appendConsole("Error occured during connect. Exception details:");
			ex.printStackTrace();
			appendConsole(Util.getStackTrace(ex));
			this.setConnectionState(ConnectionState.Disconnected, Reason.OpenVpnNotFound);

		}
	}
	
	private String getOpenVpnDir() {
		return System.getProperty("user.dir") + "/openvpn/";
	}

	private String getOpenVpnStartString() {
		Map<String, String> envs = System.getenv();
		String programFiles = envs.get("ProgramFiles");
		String programFiles86 = envs.get("ProgramFiles(x86)");

		List<String> possibleOpenVpnExeLocations = Util.getPossibleExeLocations(programFiles, programFiles86);

		for (String possibleLocaion : possibleOpenVpnExeLocations) {
			File f = new File(possibleLocaion);
			if (f.exists())
				return possibleLocaion;
		}

		return null;
	}

	private void bindConsole(Process process) {
		this.inputStreamWorker = new ProcessWrapper(process.getInputStream(), console, this);
		this.inputStreamWorker.start();

		this.errorStreamWorker = new ProcessWrapper(process.getErrorStream(), console, this);
		this.errorStreamWorker.start();
	}

	@Override
	public StringBuffer getNewConsoleLines() throws RemoteException {
		if (console != null)
			return console.getNewAppends();
		else
			return new StringBuffer();
	}

	@Override
	public void setConnecting() throws RemoteException {
		this.setConnectionState(ConnectionState.Connecting, Reason.None);
	}

	@Override
	public void exit() throws RemoteException {
		try {
			appendConsole("OpenVPNProcessHost exiting.");
			Naming.unbind(OpenVpnProcessHost.SHELLFIRE_OPEN_VPN_PROCESS_HOST);
		
			UnicastRemoteObject.unexportObject(this, true);

			
			Thread.sleep(2000);
			System.exit(0);
			
		} catch (Exception e) {
		}
	}

	@Override
	public void setAppDataFolder(String appData) throws RemoteException {
		this.appData = appData;
	}

	private void fixTapDevices() throws RemoteException {
		if (Util.isWindows() && Util.isVistaOrLater()) {
			appendConsole("Performing tap-fix on windows");
			TapFixer.restartAllTapDevices();
		}
	}
	
	private void appendConsole(String s) {
		if (this.console != null)
			this.console.append(s.trim());
	}
	
	private void initAppleEventHandlers() {
		if (!Util.isWindows()) {
			Application macApplication = Application.getApplication();
			macApplication.addAppEventListener(new SystemSleepListener() {
				public void systemAboutToSleep(SystemSleepEvent arg0) {
					new Thread(new Runnable() {

						public void run() {
							synchronized (sleepBeingHandled) {
								appendConsole("System going to sleep");
								stopConnectionMonitoring();
								if (connectionState == ConnectionState.Connected || connectionState == ConnectionState.Connecting) {
									disconnectedDueToSleep = true;
								
									appendConsole("disconnecting");
									try {
										disconnect(Reason.SystemSleepInduced);
									} catch (RemoteException e) {
										appendConsole(e.getLocalizedMessage());
										appendConsole(Util.getStackTrace(e));
									}
								
								}
							}
							
						}
					}).start();

				}

				public void systemAwoke(SystemSleepEvent arg0) {
					new Thread(new Runnable() {
						public void run() {
							appendConsole("System woke up - waiting until (potentially still running going-to-sleep-process is finished...");
							synchronized (sleepBeingHandled) {
								appendConsole("...done. Checking for internet availability.");

								boolean networkIsAvailable = Util.internetIsAvailable();

								if (networkIsAvailable) {
									appendConsole("Internet is available");
									
									if (disconnectedDueToSleep && connectionState == ConnectionState.Disconnected) {
										appendConsole("Connection has been terminated due to sleep mode, reconnecting automatically");
										try {
											connect(Reason.AwokeFromSystemSleep);
										} catch (RemoteException e) {
											appendConsole(e.getLocalizedMessage());
											appendConsole(Util.getStackTrace(e));
										}
										disconnectedDueToSleep = false;
									} else {
										appendConsole("Was not connected - doing nothing :-)");
									}
									
								} else {
									appendConsole("No network connection after sleep");
								}	
							}
							
						}
					}).start();

				}

			});	
		}
	}

  @Override
  public void reinstallTapDriver() throws RemoteException {
    TapFixer.reinstallTapDriver();
    
  }

  @Override
  public void addVpnToAutoStart() throws RemoteException {
    getRegistry().addVpnToAutoStart();
    
  }

  @Override
  public void removeVpnFromAutoStart() throws RemoteException {
    getRegistry().removeVpnFromAutoStart();
    
  }

  @Override
  public boolean vpnAutoStartEnabled() throws RemoteException {
    return getRegistry().vpnAutoStartEnabled();
  }

  @Override
  public void disableSystemProxy() throws RemoteException {
    getRegistry().disableSystemProxy();
  }

  @Override
  public void enableSystemProxy() throws RemoteException {
    getRegistry().enableSystemProxy();
  }

  @Override
  public boolean isAutoProxyConfigEnabled() throws RemoteException {
    try {
      return getRegistry().isAutoProxyConfigEnabled();  
    } catch (Exception e) {
      e.printStackTrace();
      throw new RemoteException(e.getMessage());
    }
    
  }

  @Override
  public String getAutoProxyConfigPath() throws RemoteException {
    return getRegistry().getAutoProxyConfigPath();
  }
  
  /**
   * This should only be instantiated from the process host with root privileges
   * @return
   */
  public static IVpnRegistry getRegistry() throws RemoteException {
    if (registry == null) {
      if (Util.isWindows()) {
        try {
          registry = new WinRegistry();  
        } catch (Exception e) {
          e.printStackTrace();
          throw new RemoteException(e.getMessage());
        }
        
      } else {
          registry = new MacRegistry();
      }
    }

    return registry;
  }
	
}
