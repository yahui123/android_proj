package com.tang.trade.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by daibin on 2018/5/8.
 */

@Dao
public abstract class BorderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertOne(BorderEntity borderEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<BorderEntity> list);

    @Query("SELECT msg FROM config_msg WHERE msgKey IN(:msgKey)")
    public abstract String queryOne(String msgKey);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateAll(List<BorderEntity> list);

    @Delete
    public abstract void removeOne(BorderEntity borderEntity);

    @Delete
    public abstract void removeAll(List<BorderEntity> list);


}
