/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

/**
 * 
 * @author bettmenn
 */
public class MapHoverListener implements MouseMotionListener {

  private final JXMapViewer mainMap;
  private JPanel hover;
  private LinkedList<LocatableIcon> locatableIconList;
  private Timer currentTimer;
  private static final int Delay = 3;
  private HashMap<LocatableIcon, JPanel> hovers = new HashMap<LocatableIcon, JPanel>();
  private MouseEvent e;
  private org.netbeans.lib.awtextra.AbsoluteConstraints constraints = new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40);

  public MapHoverListener(JXMapViewer mainMap) {
    this.mainMap = mainMap;
    this.mainMap.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

  }

  public void setHoverItems(LinkedList<LocatableIcon> locatableIconList) {
    this.locatableIconList = locatableIconList;
    this.initLabels();
  }

  private boolean mouseOverIcon(LocatableIcon icon, MouseEvent e) {
    GeoPosition iconGeoPosition = (icon.getGeoPosition());
    GeoPosition mouseGeoPosition = (mainMap.convertPointToGeoPosition(e.getPoint()));
    GeoPosition normalisedMouseGeoPosition = normalise(mouseGeoPosition);
    Point mousePixelPosition = this.getConvertedGp(normalisedMouseGeoPosition);
    Point iconPixelPosition = this.getConvertedGp(iconGeoPosition);
    mousePixelPosition = this.getIconCenter(mousePixelPosition);
    double distance = iconPixelPosition.distance(mousePixelPosition);
    boolean result = (distance < 16);

    return result;
  }

  private GeoPosition normalise(GeoPosition pos) {
    double longitude = pos.getLongitude();
    while (longitude > 180)
      longitude -= 360;
    while (longitude < -180)
      longitude += 360;

    return new GeoPosition(pos.getLatitude(), longitude);
  }

  private Point getConvertedGp(GeoPosition myGp) {
    Point2D gp_pt = this.mainMap.getTileFactory().geoToPixel(myGp, this.mainMap.getZoom());
    // convert to screen
    Rectangle rect = this.mainMap.getViewportBounds();

    return new Point((int) gp_pt.getX() - rect.x, (int) gp_pt.getY() - rect.y);
  }

  private void processHover(LocatableIcon icon, MouseEvent e) {
    if (this.mouseOverIcon(icon, e)) {

      boolean startShowHover = false;

      // if a hoverbox is already displayed
      if (this.hover != null) {
        // check if its the hoverbox of the current icon
        if (this.hover.equals(this.hovers.get(icon))) {
          // if yes, simply restart the timer and do nothing else
          this.stopCurrentTimer();
          startShowHover = true;
        } else {
          // otherwise
          // stop the current timer for, hide it and then show the
          // hover for the other server
          this.stopCurrentTimer();
          this.hideCurrentHover();
          startShowHover = true;
        }
      } else {
        // hover was null - start the show
        startShowHover = true;
      }

      if (startShowHover) {
        this.showHover(icon);
        this.startTimer();
      }
    }

    if (this.hover != null && this.hover.isVisible()) {
      if (this.hover.equals(this.hovers.get(icon))) {
        this.updateHoverPosition(icon);
      }
    }
  }

  private void showHover(LocatableIcon icon) {
    if (this.hover != null && this.hover.isVisible()) {
      this.hover.setVisible(false);
      this.mainMap.remove(hover);
    }

    this.hover = this.hovers.get(icon);
    this.mainMap.add(this.hover, this.constraints);
    // update position
    this.updateHoverPosition(icon);

    // set to visible
    hover.setVisible(true);

  }

  private void updateHoverPosition(LocatableIcon icon) {
    GeoPosition iconPosition = icon.getGeoPosition();
    Point pixelPosition = this.getConvertedGp(iconPosition);
    
    Point labelPosition = this.getLabelPosition((pixelPosition));
    this.hover.setLocation(labelPosition);
    this.constraints.x = labelPosition.x;
    this.constraints.y = labelPosition.y;
  }

  private void hideCurrentHover() {
    if (hover != null) {
      hover.setVisible(false);
      this.mainMap.remove(hover);
    }
  }

  private void startTimer() {
    int delay = MapHoverListener.Delay * 1000; // milliseconds
    ActionListener taskPerformer = new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (!mouseInHoverBox()) {
          hover.setVisible(false);
          // mainMap.remove(hover);
          hover = null;
        }
        currentTimer.stop();
        currentTimer = null;
      }
    };

    this.currentTimer = new Timer(delay, taskPerformer);
    this.currentTimer.setRepeats(false);
    this.currentTimer.start();
  }

  private void stopCurrentTimer() {
    if (this.currentTimer != null) {
      this.currentTimer.stop();
      this.currentTimer = null;
    }
  }

  private void processHover(MouseEvent e) {
    this.e = e;

    for (LocatableIcon icon : this.locatableIconList) {
      this.processHover(icon, e);
    }
  }

  private boolean mouseInHoverBox() {
    if (hover == null) {
      return false;
    }

    if (!hover.isVisible()) {
      return false;
    }

    Point mp = e.getPoint();

    int hoverX = hover.getX();
    int hoverY = hover.getY();
    int hoverWidth = hover.getWidth();
    int hoverHeight = hover.getHeight();

    boolean inX = (mp.x > hoverX) && (mp.x < hoverX + hoverWidth);
    boolean inY = (mp.y > hoverY) && (mp.y < hoverY + hoverHeight);

    if (inX && inY) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    this.processHover(e);
  }

  public void mouseMoved(MouseEvent e) {
    this.processHover(e);
  }

  private GeoPosition getGeoPosition(LocatableIcon icon) {
    return icon.getGeoPosition();
  }

  private void initLabels() {
    if (this.hovers == null)
      this.hovers = new HashMap<LocatableIcon, JPanel>();

    if (this.locatableIconList != null) {
      for (LocatableIcon icon : this.locatableIconList) {
        JPanel curPanel = icon.getPanel();
        curPanel.setVisible(false);

        if (!this.hovers.containsKey(icon)) {

          // this.addToMainMapIfNotYetContained(curPanel);
          this.hovers.put(icon, curPanel);
        }

      }
    }
  }

  /**
   * Corrects for the fact that the hover point should be the middle of the icon noth the top left corner
   * 
   * @param point
   * @return
   */
  private Point getIconCenter(Point point) {
    return new Point(point.x - 8, point.y - 8);
  }

  private Point getLabelPosition(Point point) {
    Point p = new Point(point.x + 25, point.y);

    // make sure the hover box is not displayed outside of the visible map
    int boxWidth = this.hover.getWidth();
    int boxHeight = this.hover.getHeight();

    int mapWidth = this.mainMap.getWidth();
    int mapHeight = this.mainMap.getHeight();

    // if on the right there is not enough space, display on the left
    if (p.x + boxWidth > mapWidth) {
      p.x = point.x - 25 - boxWidth;
    }

    // if too much on the bottom, display above
    if (p.y + boxHeight > mapHeight) {
      p.y = point.y - boxHeight;
    }

    return p;
  }

}
