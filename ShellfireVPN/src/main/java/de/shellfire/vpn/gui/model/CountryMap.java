package de.shellfire.vpn.gui.model;

import java.net.URL;
import java.util.EnumMap;

import javax.swing.ImageIcon;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.types.Country;

public class CountryMap {
  
  private static final EnumMap<Country, String> countryMap = new EnumMap<Country, String>(Country.class);
  private static final EnumMap<Country, ImageIcon> iconMap = new EnumMap<Country, ImageIcon>(Country.class);
  
  
  static {
    countryMap.put(Country.Afghanistan, "af");
    countryMap.put(Country.Egypt, "eg");
    countryMap.put(Country.Albania, "al");
    countryMap.put(Country.Algeria, "dz");
    countryMap.put(Country.AmericanSamoa, "as");
    countryMap.put(Country.UnitedStatesMinorOutlyingIslands, "um");
    countryMap.put(Country.Andorra, "ad");
    countryMap.put(Country.Angola, "ao");
    countryMap.put(Country.Anguilla, "ai");
    countryMap.put(Country.Antarctica, "aq");
    countryMap.put(Country.AntiguaandBarbuda, "ag");
    countryMap.put(Country.EquatorialGuinea, "gq");
    countryMap.put(Country.Argentine, "ar");
    countryMap.put(Country.Armenia, "am");
    countryMap.put(Country.Aruba, "aw");
    countryMap.put(Country.Azerbaijan, "az");
    countryMap.put(Country.Ethiopia, "et");
    countryMap.put(Country.Australia, "au");
    countryMap.put(Country.Bahamas, "bs");
    countryMap.put(Country.Bahrain, "bh");
    countryMap.put(Country.Bangladesh, "bd");
    countryMap.put(Country.Barbados, "bb");
    countryMap.put(Country.Belarus, "by");
    countryMap.put(Country.Belgium, "be");
    countryMap.put(Country.Belize, "bz");
    countryMap.put(Country.Benin, "bj");
    countryMap.put(Country.Bermuda, "bm");
    countryMap.put(Country.Bhutan, "bt");
    countryMap.put(Country.Bolivia, "bo");
    countryMap.put(Country.BosniaandHerzegovina, "ba");
    countryMap.put(Country.Botswana, "bw");
    countryMap.put(Country.BouvetIsland, "bv");
    countryMap.put(Country.Brazil, "br");
    countryMap.put(Country.BritishIndianOceanTerritory, "io");
    countryMap.put(Country.BruneiDarussalam, "bn");
    countryMap.put(Country.Bulgaria, "bg");
    countryMap.put(Country.BurkinaFaso, "bf");
    countryMap.put(Country.Burundi, "bi");
    countryMap.put(Country.Chile, "cl");
    countryMap.put(Country.China, "cn");
    countryMap.put(Country.CookIslands, "ck");
    countryMap.put(Country.CostaRica, "cr");
    countryMap.put(Country.CotedIvoire, "ci");
    countryMap.put(Country.Denmark, "dk");
    countryMap.put(Country.Germany, "de");
    countryMap.put(Country.Dominica, "dm");
    countryMap.put(Country.Djibouti, "dj");
    countryMap.put(Country.Ecuador, "ec");
    countryMap.put(Country.ElSalvardor, "sv");
    countryMap.put(Country.Eritrea, "er");
    countryMap.put(Country.Estonia, "ee");
    countryMap.put(Country.FalklandIslands, "fk");
    countryMap.put(Country.FaroeIslands, "fo");
    countryMap.put(Country.Fiji, "fj");
    countryMap.put(Country.Finland, "fi");
    countryMap.put(Country.France, "fr");
    countryMap.put(Country.FrenchSouthernTerritories, "tf");
    countryMap.put(Country.FrenchGuiana, "gf");
    countryMap.put(Country.FrenchPolynesia, "pf");
    countryMap.put(Country.Gabon, "ga");
    countryMap.put(Country.Gambia, "gm");
    countryMap.put(Country.Georgia, "ge");
    countryMap.put(Country.Ghana, "gh");
    countryMap.put(Country.Gibraltar, "gi");
    countryMap.put(Country.Grenada, "gd");
    countryMap.put(Country.Greece, "gr");
    countryMap.put(Country.Greenland, "gl");
    countryMap.put(Country.Guadeloupe, "gp");
    countryMap.put(Country.Guam, "gu");
    countryMap.put(Country.Guatemala, "gt");
    countryMap.put(Country.Guinea, "gn");
    countryMap.put(Country.GuineaBissau, "gw");
    countryMap.put(Country.Guyana, "gy");
    countryMap.put(Country.Haiti, "ht");
    countryMap.put(Country.VaticanCityStateHolySee, "va");
    countryMap.put(Country.HeardIslandandMcDonaldIslands, "hm");
    countryMap.put(Country.Honduras, "hn");
    countryMap.put(Country.HongKong, "hk");
    countryMap.put(Country.India, "in");
    countryMap.put(Country.Indonesia, "id");
    countryMap.put(Country.Iraq, "iq");
    countryMap.put(Country.Iran, "ir");
    countryMap.put(Country.Ireland, "ie");
    countryMap.put(Country.Iceland, "is");
    countryMap.put(Country.Israel, "il");
    countryMap.put(Country.IsleofMan, "im");
    countryMap.put(Country.Italy, "it");
    countryMap.put(Country.Jamaica, "jm");
    countryMap.put(Country.Japan, "jp");
    countryMap.put(Country.Yemen, "ye");
    countryMap.put(Country.Jordan, "jo");
    countryMap.put(Country.CaymanIslands, "ky");
    countryMap.put(Country.Cambodia, "kh");
    countryMap.put(Country.Cameroon, "cm");
    countryMap.put(Country.Canada, "ca");
    countryMap.put(Country.CapeVerde, "cv");
    countryMap.put(Country.Kazakhstan, "kz");
    countryMap.put(Country.Qatar, "qa");
    countryMap.put(Country.Kenya, "ke");
    countryMap.put(Country.Kyrgyzstan, "kg");
    countryMap.put(Country.Kiribati, "ki");
    countryMap.put(Country.CocosIslands, "cc");
    countryMap.put(Country.Colombia, "co");
    countryMap.put(Country.Comoros, "km");
    countryMap.put(Country.Congo, "cg");
    countryMap.put(Country.CongodemocraticRep, "cd");
    countryMap.put(Country.KoreaDemocraticPeoplesRepublicof, "kp");
    countryMap.put(Country.KoreaRepublicof, "kr");
    countryMap.put(Country.Croatia, "hr");
    countryMap.put(Country.Cuba, "cu");
    countryMap.put(Country.Kuwait, "kw");
    countryMap.put(Country.Latvia, "lv");
    countryMap.put(Country.Lebanon, "lb");
    countryMap.put(Country.Liberia, "lr");
    countryMap.put(Country.LibyanArabJamahiriya, "ly");
    countryMap.put(Country.Liechtenstein, "li");
    countryMap.put(Country.Lithuania, "lt");
    countryMap.put(Country.Luxembourg, "lu");
    countryMap.put(Country.Macau, "mo");
    countryMap.put(Country.Madagascar, "mg");
    countryMap.put(Country.Malawi, "mw");
    countryMap.put(Country.Malaysia, "my");
    countryMap.put(Country.Maldives, "mv");
    countryMap.put(Country.Mali, "ml");
    countryMap.put(Country.Malta, "mt");
    countryMap.put(Country.MarianaIslands, "mp");
    countryMap.put(Country.Morocco, "ma");
    countryMap.put(Country.MarshallIslands, "mh");
    countryMap.put(Country.Martinique, "mq");
    countryMap.put(Country.Mauritania, "mr");
    countryMap.put(Country.Mauritius, "mu");
    countryMap.put(Country.Mayotte, "yt");
    countryMap.put(Country.Macedonia, "mk");
    countryMap.put(Country.Mexico, "mx");
    countryMap.put(Country.MicronesiaFederatedStatesof, "fm");
    countryMap.put(Country.MoldovaRepublicof, "md");
    countryMap.put(Country.Mongolia, "mn");
    countryMap.put(Country.Montserrat, "ms");
    countryMap.put(Country.Mozambique, "mz");
    countryMap.put(Country.MyanmarBurma, "mm");
    countryMap.put(Country.Namibia, "na");
    countryMap.put(Country.Nauru, "nr");
    countryMap.put(Country.Nepal, "np");
    countryMap.put(Country.NewCaledonia, "nc");
    countryMap.put(Country.NewZealand, "nz");
    countryMap.put(Country.Nicaragua, "ni");
    countryMap.put(Country.Netherlands, "nl");
    countryMap.put(Country.NetherlandsAntilles, "an");
    countryMap.put(Country.Niger, "ne");
    countryMap.put(Country.Nigeria, "ng");
    countryMap.put(Country.Niue, "nu");
    countryMap.put(Country.NorfolkIsland, "nf");
    countryMap.put(Country.Norway, "no");
    countryMap.put(Country.Oman, "om");
    countryMap.put(Country.Austria, "at");
    countryMap.put(Country.EastTimor, "tp");
    countryMap.put(Country.Pakistan, "pk");
    countryMap.put(Country.Palau, "pw");
    countryMap.put(Country.Panama, "pa");
    countryMap.put(Country.PapuaNewGuinea, "pg");
    countryMap.put(Country.Paraguay, "py");
    countryMap.put(Country.Peru, "pe");
    countryMap.put(Country.Philipines, "ph");
    countryMap.put(Country.PitcairnIsland, "pn");
    countryMap.put(Country.Poland, "pl");
    countryMap.put(Country.Portugal, "pt");
    countryMap.put(Country.PuertoRico, "pr");
    countryMap.put(Country.Reunion, "re");
    countryMap.put(Country.Rwanda, "rw");
    countryMap.put(Country.Romania, "ro");
    countryMap.put(Country.RussianFederation, "ru");
    countryMap.put(Country.SolomonIslands, "sb");
    countryMap.put(Country.Zambia, "zm");
    countryMap.put(Country.Samoa, "ws");
    countryMap.put(Country.SanMarino, "sm");
    countryMap.put(Country.SaoTomeandPrincipe, "st");
    countryMap.put(Country.SaudiArabia, "sa");
    countryMap.put(Country.Sweden, "se");
    countryMap.put(Country.Switzerland, "ch");
    countryMap.put(Country.Senegal, "sn");
    countryMap.put(Country.Seychelles, "sc");
    countryMap.put(Country.SierraLeone, "sl");
    countryMap.put(Country.Zimbabwe, "zw");
    countryMap.put(Country.Singapore, "sg");
    countryMap.put(Country.Slovakia, "sk");
    countryMap.put(Country.Slovenia, "si");
    countryMap.put(Country.Somalia, "so");
    countryMap.put(Country.Spain, "es");
    countryMap.put(Country.SriLanka, "lk");
    countryMap.put(Country.SaintHelena, "sh");
    countryMap.put(Country.SaintKittsandNevis, "kn");
    countryMap.put(Country.SaintLucia, "lc");
    countryMap.put(Country.SaintPierreandMiquelon, "pm");
    countryMap.put(Country.SaintVincentandtheGrenadines, "vc");
    countryMap.put(Country.SouthAfrica, "za");
    countryMap.put(Country.Sudan, "sd");
    countryMap.put(Country.SouthGeorgiaandthesouthSandwichIslands, "gs");
    countryMap.put(Country.Suriname, "sr");
    countryMap.put(Country.SvalbardandJanMayen, "sj");
    countryMap.put(Country.Swaziland, "sz");
    countryMap.put(Country.SyrianArabRepublic, "sy");
    countryMap.put(Country.Tajikistan, "tj");
    countryMap.put(Country.TaiwanProvinceofChina, "tw");
    countryMap.put(Country.TanzaniaUnitedRepublicof, "tz");
    countryMap.put(Country.Thailand, "th");
    countryMap.put(Country.Togo, "tg");
    countryMap.put(Country.TokelauIslands, "tk");
    countryMap.put(Country.Tonga, "to");
    countryMap.put(Country.TrinidadandTobago, "tt");
    countryMap.put(Country.Chad, "td");
    countryMap.put(Country.CzechRepublic, "cz");
    countryMap.put(Country.Tunisia, "tn");
    countryMap.put(Country.Turkey, "tr");
    countryMap.put(Country.Turkmenistan, "tm");
    countryMap.put(Country.TurksandCaicosIslands, "tc");
    countryMap.put(Country.Tuvalu, "tv");
    countryMap.put(Country.Uganda, "ug");
    countryMap.put(Country.Ukraine, "ua");
    countryMap.put(Country.Hungary, "hu");
    countryMap.put(Country.Uruguay, "uy");
    countryMap.put(Country.Uzbekistan, "uz");
    countryMap.put(Country.Vanuatu, "vu");
    countryMap.put(Country.Venezuela, "ve");
    countryMap.put(Country.UnitedArabEmirates, "ae");
    countryMap.put(Country.Usa, "us");
    countryMap.put(Country.UnitedKingdom, "gb");
    countryMap.put(Country.VietNam, "vn");
    countryMap.put(Country.WallisandFutunaIslands, "wf");
    countryMap.put(Country.ChristmasIsland, "cx");
    countryMap.put(Country.CentralAfricanRepublic, "cf");
    countryMap.put(Country.Cyprus, "cy");
    countryMap.put(Country.Serbia, "rs");

  }
  
  
  
  public static String get(Country country) {
    return countryMap.get(country);
  }

  public static ImageIcon getIcon(Country country) {
    if (iconMap.get(country) == null) {
      URL file = CountryMap.class.getResource("/flags/"+get(country) + ".png");
      ImageIcon icon = null;
      if (file != null) {
        icon = Util.getImageIcon("/flags/"+get(country) + ".png");
      } else {
        icon = Util.getImageIcon("/flags/de.png");
      }
      

      iconMap.put(country, icon);
    }
    
    return iconMap.get(country);
  }
  
  
}

