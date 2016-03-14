/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.types;

import java.awt.Image;

import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import de.shellfire.vpn.client.Controller;

/**
 *
 * @author bettmenn
 */
public interface LocatableIcon {
    public GeoPosition getGeoPosition();
    public Image getIcon();

    public JPanel getPanel();

    public String getCity();

    public String getCountryString();
    
    public Controller getController();
    public void setController(Controller controller);
}
