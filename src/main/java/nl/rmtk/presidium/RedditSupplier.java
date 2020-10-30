package nl.th8.presidium;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.references.InboxReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class RedditSupplier {

    public static String SUBREDDIT;

    public final UserAgent userAgent;
    private final Credentials credentials;
    public RedditClient redditClient;
    public InboxReference inbox;
    public boolean redditDown = false;

    @Autowired
    public RedditSupplier(@Value("${manager.subreddit}") String subreddit,
                          @Value("${manager.reddit-username}") String username,
                          @Value("${manager.reddit-password}") String password,
                          @Value("${manager.reddit-client-id}") String redditClientId,
                          @Value("${manager.reddit-client-secret}") String redditClientSecret)
    {
        SUBREDDIT = subreddit;
        this.userAgent = new UserAgent("GERDI-RMTK", "nl.th8.presidium", "v1.1", username);
        this.credentials = Credentials.script(username, password, redditClientId, redditClientSecret);
        NetworkAdapter adapter = new OkHttpNetworkAdapter(this.userAgent);
        try {
            this.redditClient = OAuthHelper.automatic(adapter, this.credentials);
            this.inbox = redditClient.me().inbox();
        }
        catch (Exception e) {
            this.redditDown = true;
        }
    }

    public void retryGetReddit() {
        if(redditDown) {
            try {
                NetworkAdapter adapter = new OkHttpNetworkAdapter(this.userAgent);
                this.redditClient = OAuthHelper.automatic(adapter, this.credentials);
                this.inbox = redditClient.me().inbox();
                redditDown = false;
            }
            catch (Exception ignore) {/*Ignore failures when trying to reconnect */}
        }
    }
}
