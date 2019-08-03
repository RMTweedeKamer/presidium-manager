package nl.th8.presidium.home.controller.dto;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Kamerstuk implements Comparable<Kamerstuk> {

    @Id
    private String id;

    private KamerstukType type;

    private String callsign;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private boolean urgent;

    private List<Minister> toCall;

    private Date postDate;

    public Kamerstuk() {
        this.toCall = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public KamerstukType getType() {
        return type;
    }

    public void setType(KamerstukType type) {
        this.type = type;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
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

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(String datetime) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.postDate = formatter.parse(datetime);
    }

    @Override
    public int compareTo(@org.jetbrains.annotations.NotNull Kamerstuk k2) {
        if(this.postDate.after(k2.postDate)) {
            return 1;
        }
        else if(this.postDate.equals(k2.postDate)) {
            return 0;
        }
        else {
            return -1;
        }
    }
}
