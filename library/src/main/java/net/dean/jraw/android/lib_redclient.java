package net.dean.jraw.android;
import net.dean.jraw.http.RestResponse;
import net.dean.jraw.http.UserAgent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import net.dean.jraw.http.oauth.InvalidScopeException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.HttpRequest;
import net.dean.jraw.http.NetworkException;



/**
 * Created on 09/11/18.
 */
public class lib_redclient extends RedditClient {
    private static final String KEY_USER_AGENT_OVERRIDE = "net.dean.jraw.USER_AGENT_OVERRIDE";
    private static final String KEY_REDDIT_USERNAME =     "net.dean.jraw.REDDIT_USERNAME";
    private static final String PLATFORM =                "android";

    private static UserAgent getUserAgent(Context context) {
        try {
            Bundle bundle = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;

            if (bundle == null)
                throw new IllegalStateException("Please specify a <meta-data> for either " + KEY_REDDIT_USERNAME +
                        " or " + KEY_USER_AGENT_OVERRIDE);

            String userAgent = bundle.getString(KEY_USER_AGENT_OVERRIDE, null);
            if (userAgent != null)
                return UserAgent.of(userAgent);

            String username = bundle.getString(KEY_REDDIT_USERNAME, null);
            if (username == null)
                throw new IllegalStateException("No <meta-data> for " + KEY_REDDIT_USERNAME);
            return UserAgent.of(PLATFORM, context.getPackageName(), BuildConfig.VERSION_NAME, username);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Could not find package metadata for own package", e);
        }
    }


    public lib_redclient(Context context) {
        this(getUserAgent(context));
    }

    public lib_redclient(UserAgent userAgent) {
        super(userAgent);
    }

    @Override
    public RestResponse execute(HttpRequest request) throws NetworkException, InvalidScopeException {
        if (getUserAgent().trim().isEmpty()) {
            throw new IllegalStateException("No UserAgent specified");
        }
        return super.execute(request);
    }
}
