package com.capstone.jarr.comset;
import java.util.Set;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.HashSet;
import com.capstone.jarr.R;

/**
 * Created on 09/09/2018.
 */

public class comset_preference {
    public static Set<String> getFavoriteSubreddits(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(R.string.str_favkey), new HashSet<String>());
    }

    private static void editFavoriteSubreddit(Context context, String id, Boolean add) {
        Set<String> favorites = getFavoriteSubreddits(context);
        Set<String> favoritesCopy = new HashSet<>();
        favoritesCopy.addAll(favorites);
        if (add) {
            favoritesCopy.add(id);
        } else {
            favoritesCopy.remove(id);
        }

        updateFavorites(context, favoritesCopy);
    }

    public static void updateFavorites(Context context, Set<String> favoriteIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(context.getString(R.string.str_favkey), favoriteIds);
        editor.apply();
    }

    public static void addFavoriteSubreddit(Context context, String id) {
        editFavoriteSubreddit(context, id, true);
    }

    public static void removeFavoriteSubreddit(Context context, String id) {
        editFavoriteSubreddit(context, id, false);
    }
}
