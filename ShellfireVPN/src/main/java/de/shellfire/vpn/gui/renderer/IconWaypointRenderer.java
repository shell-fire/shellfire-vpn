
package de.shellfire.vpn.gui.renderer;


import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;

import de.shellfire.vpn.gui.model.IconWaypoint;

/**
 *
 * @author bettmenn
 */
public class IconWaypointRenderer implements WaypointRenderer<Waypoint> {

  @Override
  public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint waypoint) {
    if (waypoint != null && waypoint instanceof IconWaypoint) {
      IconWaypoint wpn = (IconWaypoint)waypoint;
      BufferedImage icon = wpn.getIcon();
      
      Point2D point = map.getTileFactory().geoToPixel(waypoint.getPosition(), map.getZoom());
      int x = (int)point.getX();
      int y = (int)point.getY();
      
      g.drawImage(icon, x -icon.getWidth() / 2, y -icon.getHeight(), null);
      //g.drawImage(icon, null,null);  


    }
  }
    
}
