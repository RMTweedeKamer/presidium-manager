package nl.th8.presidium.scheduler.service;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.SubmissionKind;
import net.dean.jraw.references.SubmissionReference;
import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.controller.dto.StatDTO;
import nl.th8.presidium.home.data.KamerstukRepository;
import nl.th8.presidium.scheduler.InvalidUsernameException;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import nl.th8.presidium.scheduler.controller.dto.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        PriorityQueue<Kamerstuk> queueToPost = kamerstukRepository.findAllByPostDateIsBeforeAndPostedIsFalseAndDeniedIsFalse(new Date());

        Date checktime = new Date();
        logger.info("Checking for kamerstukken to post at {}", checktime.toString());
        StatDTO.setLastToPostCheck(checktime);

        while(queueToPost.size() > 0) {
            Kamerstuk toPost = queueToPost.poll();
            postKamerstuk(toPost);
            if(toPost.getCallsign() != null) {
                logger.info("Posting kamerstuk with identifier: {}", toPost.getCallsign());
                notificationService.addNotification(new Notification(String.format("Kamerstuk met identificator: %s is gepost.", toPost.getCallsign()),
                        String.format("Gepost op: %s", new Date().toString())));
            } else {
                logger.info("Posting kamerstuk of type: {}", toPost.getType().getName());
                notificationService.addNotification(new Notification(String.format("Kamerstuk van het type: %s is gepost.", toPost.getType().getName()),
                        String.format("Gepost op: %s", new Date().toString())));
            }

        }
    }

    @Scheduled(cron = "0 0 12 ? * FRI")
    public void postVote() {
        List<Kamerstuk> kamerstukkenToCheck = kamerstukRepository.findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull();

        Date currentDate = new Date();
        logger.info("Checking for vote to post at {}", currentDate.toString());
        StatDTO.setLastToVoteCheck(currentDate);

        Predicate<Kamerstuk> onSameDay = kamerstuk -> DateUtils.isSameDay(kamerstuk.getVoteDate(), currentDate);
        Predicate<Kamerstuk> dayHasPassed = kamerstuk -> kamerstuk.getVoteDate().before(currentDate);
        kamerstukkenToCheck = kamerstukkenToCheck.stream()
                .filter(onSameDay.or(dayHasPassed))
                .sorted(Comparator.comparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
        kamerstukkenToCheck
                .forEach(kamerstuk -> {
                    kamerstuk.setVotePosted(true);
                    kamerstukRepository.save(kamerstuk);
                });

        if(kamerstukkenToCheck.size() > 0) {
            constructVotePost(kamerstukkenToCheck);
        }
    }

    public List<Kamerstuk> getNonScheduledKamerstukken() {
        return kamerstukRepository.findAllByPostDateIsNullAndDeniedIsFalse();
    }

    public List<Kamerstuk> getKamerstukkenQueue() {
        return kamerstukRepository.findAllByPostDateIsAfterAndDeniedIsFalse(new Date()).stream()
                .sorted(Comparator.comparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getDelayedKamerstukkenVoteQueue() {
        return kamerstukRepository.findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNull().stream()
                .sorted(Comparator.comparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getKamerstukkenVoteQueue() {
        return kamerstukRepository.findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull().stream()
                .sorted(Comparator.comparing(Kamerstuk::getVoteDate))
                .collect(Collectors.toList());
    }

    public String queueKamerstuk(Kamerstuk kamerstuk, String mod) throws InvalidUsernameException {
        //Process kamerstuk data
        kamerstuk.processToCallString();
        kamerstuk.processCallsigns();

        //Save kamerstuk to MongoDB
        Kamerstuk kamerstukSaved = kamerstukRepository.save(kamerstuk);

        //Notify
        notificationService.addNotification(new Notification(String.format(Constants.QUEUED_TITLE, kamerstuk.getCallsign()),
                String.format(Constants.QUEUED_TEXT, kamerstuk.getPostDateAsString(), mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            try {
                supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.QUEUED_SUBJECT, String.format(Constants.QUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getPostDateAsString()));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }
        return kamerstukSaved.getId();
    }

    public void rescheduleKamerstuk(String id, Date postDate, Date voteDate, String mod) throws KamerstukNotFoundException, InvalidUsernameException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(id);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setPostDateFromDate(postDate);
        kamerstuk.setVoteDateFromDate(voteDate);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.REQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.REQUEUED_TEXT, mod)));
        if (kamerstuk.getSubmittedBy() != null) {
            try {
                supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.REQUEUED_SUBJECT, String.format(Constants.REQUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getPostDateAsString()));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }
    }

    public void rescheduleVote(String id, Date voteDate, String mod) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(id);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setVoteDateFromDate(voteDate);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.REQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.REQUEUED_TEXT, mod)));
    }

    public void dequeueKamerstuk(String kamerstukId, String reason, String mod) throws KamerstukNotFoundException, InvalidUsernameException {
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
            try {
                supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.DEQUEUED_SUBJECT, String.format(Constants.DEQUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getReason()));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }
    }

    public void denyKamerstuk(String kamerstukId, String reason, String mod) throws KamerstukNotFoundException, InvalidUsernameException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(kamerstukId);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setDenied(true);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.DENIED_TITLE, kamerstuk.getTitle()), String.format(Constants.DENIED_TEXT, mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            try {
                supplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.DENIED_SUBJECT, String.format(Constants.DENIED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), reason));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }
    }

    public void withdrawKamerstuk(String kamerstukId, String mod) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(kamerstukId);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setDenied(true);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.WITHDRAW_TITLE, kamerstuk.getTitle()), String.format(Constants.WITHDRAW_TEXT, mod)));
    }

    public void delayKamerstuk(String kamerstukId, String mod) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(kamerstukId);
        Kamerstuk kamerstuk;
        if(possibleKamerstuk.isPresent()) {
            kamerstuk = possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }

        kamerstuk.setVoteDateFromDate(null);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.DELAY_TITLE, kamerstuk.getTitle()), String.format(Constants.DELAY_TEXT, mod)));
    }

    private void postKamerstuk(Kamerstuk kamerstuk) {
        String title;
        if(kamerstuk.getCallsign() != null) {
            title = String.format("%s: %s", kamerstuk.getCallsign(), kamerstuk.getTitle());
        } else {
            title = kamerstuk.getTitle();
        }
        String content = String.format("##%s \n \n%s \n \n##%s", kamerstuk.getTitle(), kamerstuk.getContent(), kamerstuk.getReadLengthString());
        SubmissionReference submission = supplier.redditClient.subreddit(Constants.SUBREDDIT).submit(SubmissionKind.SELF, title, content, false);
        kamerstuk.setUrl("https://reddit.com/r/"+Constants.SUBREDDIT+"/comments/"+submission.getId());
        kamerstuk.setPosted(true);
        if(!kamerstuk.getType().needsVote()) {
            kamerstuk.setVotePosted(true);
        }
        kamerstukRepository.save(kamerstuk);
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

    private void constructVotePost(List<Kamerstuk> votesToPost) {
        StringBuilder title = new StringBuilder();
        StringBuilder links = new StringBuilder();
        StringBuilder format = new StringBuilder();
        StringBuilder content = new StringBuilder();
        Kamerstuk newVote = new Kamerstuk();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 3);
        newVote.setVoteDateFromDate(c.getTime());
        title.append("Stemming Tweede Kamer over ");

        String prefix = "";
        for(Kamerstuk kamerstuk : votesToPost) {
            title.append(prefix).append(kamerstuk.getCallsign());
            prefix = ", ";
            links.append("[").append(kamerstuk.getCallsign()).append(": ").append(kamerstuk.getTitle()).append("](").append(kamerstuk.getUrl()).append(")  \n");
            format.append(kamerstuk.getCallsign()).append(":  \n");
        }
        content.append("Leden van de Tweede Kamer der Staten-Generaal,\n\n");
        content.append("**U kunt op de volgende kamerstuk(ken) uw stem uitbrengen:**  \n");
        content.append(links.toString());
        content.append("\n---\n");
        content.append("**Hanteer a.u.b. het volgende format:**\n\n");
        content.append("Telkens met twee spaties achter de regel\n");
        content.append("Voor/Tegen/Onthouden:\n\n");
        content.append(format.toString());
        content.append("\n---\n");
        content.append("###Deze stemming sluit op: ").append(newVote.getVoteDateAsString()).append("\n");
        content.append("**Let op: stemmen na deze datum zijn ongeldig**");

        newVote.setType(KamerstukType.STEMMING);
        newVote.setTitle(title.toString());
        newVote.setContent(content.toString());
        newVote.setUrgent(false);
        newVote.setPostDateFromDate(new Date());
        newVote.setVotePosted(true);
        kamerstukRepository.save(newVote);
    }
}
