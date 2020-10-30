package nl.rmtk.presidium.archive.controller.dto;

@SuppressWarnings("unused")
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
