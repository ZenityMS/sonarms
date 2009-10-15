package client;

public class MapleCharacterUtil {
    public static boolean canCreateChar(String name, int world) {
        if (name.length() < 4 || name.length() > 12)
            return false;
        if (java.util.regex.Pattern.compile("[a-zA-Z0-9_-]{3,12}").matcher(name).matches())
            return MapleCharacter.getIdByName(name, world) < 0 && !name.toLowerCase().contains("gm");
        return false;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
    }
}