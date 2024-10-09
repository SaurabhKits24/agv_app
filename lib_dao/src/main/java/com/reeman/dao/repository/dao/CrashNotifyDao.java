package com.reeman.dao.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.reeman.dao.repository.entities.CrashNotify;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface CrashNotifyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addCrashNotify(CrashNotify crashNotify);

    @Query("SELECT * FROM T_CRASH_NOTIFY")
    Single<List<CrashNotify>> getAllCrashNotify();

    @Query("DELETE FROM T_CRASH_NOTIFY WHERE id = :id")
    void deleteNotify(int id);
}
