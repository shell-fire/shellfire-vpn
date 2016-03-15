/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.model;

import java.util.HashSet;
import java.util.LinkedList;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.Waypoint;

import de.shellfire.vpn.client.Controller;
import de.shellfire.vpn.gui.helper.MapHoverListener;
import de.shellfire.vpn.gui.helper.VpnMapPainter;
import de.shellfire.vpn.gui.renderer.IconWaypointRenderer;
import de.shellfire.vpn.types.LocatableIcon;
import de.shellfire.vpn.types.Server;
import de.shellfire.vpn.webservice.ServerList;
import de.shellfire.vpn.webservice.model.WsGeoPosition;

/**
 *
 * @author bettmenn
 */
public class MapController {
  private final JXMapKit mapKit;
  private boolean showOwnPositionOnMap;

  private VpnMapPainter painter;
  private ServerList serverList;
  private WsGeoPosition ownPosition;
  private Waypoint ownPositionWaypoint;
  private HomePosition homePosition;
  private final Controller controller;
  private MapHoverListener hoverListener;
  private HashSet<Waypoint> serverWayPoints;
  private LinkedList<LocatableIcon> serverHoverItems;

  public MapController(JXMapKit jXMapKit1, Controller controller) {
    this.mapKit = jXMapKit1;

    this.controller = controller;
    this.initPainter();
    this.initHoverListener();
    this.mapKit.getMainMap().setRestrictOutsidePanning(true);
    this.mapKit.getMainMap().setHorizontalWrapped(false);
  }

  public void setServers(ServerList serverList) {
    this.serverList = serverList;
  }

  public void updateMap() {
    this.updateWaypoints();
    this.updateHoverListener();
    mapKit.getMainMap().repaint();
  }

  public void setOwnPosition(WsGeoPosition pos) {
    this.ownPosition = pos;
    this.painter.setOwnPosition(pos);
  }

  public void setShowOwnPositionOnMap(boolean showOwnPositionOnMap) {
    this.showOwnPositionOnMap = showOwnPositionOnMap;
    this.painter.setShowOwnPositionOnMap(showOwnPositionOnMap);
    this.updateMap();
  }

  private void updateWaypoints() {
    HashSet<Waypoint> waypoints = getAllWaypoints();
    painter.setWaypoints(waypoints);
  }

  private HashSet<Waypoint> getAllWaypoints() {
    HashSet<Waypoint> serverWaypoints = this.getServerWaypoints();

    @SuppressWarnings("unchecked")
    HashSet<Waypoint> result = (HashSet<Waypoint>) serverWaypoints.clone();

    if (this.showOwnPositionOnMap) {
      Waypoint homeWaypoint = this.getHomeWaypoint();
      result.add(homeWaypoint);
    }

    return result;
  }

  private void initPainter() {
    painter = new VpnMapPainter(this.controller);
    painter.setWaypoints(new HashSet<Waypoint>());
    painter.setRenderer(new IconWaypointRenderer());
    mapKit.getMainMap().setOverlayPainter( painter);
  }

  private HashSet<Waypoint> getServerWaypoints() {
    if (this.serverWayPoints == null) {
      HashSet<Waypoint> waypoints = new HashSet<Waypoint>();

      for (Server server : serverList.getAll()) {
        waypoints.add(new IconWaypoint(server));
      }

      this.serverWayPoints = waypoints;
    }

    return this.serverWayPoints;

  }

  private Waypoint getHomeWaypoint() {

    if (!this.showOwnPositionOnMap) {
      this.ownPositionWaypoint = null;
    } else {
      if (this.ownPosition != null) {
        if (this.homePosition == null)
          this.homePosition = new HomePosition(this.ownPosition);

        this.ownPositionWaypoint = new IconWaypoint(this.homePosition);
      }

    }

    return this.ownPositionWaypoint;
  }

  private void initHoverListener() {
    if (this.hoverListener == null) {
      JXMapViewer map = mapKit.getMainMap();
      this.hoverListener = new MapHoverListener(map);
      this.mapKit.getMainMap().addMouseMotionListener(this.hoverListener);
    }
  }

  private void updateHoverListener() {
    LinkedList<LocatableIcon> locatableIconList = this.getHoverItems();
    this.hoverListener.setHoverItems(locatableIconList);
  }

  private LinkedList<LocatableIcon> getHoverItems() {
    LinkedList<LocatableIcon> serverHoverItems = this.getServerHoverItems();

    LinkedList<LocatableIcon> hoverItems = (LinkedList<LocatableIcon>) serverHoverItems.clone();

    if (this.showOwnPositionOnMap && this.homePosition != null) {
      hoverItems.add(homePosition);
    }

    return hoverItems;
  }

  private LinkedList<LocatableIcon> getServerHoverItems() {
    if (this.serverHoverItems == null) {
      LinkedList<LocatableIcon> locatableIconList = new LinkedList<LocatableIcon>();
      for (Server server : serverList.getAll()) {
        server.setController(controller);
        LocatableIcon locatableIcon = (LocatableIcon) server;
        locatableIconList.add(locatableIcon);
      }
      serverHoverItems = locatableIconList;
    }

    return this.serverHoverItems;
  }

}
