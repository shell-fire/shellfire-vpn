package de.shellfire.vpn.i18n;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.types.Country;

public class CountryI18n {

  private static Map<Language, CountryI18n> instanceMap = new HashMap<Language, CountryI18n>();
  private Properties properties;

  private CountryI18n(Language language) {
    String resName = "/countries/countries-" + language.getKey() + ".xml";
    this.properties = new Properties();
    InputStream in = getClass().getResourceAsStream(resName);
    try {
      this.properties.loadFromXML(in);
      in.close();
    } catch (Exception e) {
      Util.handleException(e);
    }

  }

  public static CountryI18n getInstance(Language language) {
    if (instanceMap.get(language) == null) {
      instanceMap.put(language, new CountryI18n(language));
    }

    return instanceMap.get(language);
  }

  public String getCountryName(Country country) {
    String result = null;
    if (this.properties != null)
      result = properties.getProperty(country.toString().toLowerCase(), country.toString());
    else
      result = country.toString();

    return result;
  }

}
