package nl.th8.presidium.scheduler.service;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.SubmissionKind;
import net.dean.jraw.references.SubmissionReference;
import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.data.KamerstukRepository;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import nl.th8.presidium.scheduler.controller.dto.Notification;
import org.apache.tomcat.jni.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

@Service
public class KamerstukkenService {

    PriorityQueue<Kamerstuk> postList;

    private Logger logger = LoggerFactory.getLogger(KamerstukkenService.class);

    @Autowired
    KamerstukRepository kamerstukRepository;

    @Autowired
    RedditSupplier supplier;

    @Autowired
    NotificationService notificationService;

    @Scheduled(fixedRate = 300000)
    public void postQueuedKamerstukken() {
        PriorityQueue<Kamerstuk> queueToPost = kamerstukRepository.findAllByPostDateIsBefore(new Date());

        logger.info("Checking for kamerstukken to post at {}", new Date().toString());

        while(queueToPost.size() > 0) {
            Kamerstuk toPost = queueToPost.poll();
            postKamerstuk(toPost);
            logger.info("Posting kamerstuk with identifier: {}", toPost.getCallsign());
            notificationService.addNotification(new Notification(String.format("Kamerstuk met identificator: %s is gepost.", toPost.getCallsign()),
                    String.format("Gepost op: %s", new Date().toString())));
            kamerstukRepository.delete(toPost);
        }
    }

    public List<Kamerstuk> getNonScheduledKamerstukken() {
        return kamerstukRepository.findAllByPostDateIsNull();
    }

    public PriorityQueue<Kamerstuk> getKamerstukkenQueue() {
        return kamerstukRepository.findAllByPostDateIsAfter(new Date());
    }

    public String queueKamerstuk(Kamerstuk kamerstuk, String mod) {
        //Process kamerstuk data
        kamerstuk.processToCallString();
        kamerstuk.processCallsigns();

        //Save kamerstuk to MongoDB
        Kamerstuk kamerstukSaved = kamerstukRepository.save(kamerstuk);

        //Notify
        notificationService.addNotification(new Notification(String.format(Constants.QUEUED_TITLE, kamerstuk.getCallsign()),
                String.format(Constants.QUEUED_TEXT, kamerstuk.getPostDateAsString(), mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.QUEUED_SUBJECT, String.format(Constants.QUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getPostDateAsString()));
        }
        return kamerstukSaved.getId();
    }

    public void rescheduleKamerstuk(String id, Date postDate, String mod) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(id);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setPostDateFromDate(postDate);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.REQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.REQUEUED_TEXT, mod)));
        if (kamerstuk.getSubmittedBy() != null) {
            supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.REQUEUED_SUBJECT, String.format(Constants.REQUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getPostDateAsString()));
        }


    }

    public void dequeueKamerstuk(String kamerstukId, String reason, String mod) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(kamerstukId);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setReason(reason);
        kamerstuk.unsetPostDate();
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.DEQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.DEQUEUED_TEXT, mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.DEQUEUED_SUBJECT, String.format(Constants.DEQUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getReason()));
        }
    }

    public void denyKamerstuk(String kamerstukId, String reason, String mod) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(kamerstukId);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstukRepository.delete(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.DENIED_TITLE, kamerstuk.getTitle()), String.format(Constants.DENIED_TEXT, mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.DENIED_SUBJECT, String.format(Constants.DENIED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), reason));
        }
    }

    private void postKamerstuk(Kamerstuk kamerstuk) {
        String title;
        if(kamerstuk.getCallsign() != null) {
            title = String.format("%s: %s", kamerstuk.getCallsign(), kamerstuk.getTitle());
        } else {
            title = kamerstuk.getTitle();
        }
        String content = String.format("##%s \n \n %s", kamerstuk.getTitle(), kamerstuk.getContent());
        SubmissionReference submission = supplier.redditClient.subreddit(Constants.SUBREDDIT).submit(SubmissionKind.SELF, title, content, false);
        if(kamerstuk.getToCall().size() > 0) {
            StringBuilder replyBuilder = new StringBuilder();
            replyBuilder.append("###Voor een reactie op dit kamerstuk wordt opgeroepen:  ").append("\n");
            for (String minister : kamerstuk.getToCall()) {
                replyBuilder.append("* ").append(minister).append("  ").append("\n");
            }
            Comment comment = submission.reply(replyBuilder.toString());
            comment.toReference(supplier.redditClient).distinguish(DistinguishedStatus.MODERATOR, true);
        }
    }
}
