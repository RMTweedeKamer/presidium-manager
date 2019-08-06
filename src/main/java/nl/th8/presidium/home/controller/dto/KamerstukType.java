package nl.th8.presidium.home.controller.dto;

public enum KamerstukType {
    MOTIE ("Motie", "M"),
    WET ("Wet", "W"),
    AMENDEMENT ("Amendement", "W"),
    BRIEF ("Kamerbrief", "KS"),
    BESLUIT ("Koninklijk Besluit", "KB"),
    DEBAT ("Debat", "DB"),
    VRAGEN ("Kamervragen", "KV");

    private final String name;
    private final String call;

    KamerstukType(String name, String call) {
        this.name = name;
        this.call = call;
    }

    public String getName() {
        return name;
    }

    public String getCall() {
        return call;
    }
}
