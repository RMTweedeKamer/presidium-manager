package nl.th8.presidium.home.controller.dto;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        return Arrays.stream(KamerstukType.values())
                .filter(kamerstukType -> kamerstukType.selectable)
                .collect(Collectors.toList());
    }

    public static boolean isPublicByCall(String call) {
        for(KamerstukType k : KamerstukType.getPublics()) {
            if(call.equals(k.getCall()))
                return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getCall() {
        return call;
    }

    public boolean isSelectable() { return selectable; }

    public boolean needsVote() {
        return needsVote;
    }
}
