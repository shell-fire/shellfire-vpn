/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.awt.Image;

import org.jdesktop.swingx.mapviewer.Waypoint;

/**
 *
 * @author bettmenn
 */
public class IconWaypoint extends Waypoint {
    private final Image icon;

    public Image getIcon() {
        return icon;
    }
    
    public IconWaypoint(LocatableIcon locatableIcon) {
        super(locatableIcon.getGeoPosition());
        this.icon = locatableIcon.getIcon();
    }
    
}
