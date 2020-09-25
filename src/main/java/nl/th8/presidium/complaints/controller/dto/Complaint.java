package nl.th8.presidium.complaints.controller.dto;

@SuppressWarnings("unused")
public class Complaint {

    private String complaintText;
    private String messageLink;

    public String getComplaintText() {
        return complaintText;
    }

    public void setComplaintText(String complaintText) {
        this.complaintText = complaintText;
    }

    public String getMessageLink() {
        return messageLink;
    }

    public void setMessageLink(String messageLink) {
        this.messageLink = messageLink;
    }
}
