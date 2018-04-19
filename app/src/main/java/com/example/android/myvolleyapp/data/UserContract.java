package com.example.android.myvolleyapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Prepares the structure of the database
 */

public final class UserContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private UserContract() {}

    static final String CONTENT_AUTHORITY = "com.example.android.myvolleyapp";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_USERS = "users";

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USERS);

        /** Name of database table for pets */
        final static String TABLE_NAME = "users";

        /**
         * Unique ID number for the username (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Username.
         *
         * Type: TEXT
         */
        public final static String COLUMN_USER_NAME ="username";

        /**
         * Image Url
         *
         * Type: TEXT
         */
        public final static String COLUMN_IMAGE_URL = "imageUrl";

        /**
         * User Url
         *
         * Type TEXT
         */
        public final static String COLUMN_USER_URL = "userUrl";

        /**
         * User Info Url
         * Type TEXT
         */
        public final static String COLUMN_USER_INFO = "userInfo";
    }
}
