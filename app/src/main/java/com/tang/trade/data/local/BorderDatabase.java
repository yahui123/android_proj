package com.tang.trade.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by daibin on 2018/5/8.
 */

@Database(entities = {BorderEntity.class}, version = 1,exportSchema = false)
public abstract class BorderDatabase extends RoomDatabase {

    public abstract BorderDao borderDao();
}
