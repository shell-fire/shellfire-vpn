/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;


import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

/**
 *
 * @author bettmenn
 */
public class IconWaypointRenderer implements WaypointRenderer {

    @Override
    public boolean paintWaypoint(Graphics2D gd, JXMapViewer jxmv, Waypoint wpnt) {
        if (wpnt instanceof IconWaypoint) {
            IconWaypoint iwp = (IconWaypoint) wpnt;
            gd.drawImage(iwp.getIcon(), null, null);

            return true;            
        }
        
        return false;
    }
    
    
    
}
