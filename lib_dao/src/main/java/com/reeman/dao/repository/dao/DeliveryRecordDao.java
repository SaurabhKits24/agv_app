package com.reeman.dao.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.reeman.dao.repository.entities.DeliveryRecord;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface DeliveryRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addDeliveryRecord(DeliveryRecord record);

    @Query("SELECT * FROM T_DELIVERY_RECORD")
    Single<List<DeliveryRecord>> getAllDeliveryRecords();

    @Query("DELETE FROM T_DELIVERY_RECORD")
    void deleteAllRecords();
}
