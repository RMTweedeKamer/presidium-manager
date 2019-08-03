package nl.th8.presidium.scheduler.service;

import net.dean.jraw.models.SubmissionKind;
import nl.th8.presidium.Constants;
import nl.th8.presidium.RedditSupplier;
import nl.th8.presidium.home.controller.dto.Kamerstuk;
import nl.th8.presidium.home.data.KamerstukRepository;
import nl.th8.presidium.scheduler.controller.dto.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
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

    @Scheduled(fixedRate = 60000)
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

    public void queueKamerstuk(Kamerstuk kamerstuk) {
        kamerstukRepository.save(kamerstuk);
        notificationService.addNotification(new Notification(String.format("Kamerstuk met identificator: %s is in de wachtrij gezet.", kamerstuk.getCallsign()),
                String.format("Ingepland voor: %s", kamerstuk.getPostDate().toString())));
    }

    private void postKamerstuk(Kamerstuk kamerstuk) {
        String title = String.format("%s: %s", kamerstuk.getCallsign(), kamerstuk.getTitle());
        String content = String.format("##%s \n \n %s", kamerstuk.getTitle(), kamerstuk.getContent());
        supplier.redditClient.subreddit(Constants.SUBREDDIT).submit(SubmissionKind.SELF, title, content, false);
    }
}
