package de.shellfire.vpn.service;

import java.util.TimerTask;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.types.Reason;

public class ConnectionMonitor extends TimerTask {

	private static Logger log = Util.getLogger(ConnectionMonitor.class.getCanonicalName());
	private IVpnController vpnController;

	public ConnectionMonitor(IVpnController vpnController) {
		this.vpnController = vpnController;
	}

	@Override
	public void run() {
		try {
			if (Util.isReachableWithTimeoutAutoRetry("www.google.de")) {
				log.debug("Connection Monitoring: all good");
			} else {
				boolean reconnect = false;
				if (Util.GOOGLE_DE == null) {
					log.debug("Connection Monitoring Detected Timeout - Google IP not known");
					reconnect = true;
				} else {
					log.debug("Connection Monitoring Detected Timeout - testing with GOOGLE_DE ip address");
					if (Util.isReachableWithTimeoutAutoRetry(Util.GOOGLE_DE)) {
						log.debug("Connection Monitoring with IP only worked: all good");
					} else {
						log.debug("Connection Monitoring Detected Timeout - even with IP address. disconnecting & reconnecting");
						reconnect = true;
					}

				}

				if (reconnect) {
					vpnController.disconnect(Reason.ConnectionTimeout);
					log.debug("Connection Monitoring: disconnected - sleeping 1 second");
					Thread.sleep(1000);
					log.debug("Connection Monitoring: after sleeping for 1second, reconnecting");
					vpnController.connect(Reason.ConnectionTimeout);
				}
			}
		} catch (Exception e) {
			log.error("Error in Connection Monitoring {}", e.getMessage(), e);
		}

	}
}
