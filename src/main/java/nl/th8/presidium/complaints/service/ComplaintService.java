package nl.th8.presidium.complaints.service;

import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.TemmieSupplier;
import nl.th8.presidium.complaints.InvalidComplaintException;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComplaintService {

    private final RedditSupplier redditSupplier;

    private final TemmieSupplier temmieSupplier;

    @SuppressWarnings("FieldCanBeLocal")
    private final int discordFieldMaxLength = 1024;

    @Autowired
    public ComplaintService(RedditSupplier redditSupplier, TemmieSupplier temmieSupplier) {
        this.redditSupplier = redditSupplier;
        this.temmieSupplier = temmieSupplier;
    }

    public void sendComplaint(String complaint, String link) throws InvalidComplaintException {
        UrlValidator validator = new UrlValidator();
        if((validator.isValid(link) || link.isEmpty()) && complaint.length() < discordFieldMaxLength && !redditSupplier.doNotPost) {
            redditSupplier.inbox.compose("/r/" + RedditSupplier.SUBREDDIT, Constants.COMPLAINT_SUBJECT, String.format(Constants.COMPLAINT_BODY, complaint, link));
            temmieSupplier.complaintEmbeddedMessage(complaint, link);
        }
        else {
            throw new InvalidComplaintException();
        }
    }
}
