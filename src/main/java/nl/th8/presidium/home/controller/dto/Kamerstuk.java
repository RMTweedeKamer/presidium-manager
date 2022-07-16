package nl.th8.presidium.home.controller.dto;

import nl.th8.presidium.Constants;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Kamerstuk implements Comparable<Kamerstuk> {

    @Id
    private String id;

    private String secret;

    private KamerstukType type;

    private String callsign;

    private String title;

    private String bundleTitle;

    private String content;

    private boolean urgent;

    private List<String> toCall;

    private String toCallString;

    private Date postDate;

    private Date voteDate;

    private String submittedBy;

    private String reason;

    private boolean posted;

    private boolean votePosted;

    private boolean denied;

    private boolean voteProcessed;

    private String url;

    private int readingLength;

    private String advice;

    private boolean isBundled;

    private Map<String, VoteType> voteMap;

    public Kamerstuk() {
        this.toCall = new ArrayList<>();
        this.posted = false;
        this.votePosted = false;
        this.denied = false;
        this.voteProcessed = false;
        this.readingLength = 3;
        this.voteMap = new HashMap<>();
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

    public String getCallnumber() {
        int prefixLength = this.type.getCall().length();
        return callsign.substring(prefixLength);
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getTitle() {
        return title;
    }

    public void setBundleTitle(String bundleTitle) {
        this.bundleTitle = bundleTitle;
    }

    public String getBundleTitle() {
        return bundleTitle;
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

    public void setPostDateFromDate(Date datetime) {
        this.postDate = datetime;
    }

    public void unsetPostDate() {
        this.postDate = null;
    }

    public Date getVoteDate() {
        return voteDate;
    }

    public String getVoteDateAsString() {
        if(this.voteDate != null) {
            DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, new Locale("nl", "NL"));
            return format.format(this.voteDate);
        }
        return "N.v.t.";
    }

    public void setVoteDate(String datetime) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.voteDate = formatter.parse(datetime);
    }

    public void setVoteDateFromDate(Date datetime) {
        this.voteDate = datetime;
    }

    public void unsetVoteDate() {
        this.voteDate = null;
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
        if(this.toCallString != null && !this.toCallString.isEmpty()) {
            this.toCall = Arrays.asList(this.toCallString.split(";"));
        }
    }

    public String getToCallAsText() {
        StringBuilder builder = new StringBuilder();
        for(String callee : this.toCall) {
            builder.append(callee).append("</br>");
        }
        return builder.toString();
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void processCallsigns() {
        switch(type) {
            case RESULTATEN:
            case STEMMING:
                this.callsign = type.getName();
                break;
            default:
                break;
        }
    }

    public boolean isPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

    public boolean isVotePosted() {
        return votePosted;
    }

    public void setVotePosted(boolean votePosted) {
        this.votePosted = votePosted;
    }

    public boolean isDenied() {
        return denied;
    }

    public void setDenied(boolean denied) {
        this.denied = denied;
    }

    public boolean isVoteProcessed() {
        return voteProcessed;
    }

    public void setVoteProcessed(boolean voteProcessed) {
        this.voteProcessed = voteProcessed;
    }

    public String getUrl() {
        return url;
    }

    public boolean hasUrl() {
        return this.url != null && !this.url.isEmpty();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getReadingLength() {
        return readingLength;
    }

    public void setReadingLength(int readingLength) {
        this.readingLength = readingLength;
    }

    public String getReadLengthString() {
        Calendar c = Calendar.getInstance();
        c.setTime(this.postDate);
        c.add(Calendar.DATE, this.readingLength);
        Date endReading = c.getTime();

        DateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT, new Locale("nl", "NL"));

        return String.format("Deze lezing loopt tot en met %s", format.format(endReading));
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public boolean isBundled() {
        return isBundled;
    }

    public void setBundled(boolean bundled) {
        isBundled = bundled;
    }

    public Map<String, VoteType> getVoteMap() {
        return voteMap;
    }

    public void setVoteMap(Map<String, VoteType> voteMap) {
        this.voteMap = voteMap;
    }

    public String getResultFromVoteMap() {
        StringBuilder resultBuilder = new StringBuilder();
        final long[] votesFor = new long[1];
        final long[] votesAgainst = new long[1];
        for(VoteType voteType : VoteType.values()) {
            long count = voteMap.values().stream().filter(type -> type == voteType).count();
            resultBuilder.append(voteType.getName()).append(": ").append(count).append("  \n");
            if (voteType == VoteType.VOOR)
                votesFor[0] = count;
            else if (voteType == VoteType.TEGEN)
                votesAgainst[0] = count;
        }

        if(votesFor[0] > votesAgainst[0]) {
            resultBuilder.append("\nDeze ").append(type.getName().toLowerCase()).append(" is aangenomen");
            if(type == KamerstukType.WET)
                resultBuilder.append(", en gaat door naar de Eerste Kamer. \n");
            else
                resultBuilder.append(". \n");
        } else {
            resultBuilder.append("\nDeze ").append(type.getName().toLowerCase()).append(" is afgewezen. \n");
        }

        return resultBuilder.toString();
    }

    public String getTurnoutForKamerstuk() {
        long validVotes = voteMap.values().stream().filter(voteType -> voteType != VoteType.NG).count();

        return "De opkomst was: " + BigDecimal.valueOf(validVotes / (float) voteMap.values().size() * 100).setScale(2, RoundingMode.HALF_UP) + "%\n";
    }
}
