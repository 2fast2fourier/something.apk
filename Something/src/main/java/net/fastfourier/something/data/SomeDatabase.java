package net.fastfourier.something.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.salvadordalvik.fastlibrary.data.FastDatabase;

/**
 * Created by matthewshepard on 1/21/14.
 */
public class SomeDatabase extends FastDatabase {
    public static final int DB_VERSION = 3;

    public static final String TABLE_FORUM = "forum";
    public static final String TABLE_STARRED_FORUM = "starred_forum";

    public static final String VIEW_FORUMS = "all_forums";
    public static final String VIEW_STARRED_FORUMS = "starred_forums";

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
        createViews(db);
    }

    private void createViews(SQLiteDatabase db){
        db.execSQL("create view all_forums as " +
                "select forum.forum_id as forum_id, forum_name, parent_forum_id, category, forum_starred " +
                "from forum left join starred_forum using (forum_id) " +
                "order by forum_index");
        db.execSQL("create view starred_forums as " +
                "select forum.forum_id as forum_id, forum_name, parent_forum_id, category, forum_starred " +
                "from forum, starred_forum using (forum_id) " +
                "order by forum_index");
    }

    private void dropViews(SQLiteDatabase db){
        db.execSQL("drop view if exists all_forums");
        db.execSQL("drop view if exists starred_forums");
    }

    private void dropTables(SQLiteDatabase db){
        db.execSQL("drop table if exists forum");
        db.execSQL("drop table if exists starred_forum");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropViews(db);
        dropTables(db);
        onCreate(db);
    }
}
