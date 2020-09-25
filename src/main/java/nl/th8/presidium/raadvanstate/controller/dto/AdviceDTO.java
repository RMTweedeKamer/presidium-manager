package nl.th8.presidium.raadvanstate.controller.dto;

public class AdviceDTO {

    private String kamerstukId;
    private String advice;
    private boolean sendNotification;

    public String getKamerstukId() {
        return kamerstukId;
    }

    public void setKamerstukId(String kamerstukId) {
        this.kamerstukId = kamerstukId;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public boolean sendNotification() {
        return sendNotification;
    }

    public void setSendNotification(boolean sendNotification) {
        this.sendNotification = sendNotification;
    }
}
