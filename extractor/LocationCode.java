/*
 * LocationCode.java
 *
 * Created on 06 March 2002, 13:39
 */

package gilbert.extractor;
import java.util.*;
import org.apache.log4j.*;

/**
 * Contains country names and codes for top-level domains
 *
 * @author  Daniel Hahn
 * @version CVS $Revision$
 */
public class LocationCode {
    
    /// Logger for this class
    protected static Logger logger = Logger.getLogger(LocationCode.class);
    
    /**
     * Array with codes and names.
     */
    protected static final String[][] cCodes = {
        // WARNING: The order of this data may matter.
        // IN SHORT: DO NOT TOUCH...
        { "1", "ad", "Andorra" },
        { "2", "ae", "United Arab Emirates" },
        { "3", "af", "Afghanistan" },
        { "4", "ag", "Antigua and Barbuda" },
        { "5", "ai", "Anguilla" },
        { "6", "al", "Albania" },
        { "7", "am", "Armenia" },
        { "8", "an", "Netherlands Antilles" },
        { "9", "ao", "Angola" },
        { "10", "aq", "Antarctica" },
        { "11", "ar", "Argentina" },
        { "12", "as", "American Samoa" },
        { "13", "at", "Austria" },
        { "14", "au", "Australia" },
        { "15", "aw", "Aruba" },
        { "16", "az", "Azerbaijan" },
        { "17", "ba", "Bosnia and Herzegovina" },
        { "18", "bb", "Barbados" },
        { "19", "bd", "Bangladesh" },
        { "20", "be", "Belgium" },
        { "21", "bf", "Burkina Faso" },
        { "22", "bg", "Bulgaria" },
        { "23", "bh", "Bahrain" },
        { "24", "bi", "Burundi" },
        { "25", "bj", "Benin" },
        { "26", "bm", "Bermuda" },
        { "27", "bn", "Brunei Darussalam" },
        { "28", "bo", "Bolivia" },
        { "29", "br", "Brazil" },
        { "30", "bs", "Bahamas" },
        { "31", "bt", "Bhutan" },
        { "32", "bv", "Bouvet Island" },
        { "33", "bw", "Botswana" },
        { "34", "by", "Belarus" },
        { "35", "bz", "Belize" },
        { "36", "ca", "Canada" },
        { "37", "cc", "Cocos (Keeling) Islands" },
        { "38", "cf", "Central African Republic" },
        { "39", "cg", "Congo" },
        { "40", "ch", "Switzerland" },
        { "41", "ci", "Cote D'Ivoire (Ivory Coast)" },
        { "42", "ck", "Cook Islands" },
        { "43", "cl", "Chile" },
        { "44", "cm", "Cameroon" },
        { "45", "cn", "China" },
        { "46", "co", "Colombia" },
        { "47", "cr", "Costa Rica" },
        { "48", "cs", "Czechoslovakia (former)" },
        { "49", "cu", "Cuba" },
        { "50", "cv", "Cape Verde" },
        { "51", "cx", "Christmas Island" },
        { "52", "cy", "Cyprus" },
        { "53", "cz", "Czech Republic" },
        { "54", "de", "Germany" },
        { "55", "dj", "Djibouti" },
        { "56", "dk", "Denmark" },
        { "57", "dm", "Dominica" },
        { "58", "do", "Dominican Republic" },
        { "59", "dz", "Algeria" },
        { "60", "ec", "Ecuador" },
        { "61", "ee", "Estonia" },
        { "62", "eg", "Egypt" },
        { "63", "eh", "Western Sahara" },
        { "64", "er", "Eritrea" },
        { "65", "es", "Spain" },
        { "66", "et", "Ethiopia" },
        { "67", "fi", "Finland" },
        { "68", "fj", "Fiji" },
        { "69", "fk", "Falkland Islands (Malvinas)" },
        { "70", "fm", "Micronesia" },
        { "71", "fo", "Faroe Islands" },
        { "72", "fr", "France" },
        { "73", "fx", "France, Metropolitan" },
        { "74", "ga", "Gabon" },
        { "75", "gb", "Great Britain (UK)" },
        { "76", "gd", "Grenada" },
        { "77", "ge", "Georgia" },
        { "78", "gf", "French Guiana" },
        { "79", "gh", "Ghana" },
        { "80", "gi", "Gibraltar" },
        { "81", "gl", "Greenland" },
        { "82", "gm", "Gambia" },
        { "83", "gn", "Guinea" },
        { "84", "gp", "Guadeloupe" },
        { "85", "gq", "Equatorial Guinea" },
        { "86", "gr", "Greece" },
        { "87", "gs", "S. Georgia and S. Sandwich Isls." },
        { "88", "gt", "Guatemala" },
        { "89", "gu", "Guam" },
        { "90", "gw", "Guinea-Bissau" },
        { "91", "gy", "Guyana" },
        { "92", "hk", "Hong Kong" },
        { "93", "hm", "Heard and McDonald Islands" },
        { "94", "hn", "Honduras" },
        { "95", "hr", "Croatia (Hrvatska)" },
        { "96", "ht", "Haiti" },
        { "97", "hu", "Hungary" },
        { "98", "id", "Indonesia" },
        { "99", "ie", "Ireland" },
        { "100", "il", "Israel" },
        { "101", "in", "India" },
        { "102", "io", "British Indian Ocean Territory" },
        { "103", "iq", "Iraq" },
        { "104", "ir", "Iran" },
        { "105", "is", "Iceland" },
        { "106", "it", "Italy" },
        { "107", "jm", "Jamaica" },
        { "108", "jo", "Jordan" },
        { "109", "jp", "Japan" },
        { "110", "ke", "Kenya" },
        { "111", "kg", "Kyrgyzstan" },
        { "112", "kh", "Cambodia" },
        { "113", "ki", "Kiribati" },
        { "114", "km", "Comoros" },
        { "115", "kn", "Saint Kitts and Nevis" },
        { "116", "kp", "Korea (North)" },
        { "117", "kr", "Korea (South)" },
        { "118", "kw", "Kuwait" },
        { "119", "ky", "Cayman Islands" },
        { "120", "kz", "Kazakhstan" },
        { "121", "la", "Laos" },
        { "122", "lb", "Lebanon" },
        { "123", "lc", "Saint Lucia" },
        { "124", "li", "Liechtenstein" },
        { "125", "lk", "Sri Lanka" },
        { "126", "lr", "Liberia" },
        { "127", "ls", "Lesotho" },
        { "128", "lt", "Lithuania" },
        { "129", "lu", "Luxembourg" },
        { "130", "lv", "Latvia" },
        { "131", "ly", "Libya" },
        { "132", "ma", "Morocco" },
        { "133", "mc", "Monaco" },
        { "134", "md", "Moldova" },
        { "135", "mg", "Madagascar" },
        { "136", "mh", "Marshall Islands" },
        { "137", "mk", "Macedonia" },
        { "138", "ml", "Mali" },
        { "139", "mm", "Myanmar" },
        { "140", "mn", "Mongolia" },
        { "141", "mo", "Macau" },
        { "142", "mp", "Northern Mariana Islands" },
        { "143", "mq", "Martinique" },
        { "144", "mr", "Mauritania" },
        { "145", "ms", "Montserrat" },
        { "146", "mt", "Malta" },
        { "147", "mu", "Mauritius" },
        { "148", "mv", "Maldives" },
        { "149", "mw", "Malawi" },
        { "150", "mx", "Mexico" },
        { "151", "my", "Malaysia" },
        { "152", "mz", "Mozambique" },
        { "153", "na", "Namibia" },
        { "154", "nc", "New Caledonia" },
        { "155", "ne", "Niger" },
        { "156", "nf", "Norfolk Island" },
        { "157", "ng", "Nigeria" },
        { "158", "ni", "Nicaragua" },
        { "159", "nl", "Netherlands" },
        { "160", "no", "Norway" },
        { "161", "np", "Nepal" },
        { "162", "nr", "Nauru" },
        { "163", "nt", "Neutral Zone" },
        { "164", "nu", "Niue" },
        { "165", "nz", "New Zealand (Aotearoa)" },
        { "166", "om", "Oman" },
        { "167", "pa", "Panama" },
        { "168", "pe", "Peru" },
        { "169", "pf", "French Polynesia" },
        { "170", "pg", "Papua New Guinea" },
        { "171", "ph", "Philippines" },
        { "172", "pk", "Pakistan" },
        { "173", "pl", "Poland" },
        { "174", "pm", "St. Pierre and Miquelon" },
        { "175", "pn", "Pitcairn" },
        { "176", "pr", "Puerto Rico" },
        { "177", "pt", "Portugal" },
        { "178", "pw", "Palau" },
        { "179", "py", "Paraguay" },
        { "180", "qa", "Qatar" },
        { "181", "re", "Reunion" },
        { "182", "ro", "Romania" },
        { "183", "ru", "Russian Federation" },
        { "184", "rw", "Rwanda" },
        { "185", "sa", "Saudi Arabia" },
        { "186", "sb", "Solomon Islands" },
        { "187", "sc", "Seychelles" },
        { "188", "sd", "Sudan" },
        { "189", "se", "Sweden" },
        { "190", "sg", "Singapore" },
        { "191", "sh", "St. Helena" },
        { "192", "si", "Slovenia" },
        { "193", "sj", "Svalbard and Jan Mayen Islands" },
        { "194", "sk", "Slovak Republic" },
        { "195", "sl", "Sierra Leone" },
        { "196", "sm", "San Marino" },
        { "197", "sn", "Senegal" },
        { "198", "so", "Somalia" },
        { "199", "sr", "Suriname" },
        { "200", "st", "Sao Tome and Principe" },
        { "201", "su", "USSR (former)" },
        { "202", "sv", "El Salvador" },
        { "203", "sy", "Syria" },
        { "204", "sz", "Swaziland" },
        { "205", "tc", "Turks and Caicos Islands" },
        { "206", "td", "Chad" },
        { "207", "tf", "French Southern Territories" },
        { "208", "tg", "Togo" },
        { "209", "th", "Thailand" },
        { "210", "tj", "Tajikistan" },
        { "211", "tk", "Tokelau" },
        { "212", "tm", "Turkmenistan" },
        { "213", "tn", "Tunisia" },
        { "214", "to", "Tonga" },
        { "215", "tp", "East Timor" },
        { "216", "tr", "Turkey" },
        { "217", "tt", "Trinidad and Tobago" },
        { "218", "tv", "Tuvalu" },
        { "219", "tw", "Taiwan" },
        { "220", "tz", "Tanzania" },
        { "221", "ua", "Ukraine" },
        { "222", "ug", "Uganda" },
        { "223", "uk", "United Kingdom" },
        { "224", "um", "US Minor Outlying Islands" },
        { "225", "us", "United States" },
        { "226", "uy", "Uruguay" },
        { "227", "uz", "Uzbekistan" },
        { "228", "va", "Vatican City State (Holy See)" },
        { "229", "vc", "Saint Vincent and the Grenadines" },
        { "230", "ve", "Venezuela" },
        { "231", "vg", "Virgin Islands (British)" },
        { "232", "vi", "Virgin Islands (U.S.)" },
        { "233", "vn", "Viet Nam" },
        { "234", "vu", "Vanuatu" },
        { "235", "wf", "Wallis and Futuna Islands" },
        { "236", "ws", "Samoa" },
        { "237", "ye", "Yemen" },
        { "238", "yt", "Mayotte" },
        { "239", "yu", "Yugoslavia" },
        { "240", "za", "South Africa" },
        { "241", "zm", "Zambia" },
        { "242", "zr", "Zaire" },
        { "243", "zw", "Zimbabwe" },
        { "244", "com", "US Commercial" },
        { "245", "edu", "US Educational" },
        { "246", "gov", "US Government" },
        { "247", "int", "International" },
        { "248", "mil", "US Military" },
        { "249", "net", "Network" },
        { "250", "org", "Non-Profit Organization" },
        { "251", "arpa", "Old style Arpanet" },
        { "252", "nato", "Nato field" },
        
    };
    
