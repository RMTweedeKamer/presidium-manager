package nl.th8.presidium.scheduler.service;

import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.tree.CommentNode;
import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.TemmieSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.controller.dto.KamerstukType;
import nl.th8.presidium.home.controller.dto.StatDTO;
import nl.th8.presidium.home.controller.dto.VoteType;
import nl.th8.presidium.home.data.KamerstukRepository;
import nl.th8.presidium.scheduler.controller.dto.Notification;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class VoteService {

    private final Logger logger = LoggerFactory.getLogger(VoteService.class);

    private final KamerstukRepository kamerstukRepository;

    private final SettingsProvider settingsProvider;

    private final RedditSupplier redditSupplier;

    private final NotificationService notificationService;

    private final TemmieSupplier temmieSupplier;


    @Autowired
    public VoteService(KamerstukRepository kamerstukRepository, SettingsProvider settingsProvider, RedditSupplier redditSupplier, NotificationService notificationService, TemmieSupplier temmieSupplier) {
        this.kamerstukRepository = kamerstukRepository;
        this.settingsProvider = settingsProvider;
        this.redditSupplier = redditSupplier;
        this.notificationService = notificationService;
        this.temmieSupplier = temmieSupplier;
    }

    public List<Kamerstuk> getToPostResults() {
        return kamerstukRepository.findAllByTypeIsAndDeniedIsFalseAndVoteProcessedIsFalseAndPostDateIsNull(KamerstukType.RESULTATEN);
    }

    public void dismissVotePost(Kamerstuk kamerstuk) {
        kamerstuk.setVoteProcessed(true);
        kamerstukRepository.save(kamerstuk);
    }

    @Scheduled(cron = "0 0 12 ? * SAT")
    public void postVote() {
        List<Kamerstuk> kamerstukkenToCheck = kamerstukRepository.findAllByPostedIsTrueAndVotePostedIsFalseAndDeniedIsFalseAndVoteDateIsNotNull();

        Date currentDate = new Date();
        logger.info("Checking for vote to post at {}", currentDate);
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

        if(!kamerstukkenToCheck.isEmpty()) {
            constructVotePost(kamerstukkenToCheck);
        }
    }

    @Scheduled(cron = "* 15 * * * *")
    public void checkAndProcessVoteResults() {
        List<Kamerstuk> concludedVotes = kamerstukRepository.findAllByTypeEqualsAndVoteDateBeforeAndPostedIsTrueAndDeniedIsFalseAndVoteProcessedIsFalse(KamerstukType.STEMMING, new Date());
        getVoteResults(concludedVotes);
    }

    public void processVoteResultsForUrl(String url) throws NoSuchElementException {
        String[] urlSplit = url.split("/");
        getVoteResults(List.of(kamerstukRepository.findByUrlContainsOrUrlContains(urlSplit[urlSplit.length-2], urlSplit[urlSplit.length-1]).orElseThrow()));
    }

    public void getVoteResults(List<Kamerstuk> concludedVotes) {
        logger.info("Checking for new voteResults to queue at {}", new Date());
        List<String> parsingErrors = new ArrayList<>();
        Set<String> tkMembersToCheck = new HashSet<>(settingsProvider.getTkMembers());
        Map<String, VoteType> initialVote = new HashMap<>();
        tkMembersToCheck.stream().filter(StringUtils::isNotBlank).forEach(member -> initialVote.put(member, VoteType.NG));

        logger.info("Found {} new votes to parse results for", concludedVotes.size());

        //For each passed vote
        for(Kamerstuk kamerstuk : concludedVotes) {
            String[] urlSplit = kamerstuk.getUrl().split("/");
            Map<String, Kamerstuk> votedOn = new HashMap<>();

            SubmissionReference submissionReference = redditSupplier.redditClient.submission(urlSplit[urlSplit.length-1]);
            submissionReference.comments().loadFully(redditSupplier.redditClient);

            //Read all comments
            for(CommentNode<?> commentNode : submissionReference.comments()) {
                String userName = commentNode.getSubject().getAuthor();
                String userVoteString = commentNode.getSubject().getBody();
                //Parse comment to a map of callsign - votetype
                Map<String, VoteType> userVoteMap = processSingleVote(parsingErrors, tkMembersToCheck, userName, userVoteString);

                for(Map.Entry<String, VoteType> singleUserVote : userVoteMap.entrySet()) {
                    //If we already have this kamerstuk
                    if(votedOn.containsKey(singleUserVote.getKey())) {
                        votedOn.get(singleUserVote.getKey()).getVoteMap().replace(userName, singleUserVote.getValue());
                    }
                    //If we do not have this kamerstuk yet, check if it was actually in the vote post
                    else if(kamerstuk.getContent().contains(singleUserVote.getKey())) {
                        Optional<Kamerstuk> optionalKamerstuk = kamerstukRepository.findByCallsignEqualsAndPostedIsTrue(singleUserVote.getKey());
                        if(optionalKamerstuk.isPresent()) {
                            votedOn.put(singleUserVote.getKey(), optionalKamerstuk.get());
                            votedOn.get(singleUserVote.getKey()).getVoteMap().putAll(initialVote);
                            votedOn.get(singleUserVote.getKey()).getVoteMap().replace(userName, singleUserVote.getValue());
                        } else {
                            parsingErrors.add("Er is gestemd op kamerstuk " + singleUserVote.getKey() + " maar dit kamerstuk kon ik niet vinden.");
                        }
                    } else {
                        parsingErrors.add(userName + " heeft gestemd op " + singleUserVote.getKey() + " maar dit kamerstuk is geen onderdeel van de stemming.");
                    }
                }
            }
            votedOn.values().forEach(kamerstuk1 -> kamerstuk1.setVoteProcessed(true));
            kamerstukRepository.saveAll(votedOn.values());
            kamerstuk.setVoteProcessed(true);
            kamerstukRepository.save(kamerstuk);
            notificationService.addNotification(new Notification("Stemresultaten van: " + new SimpleDateFormat(Constants.DATE_FORMAT).format(kamerstuk.getPostDate()),
                    "Dit ging er mis: " + StringUtils.joinWith("\n\n", parsingErrors)));

            constructVoteResultPost(new ArrayList<>(votedOn.values()));
        }
    }

    private Map<String, VoteType> processSingleVote(List<String> parseErrors, Set<String> allowedUsernames, String username, String voteString) {
        Map<String, VoteType> returnValue = new HashMap<>();
        if(allowedUsernames.contains(username) && StringUtils.isNotEmpty(voteString)) {
            List<String> votesSplit = Arrays.stream(voteString.split("[:\\s+]")).filter(StringUtils::isNotBlank).collect(Collectors.toList());

            for(int i = 0; i < votesSplit.size()-1; i++) {
                String part = votesSplit.get(i);
                if(part.matches("[WM][0-9]{4}|[W][0-9]{4}-[IV]{1,3}")) {
                    String nextPart = votesSplit.get(i+1);
                    returnValue.put(part, VoteType.fromString(nextPart));
                }
            }
        } else {
            parseErrors.add(username + " heeft gestemd maar staat niet ingesteld als Kamerlid.");
        }
        return returnValue;
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
            format.append(kamerstuk.getCallsign()).append(": \n\n");
        }
        content.append("Leden van de Tweede Kamer der Staten-Generaal,\n\n");
        content.append("**U kunt op de volgende kamerstuk(ken) uw stem uitbrengen:**  \n");
        content.append(links);
        content.append("\n---\n");
        content.append("**Hanteer a.u.b. het volgende format:**\n\n");
        content.append("Telkens met twee spaties achter de regel\n");
        content.append("Voor/Tegen/Onthouden:\n\n");
        content.append(format);
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

    private void constructVoteResultPost(List<Kamerstuk> resultsToPost) {
        resultsToPost.sort(Comparator.comparing(Kamerstuk::getCallsign));
        StringBuilder title = new StringBuilder();
        StringBuilder results = new StringBuilder();
        StringBuilder content = new StringBuilder();
        StringBuilder spreadsheetReadyResult = new StringBuilder();
        Kamerstuk newResult = new Kamerstuk();
        title.append("Resultaten Stemming Tweede Kamer over ");
        spreadsheetReadyResult.append("Klik op 'source' en kopieer onderstaande informatie voor de RMTK Masterspreadsheet: ").append("\n\n");

        String prefix = "";
        List<String> headerAppend = new ArrayList<>();
        for(Kamerstuk kamerstuk : resultsToPost) {
            title.append(prefix).append(kamerstuk.getCallsign());
            prefix = ", ";
            results.append("[").append(kamerstuk.getCallsign()).append(": ").append(kamerstuk.getTitle()).append("](").append(kamerstuk.getUrl()).append(")  \n");
            results.append(kamerstuk.getResultFromVoteMap());
            results.append("\n---\n");

            headerAppend.add("=hyperlink(\"" + kamerstuk.getUrl() + "\";\"" + kamerstuk.getCallsign() + "\")");
        }
        spreadsheetReadyResult.append(StringUtils.join(headerAppend, "\t"));
        spreadsheetReadyResult.append("\n\n");
        content.append(results);
        content.append(resultsToPost.get(0).getTurnoutForKamerstuk());

        for(String member: settingsProvider.getTkMembers()) {
            List<String> voteForMember = new ArrayList<>();
            for(Kamerstuk kamerstuk : resultsToPost) {
                if(StringUtils.isBlank(member))
                    voteForMember.add("");
                else if(member.equals("NVT"))
                    voteForMember.add("NVT");
                else
                    voteForMember.add(kamerstuk.getVoteMap().get(member).getSpreadsheetValue());
            }
            spreadsheetReadyResult.append(StringUtils.join(voteForMember, "\t")).append("\n");
        }



        newResult.setType(KamerstukType.RESULTATEN);
        newResult.setTitle(title.toString());
        newResult.setContent(content.toString());
        newResult.setUrgent(false);
        newResult.setVotePosted(false);
        newResult.setSubmittedBy("GERDI-RMTK");
        newResult.setAdvice(spreadsheetReadyResult.toString());
        kamerstukRepository.save(newResult);
        temmieSupplier.schedulerEmbeddedMessage(newResult);
    }
}
