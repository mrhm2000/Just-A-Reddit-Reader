package com.capstone.jarr.data;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created on 10/15/2018.
 */


public class dat_contract {
    public static final String AUTHORITY = "com.capstone.jarr";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final class posts implements BaseColumns {

        public static final String TABLE_NAME="posts";
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +"/" +CONTENT_URI.toString();

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" +CONTENT_URI.toString();


        public static final String _ID = "_id";
        public static final String DATA = "data";

        public static Uri buildUri(Long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }
}