    /**
     * Returns the location code for the given host/domain name string.
     */
    public static int getLocationCode(String host) {
        host = host.trim();
        // Fetch the host's tld
        String[] hostparts = host.split("\\.");
        String tld = hostparts[hostparts.length - 1];
        for (int i = 0 ; i < cCodes.length ; i++) {
            if (cCodes[i][1].equals(tld)) {
                int code = 0;
                try {
                    code = Integer.valueOf(cCodes[i][0]).intValue();
                    if (logger.isDebugEnabled()) {
                        logger.debug("LocationCode: Code for " + tld + " is " + code);
                    }
                } catch (NumberFormatException e) {
                    logger.error("LocationCode: Internal Error: " + e.getMessage());
                }
                return code;
            }
        }
        logger.warn("LocationCode: Code for " + tld + " not found.");
        return 0;
    }
    
    /**
     * Returns the full name for a given location code.
     */
    public static String getLocation(int locationCode) {
        return getLocation((new Integer(locationCode)).toString());
    }
    
    /**
     * Returns the full name for a given location code.
     */
    public static String getLocation(String locationCode) {
        locationCode = locationCode.toLowerCase();
        for (int i=0 ; i < cCodes.length ; i++) {
            if (cCodes[i][0].equals(locationCode)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("LocationCode: Found String for code "
                    + locationCode + ": " + cCodes[i][2]);
                }
                return cCodes[i][2];
            }
        }
        logger.warn("Could not find location for code " + locationCode);
        return "(unspecified location)";
    }
    
}
