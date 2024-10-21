
package de.shellfire.vpn.types;

import java.io.Serializable;

public enum Reason implements Serializable {

	ConnectButtonPressed, DisconnectButtonPressed, ConnectionFailed, ConnectionTimeout, PasswordWrong, None, NotEnoughPrivileges, ProcessRestartDetected, CertificateFailed, AllTapInUse, SuccesfulConnectDetected, DisconnectDetected, MapConnectButtonPressed, ApplicationExit, OpenVpnNotFound, AbortButtonPressed, NoOpenVpnParameters, TapDriverTooOld, UnknownOpenVPNError, TapDriverNotFound, GuiRestarting, GatewayRedirectFailed, ServiceStopped, TunTapDriverNotLoaded, SystemSleepInduced, AwokeFromSystemSleep, NoConnectionYet, TapDriverNotFoundPleaseRetry, NoCryptoMining, WireGuardError, WireGuardServiceStarted, OtherVpnSelected, ClientLogout, StatusUpdateUponAppRestart

}
