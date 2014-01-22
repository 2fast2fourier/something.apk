package com.salvadordalvik.something.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.salvadordalvik.fastlibrary.data.FastDatabase;

/**
 * Created by matthewshepard on 1/21/14.
 */
public class SomeDatabase extends FastDatabase {
    public static final int DB_VERSION = 2;

    public static final String TABLE_FORUM = "forum";
    public static final String TABLE_STARRED_FORUM = "starred_forum";

    private static SomeDatabase db;

    private SomeDatabase(Context context) {
        super(context, "something.db", DB_VERSION);
    }

    public synchronized static void init(Context context){
        db = new SomeDatabase(context.getApplicationContext());
    }

    public static synchronized SomeDatabase getDatabase(){
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table forum (" +
                "forum_id INTEGER PRIMARY KEY," +
                "forum_name TEXT NOT NULL," +
                "parent_forum_id INTEGER DEFAULT 0," +
                "category TEXT," +
                "forum_index INTEGER NOT NULL" +
                ")");
        db.execSQL("create table starred_forum (" +
                "forum_id INTEGER PRIMARY KEY," +
                "forum_starred INTEGER DEFAULT 1" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists forum");
        db.execSQL("drop table if exists starred_forum");
        onCreate(db);
    }
}
