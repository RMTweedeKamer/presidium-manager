package nl.th8.presidium.home.controller.dto;

import org.apache.commons.lang3.StringUtils;

public enum VoteType {

    VOOR("Voor", "Voor"),
    TEGEN("Tegen", "Tegen"),
    SO("Stem onthouden", "SO"),
    NG("Niet gestemd", "NG");

    private String name;

    private String spreadsheetValue;

    VoteType(String name, String spreadsheetValue) {
        this.name = name;
        this.spreadsheetValue = spreadsheetValue;
    }

    public static VoteType fromString(String nextPart) {
        if(StringUtils.containsIgnoreCase(nextPart, "voor") && !StringUtils.containsIgnoreCase(nextPart, "tegen") && !StringUtils.containsIgnoreCase(nextPart, "onthouden"))
            return VOOR;
        else if (StringUtils.containsIgnoreCase(nextPart, "tegen") && !StringUtils.containsIgnoreCase(nextPart, "voor") && !StringUtils.containsIgnoreCase(nextPart, "onthouden"))
            return TEGEN;
        else if (StringUtils.containsIgnoreCase(nextPart, "onthouden") && !StringUtils.containsIgnoreCase(nextPart, "voor") && !StringUtils.containsIgnoreCase(nextPart, "tegen"))
            return SO;
        else
            return NG;

    }

    public String getName() {
        return name;
    }

    public String getSpreadsheetValue() {
        return spreadsheetValue;
    }
}
