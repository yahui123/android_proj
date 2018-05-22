package com.tang.trade.data.local;

import android.arch.persistence.room.Room;

import com.tang.trade.data.local.BorderDatabase;
import com.tang.trade.tang.MyApp;

/**
 * Created by daibin on 2018/5/8.
 */

public class DatabaseManager {

    private static BorderDatabase borderDatabase;

    private DatabaseManager() {

    }

    public static BorderDatabase getInstance() {
        if (borderDatabase == null) {
            synchronized (BorderDatabase.class) {
                if (borderDatabase == null) {
                    borderDatabase = Room.databaseBuilder(MyApp.getContext(),
                            BorderDatabase.class, "db_config").allowMainThreadQueries().build();
                }
            }
        }
        return borderDatabase;
    }
}
