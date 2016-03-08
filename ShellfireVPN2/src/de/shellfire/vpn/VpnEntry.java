package de.shellfire.vpn;

import de.shellfire.www.webservice.sf_soap_php.Entry;









public class VpnEntry  extends Entry {

  public VpnEntry(Entry entry) {
    super(entry.isBoolEntry(), entry.isStarEntry(), entry.isStringEntry(), entry.isBool(), entry.getStar(), entry.getText());
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
