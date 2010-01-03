

package net.sf.odinms.server.maps;

public enum SavedLocationType {
    FREE_MARKET, WORLDTOUR, FLORINA, CYGNUSINTRO, DOJO;

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }
}
