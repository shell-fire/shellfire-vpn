package de.shellfire.vpn.gui;

import de.shellfire.www.webservice.sf_soap_php.Star;

public class VpnStar {
  
  private int numStars;
  private String text;

  public VpnStar(int i, String tr) {
    this.numStars = i;
    this.text = tr;
  }
  
  public VpnStar(Star star) {
    this.numStars = star.getNumStars();
    this.text = star.getText();
  }

  public int getNum() {
    return numStars;
  }
  
  public String getText() {
    return text;
  }

}
