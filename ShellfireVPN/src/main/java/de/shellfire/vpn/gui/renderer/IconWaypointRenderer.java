/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.renderer;


import java.awt.Graphics2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

import de.shellfire.vpn.gui.model.IconWaypoint;

/**
 *
 * @author bettmenn
 */
public class IconWaypointRenderer implements WaypointRenderer {

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, Object wpnt) {
      if (wpnt instanceof IconWaypoint) {
        IconWaypoint iwp = (IconWaypoint) wpnt;
        g.drawImage(iwp.getIcon(), null, null);
      }
    }
    
    
    
}
