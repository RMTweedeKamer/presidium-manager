package nl.th8.presidium;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.references.InboxReference;
import org.springframework.stereotype.Service;

@Service
public class RedditSupplier {

    public UserAgent userAgent;
    public RedditClient redditClient;
    public InboxReference inbox;

    public RedditSupplier() {
        this.userAgent = new UserAgent("GERDI-RMTK", "nl.th8.presidium", "v0.1", Constants.USERNAME);
        Credentials credentials = Credentials.script(Constants.USERNAME, Constants.PASSWORD, Constants.CLIENT_ID, Constants.CLIENT_SECRET);
        NetworkAdapter adapter = new OkHttpNetworkAdapter(this.userAgent);
        this.redditClient = OAuthHelper.automatic(adapter, credentials);
        this.inbox = redditClient.me().inbox();
    }
}
