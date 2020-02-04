package nl.th8.presidium.archive.controller.dto;

public class FilterDTO {
    private String filterString;

    public FilterDTO(String filterString) {
        this.filterString = filterString;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }
}
