package nl.th8.presidium.home.controller.dto;

import java.util.Date;

public class StatDTO {

    private static Date startDate;
    private static Date lastToPostCheck;
    private static Date lastToVoteCheck;
    private long kamerstukken;
    private long queue;
    private long queueVote;
    private long denied;
    private long withdrawn;
    private long succesfullyPosted;
    private String javaVersion;

    public StatDTO(long kamerstukken, long queue, long queueVote, long denied, long withdrawn, long succesfullyPosted) {
        this.kamerstukken = kamerstukken;
        this.queue = queue;
        this.queueVote = queueVote;
        this.denied = denied;
        this.withdrawn = withdrawn;
        this.succesfullyPosted = succesfullyPosted;
        this.javaVersion = System.getProperty("java.version");
    }

    public static Date getStartDate() {
        return startDate;
    }

    public static void setStartDate(Date startDate) {
        StatDTO.startDate = startDate;
    }

    public static Date getLastToPostCheck() {
        return lastToPostCheck;
    }

    public static void setLastToPostCheck(Date lastToPostCheck) {
        StatDTO.lastToPostCheck = lastToPostCheck;
    }

    public static Date getLastToVoteCheck() {
        return lastToVoteCheck;
    }

    public static void setLastToVoteCheck(Date lastToVoteCheck) {
        StatDTO.lastToVoteCheck = lastToVoteCheck;
    }

    public long getKamerstukken() {
        return kamerstukken;
    }

    public void setKamerstukken(long kamerstukken) {
        this.kamerstukken = kamerstukken;
    }

    public long getQueue() {
        return queue;
    }

    public void setQueue(long queue) {
        this.queue = queue;
    }

    public long getQueueVote() {
        return queueVote;
    }

    public void setQueueVote(long queueVote) {
        this.queueVote = queueVote;
    }

    public long getDenied() {
        return denied;
    }

    public void setDenied(long denied) {
        this.denied = denied;
    }

    public long getWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(long withdrawn) {
        this.withdrawn = withdrawn;
    }

    public long getSuccesfullyPosted() {
        return succesfullyPosted;
    }

    public void setSuccesfullyPosted(long succesfullyPosted) {
        this.succesfullyPosted = succesfullyPosted;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
}
