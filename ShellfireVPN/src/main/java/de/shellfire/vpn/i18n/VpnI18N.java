package de.shellfire.vpn.i18n;

import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import org.xnap.commons.i18n.I18nManager;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.VpnProperties;

public class VpnI18N {
  private static Logger log = Util.getLogger(VpnI18N.class.getCanonicalName());
  private static final String MESSAGE_BASE = "de.shellfire.vpn.Messages_";
  private static final String SELECTED_LANGUAGE = "InterfaceLanguage";
  private static final String DEFAULT_LANGUAGE = "en";
  private static Language language = null;

  private static LinkedList<Language> availableTranslations;
  private static I18n i18n;

  public static Language getLanguage() {
    if (VpnI18N.language == null) {
      VpnProperties props = VpnProperties.getInstance();

      String languageString = props.getProperty(SELECTED_LANGUAGE, System.getProperty("user.language"));
      Language language = new Language(languageString);
      Locale locale = language.getLocale();
      JOptionPane.setDefaultLocale(locale);

      if (getAvailableTranslations().contains(language))
        VpnI18N.language = language;
      else
        VpnI18N.language = new Language(DEFAULT_LANGUAGE);
    }

    return VpnI18N.language;
  }

  public static I18n getI18n() {
    try {
      if (i18n == null) {
        Language lang = VpnI18N.getLanguage();
        if (lang != null) {
          Locale loc = lang.getLocale();

          if (loc != null) {
        	  try {
        		  i18n = I18nFactory.getI18n(VpnI18N.class, "de.shellfire.vpn.Messages", loc, I18nFactory.FALLBACK);	  
        	  } catch (java.util.MissingResourceException e) {
        		  log.debug("Missing Language Resource Files. Returning Dummy i18n");
        		  
        	  }
            
          }

        }

      }
    } catch (Throwable e) {
      e.printStackTrace();
      e = e.getCause();
      if (e != null)
        e.printStackTrace();
    }
    
    if (i18n == null)
    	return I18nFactory.getI18n(DummyI18n.class);

    return i18n;
  }

  public static String[] dummies() {
    I18n i18n = getI18n();
    String[] dummies = new String[] {
        i18n.tr("Deutsch"),
        i18n.tr("Englisch"),
        i18n.tr("Französisch"),
        i18n.tr("Bitte gib einen Benutzernamen und ein Passwort ein."),
        i18n.tr("Login fehlgeschlagen. Bitte mit Shellfire Kundennummer (K400...), Emailadresse oder VPN Benutzernamen (sf...) einloggen."),
        i18n.tr("Bitte gib eine gültige Email-Adresse ein."), i18n.tr("Ein Account mit dieser Email-Adresse existiert bereits.") };

    return dummies;
  }

  public static void setLanguage(Language language) {
    VpnProperties props = VpnProperties.getInstance();

    if (getAvailableTranslations().contains(language)) {
      props.setProperty(SELECTED_LANGUAGE, language.getKey());
      VpnI18N.language = language;
    }
      

    JOptionPane.setDefaultLocale(language.getLocale());
    I18nManager.getInstance().setDefaultLocale(language.getLocale());
  }

  public static LinkedList<Language> getAvailableTranslations() {
    if (availableTranslations == null) {

      availableTranslations = new LinkedList<Language>();
      Locale[] l = Locale.getAvailableLocales();
      for (int i = 0; i < l.length; i++) {
        Locale cur = l[i];
        Language curLanguage = new Language(cur.getLanguage(), cur.getDisplayLanguage(Locale.GERMAN));
        try {
          String className = MESSAGE_BASE + cur.getLanguage();
          Class.forName(className);

          if (!availableTranslations.contains(curLanguage))
            availableTranslations.add(curLanguage);

        } catch (ClassNotFoundException e) {
          // Locale not available
        } catch (NoClassDefFoundError e) {
          // Locale not available
        }
      }
    }

    return availableTranslations;
  }

  public static CountryI18n getCountryI18n() {
    return CountryI18n.getInstance(getLanguage());
  }
  

}
