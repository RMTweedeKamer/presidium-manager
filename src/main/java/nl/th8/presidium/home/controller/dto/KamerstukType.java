package nl.th8.presidium.home.controller.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;

public enum KamerstukType {
    MOTIE ("Motie", "M", true),
    WET ("Wet", "W", true),
    AMENDEMENT ("Amendement", "W", true),
    BRIEF ("Kamerbrief", "KS", true),
    BESLUIT ("Koninklijk Besluit", "KB", true),
    DEBAT ("Debat", "DB", true),
    VRAGEN ("Kamervragen", "KV", true),
    RESULTATEN ("Resultaten", "", false),
    STEMMING ("Stemming", "", false);

    @JsonValue
    private final String name;
    private final String call;
    private final boolean selectable;

    KamerstukType(String name, String call, boolean selectable) {
        this.name = name;
        this.call = call;
        this.selectable = selectable;
    }

    public static List<KamerstukType> getPublics() {
        List<KamerstukType> publics = new ArrayList<>();
        for(KamerstukType kamerstukType : KamerstukType.values()) {
            if(kamerstukType.selectable) {
                publics.add(kamerstukType);
            }
        }
        return publics;
    }

    public String getName() {
        return name;
    }

    public String getCall() {
        return call;
    }
}
