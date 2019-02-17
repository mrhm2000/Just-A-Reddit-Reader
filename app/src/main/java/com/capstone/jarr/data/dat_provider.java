package com.capstone.jarr.data;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import com.capstone.jarr.data.dat_contract;
import com.capstone.jarr.data.dat_dbhelper;


/**
 * Created on 10/15/2018.
 */


public class dat_provider extends ContentProvider {
    static final String PROVIDER_NAME = "com.capstone.jarr";
    static final String URL = "content://" + PROVIDER_NAME + "/posts";
    static final Uri CONTENT_URI = Uri.parse(URL);
    private static final int TABLE = 1;
    private static final int TABLE_ID = 2;
    private static final UriMatcher mUriMatcher;
    private SQLiteOpenHelper mOpenHelper;
    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(dat_contract.AUTHORITY, dat_contract.posts.TABLE_NAME, TABLE );
        mUriMatcher.addURI(dat_contract.AUTHORITY, dat_contract.posts.TABLE_NAME + "/#", TABLE_ID);

    }
    public dat_provider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int delete;
        switch (mUriMatcher.match(uri)){
            case TABLE_ID:{
                delete = db.delete(dat_contract.posts.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TABLE:{
                delete = db.delete(dat_contract.posts.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported Uri For Deletion " + uri);
        }
        if(getContext()!=null&&delete!=0)
            getContext().getContentResolver().notifyChange(uri, null);
        return delete;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)){
            case TABLE:
                return dat_contract.posts.CONTENT_TYPE;
            case TABLE_ID:
                return dat_contract.posts.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported Uri" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri uri1=null;
        Log.i("insert",dat_contract.posts.TABLE_NAME+"  "+values.toString());
        switch (mUriMatcher.match(uri)){
            case TABLE:{
                long id = db.insert(dat_contract.posts.TABLE_NAME, null, values);
                if(id!=-1){
                    uri1 = dat_contract.posts.buildUri(id);
                }
                break;
            }
            default:{
                throw new IllegalArgumentException("Unsupported Uri For Insertion " + uri);
            }
        }
        if(getContext()!=null)
            getContext().getContentResolver().notifyChange(uri, null);
        return uri1;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        int numInserted = 0;
        String table=dat_contract.posts.TABLE_NAME;

        int uriType = mUriMatcher.match(uri);

        switch (uriType) {
            case TABLE:
                table = dat_contract.posts.TABLE_NAME;
                break;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = db.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            db.endTransaction();
        }
        return numInserted;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new dat_dbhelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor;
        switch (mUriMatcher.match(uri)){
            case TABLE:{
                cursor = db.query(dat_contract.posts.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported Uri" + uri);
        }
        if(getContext()!=null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int update;
        switch (mUriMatcher.match(uri)){
            case TABLE_ID:{
                update = db.update(dat_contract.posts.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported Uri For Updating " + uri);
        }
        if(getContext()!=null)
            getContext().getContentResolver().notifyChange(uri, null);
        return update;
    }

}
