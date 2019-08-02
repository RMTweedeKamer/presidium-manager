package nl.th8.presidium.home.controller.dto;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class Kamerstuk {

    @Id
    private String submit_id;
    private KamerstukType type;
    private String identifier;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private boolean urgent;
    private List<Minister> toCall;

    public Kamerstuk() {
        this.toCall = new ArrayList<>();
    }

    public String getSubmit_id() {
        return submit_id;
    }

    public void setSubmit_id(String submit_id) {
        this.submit_id = submit_id;
    }

    public KamerstukType getType() {
        return type;
    }

    public void setType(KamerstukType type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public List<Minister> getToCall() {
        return toCall;
    }

    public void setToCall(List<Minister> toCall) {
        this.toCall = toCall;
    }

    public void addCall(Minister minister) {
        this.toCall.add(minister);
    }
}
