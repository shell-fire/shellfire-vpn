package de.shellfire.vpn.gui.model;

import java.net.URL;
import java.util.EnumMap;

import org.slf4j.Logger;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.types.Country;
import javafx.scene.image.Image;

public class CountryMap {

	private static final EnumMap<Country, Image> iconMapFX = new EnumMap<Country, Image>(Country.class);
	private static final Logger log = Util.getLogger(CountryMap.class.getCanonicalName());
	
	public static String get(Country country) {
		return country.name();
	}

	public static Image getIconFX(Country country) {
		if (iconMapFX.get(country) == null) {
			URL file = CountryMap.class.getResource("/flags/" + get(country) + ".png");
			Image icon = null;
			if (file != null) {
				icon = Util.getImageIconFX("/flags/" + get(country) + ".png");
			} else {
				log.warn("no map found for {}, using default Germany.png", country.toString());
				icon = Util.getImageIconFX("/flags/Germany.png");
			}

			iconMapFX.put(country, icon);
		}

		return iconMapFX.get(country);
	}
}
