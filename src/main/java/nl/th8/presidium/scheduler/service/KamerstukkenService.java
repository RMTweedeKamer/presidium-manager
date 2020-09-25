package nl.th8.presidium.scheduler.service;

import net.dean.jraw.models.*;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubmissionReference;
import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.TemmieSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.controller.dto.StatDTO;
import nl.th8.presidium.home.data.KamerstukRepository;
import nl.th8.presidium.scheduler.DuplicateCallsignException;
import nl.th8.presidium.scheduler.InvalidCallsignException;
import nl.th8.presidium.scheduler.InvalidUsernameException;
import nl.th8.presidium.scheduler.KamerstukNotFoundException;
import nl.th8.presidium.scheduler.controller.dto.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class KamerstukkenService {

    private final Logger logger = LoggerFactory.getLogger(KamerstukkenService.class);

    private final KamerstukRepository kamerstukRepository;

    private final RedditSupplier redditSupplier;

    private final NotificationService notificationService;

    private final TemmieSupplier discordWebhooks;

    @Autowired
    public KamerstukkenService(KamerstukRepository kamerstukRepository, RedditSupplier redditSupplier, NotificationService notificationService, TemmieSupplier discordWebhooks) {
        this.kamerstukRepository = kamerstukRepository;
        this.redditSupplier = redditSupplier;
        this.notificationService = notificationService;
        this.discordWebhooks = discordWebhooks;
    }

    //One time function to export all kamerstukken to md files
//    @PostConstruct
//    public void exportToMarkdown() throws IOException {
//        List<Kamerstuk> toOut = kamerstukRepository.findAllByPostedIsTrue();
//        for(Kamerstuk kamerstuk : toOut) {
//            File newFile = new File(kamerstuk.getType().getName(), kamerstuk.getCallsign() + ".md");
//            new File(kamerstuk.getType().getName()).mkdirs();
//            newFile.createNewFile();
//            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
//            writer.write(String.format("##%s \n \n%s", kamerstuk.getTitle(), kamerstuk.getContent()));
//            writer.close();
//        }
//    }

    //One time function to get all old legislation from reddit.
    @SuppressWarnings("unused")
    public void importFromReddit() throws ParseException, InvalidCallsignException {
        DefaultPaginator<Submission> paginator = redditSupplier.redditClient.subreddit(RedditSupplier.SUBREDDIT)
                .posts()
                .timePeriod(TimePeriod.ALL)
                .sorting(SubredditSort.NEW)
                .build();

        for (Listing<Submission> nextPage : paginator) {
            for (Submission post : nextPage) {
                if (!post.getCreated().before(new SimpleDateFormat("dd-MM-yyyy").parse("01-10-2018"))) {

                    String substring = post.getTitle().substring(0, 7);
                    int callsignlength = checkCallsignFormat(substring);

                    if (post.getTitle().equals("Wet aanpak uitval en probleemjongeren beroepsonderwijs"))
                        callsignlength = 5;

                    if (callsignlength > 0) {
                        String callsign = post.getTitle().substring(0, callsignlength);

                        if (!kamerstukRepository.existsByCallsign(callsign)) {
                            Kamerstuk nieuw = new Kamerstuk();
                            int letterlength;
                            if (callsignlength < 7)
                                letterlength = callsignlength - 4;
                            else
                                letterlength = 1;
                            nieuw.setType(KamerstukType.getByLetters(callsign.substring(0, letterlength)));
                            nieuw.setCallsign(callsign);
                            nieuw.setTitle(post.getTitle().substring(callsignlength + 1));
                            nieuw.setContent(post.getSelfText());
                            nieuw.setPostDateFromDate(post.getCreated());
                            nieuw.setSubmittedBy("GERDI-RMTK");
                            nieuw.setPosted(true);
                            nieuw.setVotePosted(true);
                            nieuw.setUrl(post.getUrl());

                            kamerstukRepository.save(nieuw);
                        }
                    }
                }
            }
        }
    }

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

    @Scheduled(cron = "0 0 12 ? * SAT")
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
                .sorted(Comparator.comparing(Kamerstuk::getPostDate))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getDelayedKamerstukkenVoteQueue() {
        return kamerstukRepository.findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNull().stream()
                .sorted(Comparator.comparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getKamerstukkenVoteQueue() {
        return kamerstukRepository.findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull().stream()
                .sorted(Comparator.comparing(Kamerstuk::getVoteDate).thenComparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getToukieQueue() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -15);
        Date date1 = c.getTime();
        c.add(Calendar.DATE, 30);
        Date date2 = c.getTime();
        return kamerstukRepository.findAllByPostDateIsBetweenAndCallsignIsNotNull(date1, date2).stream()
                .sorted(Comparator.comparing(Kamerstuk::getType).thenComparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getTh8Queue() {
        return kamerstukRepository.findAllByPostDateIsBeforeAndCallsignIsNotNull(new Date()).stream()
                .sorted(Comparator.comparing(Kamerstuk::getType).thenComparing(Kamerstuk::getCallsign))
                .collect(Collectors.toList());
    }

    public List<Kamerstuk> getRvSQueue() {
        Predicate<Kamerstuk> isRelevant = kamerstuk -> kamerstuk.getType().forRvS();

        return kamerstukRepository.findAllByPostDateIsAfterAndDeniedIsFalse(new Date()).stream()
                .filter(isRelevant)
                .sorted(Comparator.comparing(Kamerstuk::getPostDate))
                .collect(Collectors.toList());
    }

    public void queueKamerstuk(Kamerstuk kamerstuk, String mod) throws InvalidUsernameException, DuplicateCallsignException, InvalidCallsignException {
        //Check callsign
        if(kamerstukRepository.existsByCallsignAndIdIsNot(kamerstuk.getCallsign(), kamerstuk.getId())) {
            throw new DuplicateCallsignException();
        }
        if(!checkCallsignFormat(kamerstuk.getType(), kamerstuk.getCallsign())) {
            throw new InvalidCallsignException();
        }


        //Process kamerstuk data
        kamerstuk.processToCallString();
        kamerstuk.processCallsigns();

        //Save kamerstuk to MongoDB
        kamerstukRepository.save(kamerstuk);

        //Log to scheduler notifications
        notificationService.addNotification(new Notification(String.format(Constants.QUEUED_TITLE, kamerstuk.getCallsign()),
                String.format(Constants.QUEUED_TEXT, kamerstuk.getPostDateAsString(), mod)));

        //Notify submitter via reddit
        if(kamerstuk.getSubmittedBy() != null) {
            try {
                redditSupplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.QUEUED_SUBJECT, String.format(Constants.QUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getPostDateAsString()));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }

        //Notify RvS if necessary
        if(kamerstuk.getType().forRvS()) {
            discordWebhooks.rvsEmbeddedMessage(kamerstuk);
        }
    }

    public void editKamerstuk(String kamerstukId, String title, String content, String toCallString, String mod) throws KamerstukNotFoundException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setTitle(title);
        kamerstuk.setContent(content);
        kamerstuk.setToCallString(toCallString);
        kamerstuk.processToCallString();
        kamerstukRepository.save(kamerstuk);

        //Notify
        notificationService.addNotification(new Notification(String.format(Constants.EDIT_TITLE, kamerstuk.getCallsign()),
                String.format(Constants.EDIT_TEXT, mod)));
    }

    public void rescheduleKamerstuk(String kamerstukId, Date postDate, Date voteDate, String mod) throws KamerstukNotFoundException, InvalidUsernameException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setPostDateFromDate(postDate);
        kamerstuk.setVoteDateFromDate(voteDate);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.REQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.REQUEUED_TEXT, mod)));
        if (kamerstuk.getSubmittedBy() != null) {
            try {
                redditSupplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.REQUEUED_SUBJECT, String.format(Constants.REQUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getPostDateAsString()));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }
    }

    public void rescheduleVote(String kamerstukId, Date voteDate, String mod) throws KamerstukNotFoundException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setVoteDateFromDate(voteDate);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.REQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.REQUEUED_TEXT, mod)));
    }

    public void dequeueKamerstuk(String kamerstukId, String reason, String mod) throws KamerstukNotFoundException, InvalidUsernameException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setReason(reason);
        notificationService.addNotification(new Notification(String.format(Constants.DEQUEUED_TITLE, kamerstuk.getCallsign()), String.format(Constants.DEQUEUED_TEXT, mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            try {
                redditSupplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.DEQUEUED_SUBJECT, String.format(Constants.DEQUEUED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), kamerstuk.getReason()));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }

        kamerstuk.unsetPostDate();
        kamerstuk.setCallsign(null);
        kamerstukRepository.save(kamerstuk);
    }

    public void denyKamerstuk(String kamerstukId, String reason, String mod) throws KamerstukNotFoundException, InvalidUsernameException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setDenied(true);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.DENIED_TITLE, kamerstuk.getTitle()), String.format(Constants.DENIED_TEXT, mod)));
        if(kamerstuk.getSubmittedBy() != null) {
            try {
                redditSupplier.inbox.compose(kamerstuk.getSubmittedBy(), Constants.DENIED_SUBJECT, String.format(Constants.DENIED_BODY, kamerstuk.getSubmittedBy(), kamerstuk.getTitle(), reason));
            } catch (Exception e) {
                throw new InvalidUsernameException();
            }
        }
    }

    public void withdrawKamerstuk(String kamerstukId, String mod) throws KamerstukNotFoundException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setDenied(true);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.WITHDRAW_TITLE, kamerstuk.getTitle()), String.format(Constants.WITHDRAW_TEXT, mod)));
    }

    public void delayKamerstuk(String kamerstukId, String mod) throws KamerstukNotFoundException {
        Kamerstuk kamerstuk = getKamerstukForId(kamerstukId);

        kamerstuk.setVoteDateFromDate(null);
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format(Constants.DELAY_TITLE, kamerstuk.getTitle()), String.format(Constants.DELAY_TEXT, mod)));
    }

    public void saveAdvice(String id, String advice, boolean sendNotification) throws KamerstukNotFoundException {
        Kamerstuk kamerstuk = getKamerstukForId(id);

        kamerstuk.setAdvice(advice);
        kamerstukRepository.save(kamerstuk);

        if(sendNotification) {
            redditSupplier.inbox.compose(kamerstuk.getSubmittedBy(), String.format(Constants.RVS_SUBJECT, kamerstuk.getTitle()), String.format(Constants.RVS_BODY, advice));
        }
    }

    private void postKamerstuk(Kamerstuk kamerstuk) {

        //Set title
        String title;
        if(kamerstuk.getCallsign() != null) {
            title = String.format("%s: %s", kamerstuk.getCallsign(), kamerstuk.getTitle());
        } else {
            title = kamerstuk.getTitle();
        }

        //Set content
        String content;
        if(kamerstuk.getType().isSelectable()) {
            content = String.format("##%s \n \n%s \n \n###%s", kamerstuk.getTitle(), kamerstuk.getContent(), kamerstuk.getReadLengthString());
        }
        else {
            content = String.format("##%s \n \n%s", kamerstuk.getTitle(), kamerstuk.getContent());
        }

        //Post to reddit
        SubmissionReference submission = redditSupplier.redditClient.subreddit(RedditSupplier.SUBREDDIT).submit(SubmissionKind.SELF, title, content, false);
        kamerstuk.setUrl("https://reddit.com/r/"+RedditSupplier.SUBREDDIT+"/comments/"+submission.getId());
        kamerstuk.setPosted(true);
        if(!kamerstuk.getType().needsVote()) {
            kamerstuk.setVotePosted(true);
        }
        kamerstukRepository.save(kamerstuk);

        //Call relevant users
        if(kamerstuk.getToCall().size() > 0) {
            StringBuilder replyBuilder = new StringBuilder();
            replyBuilder.append("###Voor een reactie op dit kamerstuk wordt opgeroepen:  ").append("\n");
            for (String minister : kamerstuk.getToCall()) {
                replyBuilder.append("* ").append(minister).append("  ").append("\n");
            }
            Comment comment = submission.reply(replyBuilder.toString());
            comment.toReference(redditSupplier.redditClient).distinguish(DistinguishedStatus.MODERATOR, true);
        }

        if(!kamerstuk.getAdvice().isEmpty()) {
            submission.reply(kamerstuk.getAdvice());
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

    private Kamerstuk getKamerstukForId(String id) throws KamerstukNotFoundException {
        Optional<Kamerstuk> possibleKamerstuk = kamerstukRepository.findById(id);
        if(possibleKamerstuk.isPresent()) {
            return possibleKamerstuk.get();
        }
        else {
            throw new KamerstukNotFoundException();
        }
    }

    private boolean checkCallsignFormat(KamerstukType type, String callsign) {
        boolean isAllowed = false;
        switch (type) {
            case WET:
            case MOTIE:
                isAllowed = callsign.matches("[MW][0-9]{4}");
                break;
            case BRIEF:
            case DEBAT:
            case VRAGEN:
            case BESLUIT:
                isAllowed = callsign.matches("[KD][SBV][0-9]{4}");
                break;
            case AMENDEMENT:
                isAllowed = callsign.matches("[W][0-9]{4}-[IV]{1,3}");
                break;
        }
        return isAllowed;
    }

    private int checkCallsignFormat(String callsign) {
        if(callsign.matches("[W][0-9]{4}-[IV]{1,3}")) {
            return 7;
        }
        else if(callsign.substring(0, 5).matches("[MW][0-9]{4}")) {
            return 5;
        }
        else if(callsign.substring(0, 6).matches("[KD][SBV][0-9]{4}")) {
            return 6;
        }
        else
            return 0;
    }
}
