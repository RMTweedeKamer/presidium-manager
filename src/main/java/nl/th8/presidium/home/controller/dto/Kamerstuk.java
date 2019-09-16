package nl.th8.presidium.home.controller.dto;

import nl.th8.presidium.Constants;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private List<String> toCall;

    private String toCallString;

    private Date postDate;

    private String submittedBy;

    private String reason;

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

    public List<String> getToCall() {
        return toCall;
    }

    public void setToCall(List<String> toCall) {
        this.toCall = toCall;
    }

    public void addCall(String minister) {
        this.toCall.add(minister);
    }

    public Date getPostDate() {
        return postDate;
    }

    public String getPostDateAsString() {
        DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, new Locale("nl", "NL"));
        return format.format(this.postDate);
    }

    public void setPostDate(String datetime) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.postDate = formatter.parse(datetime);
    }

    public void unsetPostDate() {
        this.postDate = null;
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

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getToCallString() {
        return toCallString;
    }

    public void setToCallString(String toCallString) {
        this.toCallString = toCallString;
    }

    public void processToCallString() {
        this.toCall = Arrays.asList(this.toCallString.split(";"));
    }
}
