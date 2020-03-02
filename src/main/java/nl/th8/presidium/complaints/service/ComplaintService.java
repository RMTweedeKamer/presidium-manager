package nl.th8.presidium.complaints.service;

import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComplaintService {

    @Autowired
    RedditSupplier supplier;

    public void sendComplaint(String complaint) {
        supplier.inbox.compose("/r/" + RedditSupplier.SUBREDDIT, Constants.COMPLAINT_SUBJECT, complaint);

    }
}
