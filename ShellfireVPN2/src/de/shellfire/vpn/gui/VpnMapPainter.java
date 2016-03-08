/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

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
import org.jdesktop.swingx.painter.Painter;

import de.shellfire.vpn.Controller;
import de.shellfire.vpn.Server;
import de.shellfire.www.webservice.sf_soap_php.WsGeoPosition;

/**
 *
 * @author bettmenn
 */
class VpnMapPainter implements Painter<JXMapViewer> {
    private WsGeoPosition ownPosition;
    private Controller controller;
    private boolean showOwnPositionOnMap;
	private WaypointPainter waypointPainter;

    public VpnMapPainter(Controller controller) {
        this.controller = controller;
        this.waypointPainter = new WaypointPainter();
    }

    public void setShowOwnPositionOnMap(boolean showPos) {
        this.showOwnPositionOnMap = showPos;
    }
    public void setOwnPosition(WsGeoPosition pos) {
        this.ownPosition = pos;
    }
    
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        try {
          drawConnectionRouteIfRequired(g, map);
        } catch (RemoteException e) {
          Util.handleException(e);
        }
        waypointPainter.paint(g, map, w, h);
    }

    private void drawConnectionRouteIfRequired(Graphics2D g, JXMapViewer map) throws RemoteException {
        if (this.showOwnPositionOnMap){
            drawConnectionRoute(g, map);
        }
    }

    private void drawConnectionRoute(Graphics2D g, JXMapViewer map) throws RemoteException {
        Server connectedTo = controller.connectedTo();
        System.out.println("Connected to:" + connectedTo);
        if (connectedTo != null) {
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

            g.drawLine((int) fromPt.getX() + 8, (int) fromPt.getY() + 8, (int) toPt.getX() + 8, (int) toPt.getY() + 8);
        }
    }

	public void setWaypoints(HashSet<Waypoint> waypoints) {
		this.waypointPainter.setWaypoints(waypoints);
		
	}

	public void setRenderer(IconWaypointRenderer iconWaypointRenderer) {
		this.waypointPainter.setRenderer(iconWaypointRenderer);
		
	}
    
}
