package net.fastfourier.something.util;

import net.fastfourier.something.BuildConfig;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class Constants {
    public static final String BASE_URL = "https://forums.somethingawful.com/";
    public static final int BOOKMARK_FORUMID = 9989;
    public static final int YOSPOS_FORUMID = 219;
    public static final int FYAD_FORUMID = 26;
    public static final int FYAD_DUMP_FORUMID = 154;
    public static final int BYOB_FORUMID = 268;
    public static final int COOL_CREW_FORUMID = 196;

    public static final int PM_FOLDER_INBOX = 0;
    public static final int PM_FOLDER_SENT_ITEMS = -1;

    public static final int REQUEST_WRITE_PERMISSIONS = 1;

    public static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * The forums only hide the postbar on certain archived forums (goldmine/gaschamber/ect).
     * They do not use the normal 'forum-closed' image,
     * so this checks against a short list of known archived forums.
     * Thread parsing also checks against the 'forum-closed' image, but not all forums use that.
     * this is fucking terrible.
     */
    public static boolean isArchiveForum(int forumId) {
        return forumId == 21 || forumId == 25 || forumId == 264 || forumId == 115 || forumId == 176 || forumId == 229;
    }
}
