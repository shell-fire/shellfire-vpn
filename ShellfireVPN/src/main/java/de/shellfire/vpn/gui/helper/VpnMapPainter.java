/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.HashSet;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.WaypointRenderer;
import org.jdesktop.swingx.painter.Painter;
import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.model.WsGeoPosition;

/**
 *Painter<JXMapViewer>
 * @author bettmenn
 */ 
public class VpnMapPainter implements Painter<JXMapViewer> {
  private static Logger log = Util.getLogger(VpnMapPainter.class.getCanonicalName());
  private WsGeoPosition ownPosition;
  private Controller controller;
  private boolean showOwnPositionOnMap;
  private WaypointPainter<Waypoint> waypointPainter;

  public VpnMapPainter(Controller controller) {
    this.controller = controller;
    this.waypointPainter = new WaypointPainter<Waypoint>();
  }

  public void setShowOwnPositionOnMap(boolean showPos) {
    this.showOwnPositionOnMap = showPos;
  }

  public void setOwnPosition(WsGeoPosition pos) {
    this.ownPosition = pos;
  }

  public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
    drawConnectionRouteIfRequired(g, map);
    waypointPainter.paint(g, map, w, h);
  }

  private void drawConnectionRouteIfRequired(Graphics2D g, JXMapViewer map) {
    if (this.showOwnPositionOnMap) {
      drawConnectionRoute(g, map);
    }
  }

  private void drawConnectionRoute(Graphics2D g, JXMapViewer map) {
    Server connectedTo = controller.connectedTo();
    log.debug("Connected to:" + connectedTo);
    if (connectedTo != null && ownPosition != null) {
      GeoPosition to = new GeoPosition(connectedTo.getLatitude(), connectedTo.getLongitude());

      GeoPosition from = new GeoPosition(ownPosition.getLatitude(), ownPosition.getLongitude());
      g = (Graphics2D) g.create();

      Rectangle rect = map.getViewportBounds();
      g.translate(-rect.x, -rect.y);

      g.setColor(Color.BLACK);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setStroke(new BasicStroke(2));

      Point2D fromPt = map.getTileFactory().geoToPixel(from, map.getZoom());
      Point2D toPt = map.getTileFactory().geoToPixel(to, map.getZoom());

      g.drawLine((int) fromPt.getX() , (int) fromPt.getY() , (int) toPt.getX() , (int) toPt.getY() );
    }
  }

  public void setWaypoints(HashSet<Waypoint> waypoints) {
    this.waypointPainter.setWaypoints(waypoints);

  }

  public void setRenderer(WaypointRenderer<Waypoint> iconWaypointRenderer) {
    this.waypointPainter.setRenderer(iconWaypointRenderer);
  }

}
