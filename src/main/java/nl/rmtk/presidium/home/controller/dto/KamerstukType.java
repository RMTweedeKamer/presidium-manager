package nl.th8.presidium.home.controller.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import nl.th8.presidium.scheduler.InvalidCallsignException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum KamerstukType {
    MOTIE ("Motie", "M", true, true, false),
    WET ("Wet", "W", true, true, true),
    AMENDEMENT ("Amendement", "W", true, true, true),
    BRIEF ("Kamerbrief", "KS", true, false, false),
    BESLUIT ("Koninklijk Besluit", "KB", true, false, true),
    DEBAT ("Debat", "DB", true, false, false),
    VRAGEN ("Kamervragen", "KV", true, false, false),
    RESULTATEN ("Resultaten", "", false, false, false),
    STEMMING ("Stemming", "", false, false, false);

    @JsonValue
    private final String name;
    private final String call;
    private final boolean selectable;
    private final boolean needsVote;
    private final boolean forRvS;

    KamerstukType(String name, String call, boolean selectable, boolean needsVote, boolean forRvS) {
        this.name = name;
        this.call = call;
        this.selectable = selectable;
        this.needsVote = needsVote;
        this.forRvS = forRvS;
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

    public static KamerstukType getByLetters(String letters) throws InvalidCallsignException {
        for(KamerstukType k : KamerstukType.getPublics()) {
            if(letters.equals(k.getCall()))
                return k;
        }
        throw new InvalidCallsignException();
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

    public boolean forRvS() { return forRvS; }
}
