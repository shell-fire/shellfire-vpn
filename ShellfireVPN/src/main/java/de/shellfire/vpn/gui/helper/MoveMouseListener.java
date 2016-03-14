/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.helper;

import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *
 * @author bettmenn
 */
public class MoveMouseListener implements MouseListener, MouseMotionListener {
  Window target;
  Point start_drag;
  Point start_loc;

  public MoveMouseListener(Window target) {
    this.target = target;
  }

  public static Window getFrame(Container target) {
    if (target instanceof Window) {
      return (Window) target;
    }
    return getFrame(target.getParent());
  }

  Point getScreenLocation(MouseEvent e) {
    Point cursor = e.getPoint();
    Point target_location = this.target.getLocationOnScreen();
    return new Point((int) (target_location.getX() + cursor.getX()),
        (int) (target_location.getY() + cursor.getY()));
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    this.start_drag = this.getScreenLocation(e);
    this.start_loc = MoveMouseListener.getFrame(this.target).getLocation();
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    Point current = this.getScreenLocation(e);
    Point offset = new Point((int) current.getX() - (int) start_drag.getX(),
        (int) current.getY() - (int) start_drag.getY());
    Window frame = MoveMouseListener.getFrame(target);
    Point new_location = new Point(
        (int) (this.start_loc.getX() + offset.getX()), (int) (this.start_loc
            .getY() + offset.getY()));
    frame.setLocation(new_location);
  }

  public void mouseMoved(MouseEvent e) {
  }
}
