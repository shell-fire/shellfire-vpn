/**
 * Entry.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package de.shellfire.vpn.webservice.model;

public class Entry implements java.io.Serializable {
  private boolean boolEntry;

  private boolean starEntry;

  private boolean stringEntry;

  private boolean bool;

  private Star star;

  private java.lang.String text;

  public Entry() {
  }

  public Entry(boolean boolEntry, boolean starEntry, boolean stringEntry, boolean bool, Star star, java.lang.String text) {
    this.boolEntry = boolEntry;
    this.starEntry = starEntry;
    this.stringEntry = stringEntry;
    this.bool = bool;
    this.star = star;
    this.text = text;
  }

  /**
   * Gets the boolEntry value for this Entry.
   * 
   * @return boolEntry
   */
  public boolean isBoolEntry() {
    return boolEntry;
  }

  /**
   * Sets the boolEntry value for this Entry.
   * 
   * @param boolEntry
   */
  public void setBoolEntry(boolean boolEntry) {
    this.boolEntry = boolEntry;
  }

  /**
   * Gets the starEntry value for this Entry.
   * 
   * @return starEntry
   */
  public boolean isStarEntry() {
    return starEntry;
  }

  /**
   * Sets the starEntry value for this Entry.
   * 
   * @param starEntry
   */
  public void setStarEntry(boolean starEntry) {
    this.starEntry = starEntry;
  }

  /**
   * Gets the stringEntry value for this Entry.
   * 
   * @return stringEntry
   */
  public boolean isStringEntry() {
    return stringEntry;
  }

  /**
   * Sets the stringEntry value for this Entry.
   * 
   * @param stringEntry
   */
  public void setStringEntry(boolean stringEntry) {
    this.stringEntry = stringEntry;
  }

  /**
   * Gets the bool value for this Entry.
   * 
   * @return bool
   */
  public boolean isBool() {
    return bool;
  }

  /**
   * Sets the bool value for this Entry.
   * 
   * @param bool
   */
  public void setBool(boolean bool) {
    this.bool = bool;
  }

  /**
   * Gets the star value for this Entry.
   * 
   * @return star
   */
  public Star getStar() {
    return star;
  }

  /**
   * Sets the star value for this Entry.
   * 
   * @param star
   */
  public void setStar(Star star) {
    this.star = star;
  }

  /**
   * Gets the text value for this Entry.
   * 
   * @return text
   */
  public java.lang.String getText() {
    return text;
  }

  /**
   * Sets the text value for this Entry.
   * 
   * @param text
   */
  public void setText(java.lang.String text) {
    this.text = text;
  }

}
