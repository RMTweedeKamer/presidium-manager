package nl.th8.presidium.home.controller.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;

public enum KamerstukType {
    MOTIE ("Motie", "M", true, true),
    WET ("Wet", "W", true, true),
    AMENDEMENT ("Amendement", "W", true, true),
    BRIEF ("Kamerbrief", "KS", true, false),
    BESLUIT ("Koninklijk Besluit", "KB", true, false),
    DEBAT ("Debat", "DB", true, false),
    VRAGEN ("Kamervragen", "KV", true, false),
    RESULTATEN ("Resultaten", "", false, false),
    STEMMING ("Stemming", "", false, false);

    @JsonValue
    private final String name;
    private final String call;
    private final boolean selectable;
    private final boolean needsVote;

    KamerstukType(String name, String call, boolean selectable, boolean needsVote) {
        this.name = name;
        this.call = call;
        this.selectable = selectable;
        this.needsVote = needsVote;
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

    public boolean needsVote() {
        return needsVote;
    }
}
