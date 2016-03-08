/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.rmi.RemoteException;

/**
 *
 * @author bettmenn
 */
public interface ConnectionStateListener {
    public void connectionStateChanged(ConnectionStateChangedEvent e) throws RemoteException;
}
