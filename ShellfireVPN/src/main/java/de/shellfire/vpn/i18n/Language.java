/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.i18n;

import java.util.Locale;

/**
 *
 * @author bettmenn
 */
public class Language {
  String key;
  String name;

  public Language(String key, String name) {
    this(key);
    this.name = name;
  }

  Language(String key) {
    this.key = key;
  }

  public String getKey() {
    return this.key;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return VpnI18N.getI18n().tr(this.getName());
  }

  public boolean equals(Object l) {
    if (l == null)
      return false;
    else if (!(l instanceof Language))
      return false;
    else {
      Language l2 = (Language) l;
      return l2.getKey().equals(this.getKey());
    }

  }

  public Locale getLocale() {
    return new Locale(this.key);
  }
}
