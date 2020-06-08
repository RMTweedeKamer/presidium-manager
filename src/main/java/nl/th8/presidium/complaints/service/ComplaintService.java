package nl.th8.presidium.complaints.service;

import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.TemmieSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComplaintService {

    @Autowired
    RedditSupplier redditSupplier;

    @Autowired
    TemmieSupplier temmieSupplier;

    public void sendComplaint(String complaint, String link) {
        redditSupplier.inbox.compose("/r/" + RedditSupplier.SUBREDDIT, Constants.COMPLAINT_SUBJECT, String.format(Constants.COMPLAINT_BODY, complaint, link));
        temmieSupplier.complaintEmbeddedMessage(complaint, link);
    }
}
