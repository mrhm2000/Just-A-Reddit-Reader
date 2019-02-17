package net.dean.jraw.android;
import net.dean.jraw.auth.TokenStore;
import android.content.SharedPreferences;
import net.dean.jraw.auth.NoSuchTokenException;
import android.content.Context;

/**
 * Created on 09/11/18.
 */
public class lib_token implements TokenStore {
    private final Context context;

    public lib_token(Context context) {
        this.context = context;
    }

    @Override
    public boolean isStored(String key) {
        return getSharedPreferences().contains(key);
    }

    @Override
    public String readToken(String key) throws NoSuchTokenException {
        String token = getSharedPreferences().getString(key, null);
        if (token == null)
            throw new NoSuchTokenException("Token for key '" + key + "' does not exist");
        return token;
    }

    @Override
    public void writeToken(String key, String token) {
        getSharedPreferences().edit()
                .putString(key, token)
                .apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(context.getString(R.string.leadfile), Context.MODE_PRIVATE);
    }
}
