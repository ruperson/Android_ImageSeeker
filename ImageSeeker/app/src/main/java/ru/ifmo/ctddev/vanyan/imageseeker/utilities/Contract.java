package ru.ifmo.ctddev.vanyan.imageseeker.utilities;

import android.provider.BaseColumns;
public final class Contract {
    private Contract() {}

    public static final class Entry implements BaseColumns {


        public final static String TABLE_NAME = "photos";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_SMALL_PHOTO ="small_photo";

        public final static String COLUMN_BIG_PHOTO = "big_photo";

        public final static String COLUMN_DESCRIPTION = "description";

    }

}
