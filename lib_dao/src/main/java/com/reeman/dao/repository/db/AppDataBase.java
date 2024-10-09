package com.reeman.dao.repository.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.reeman.dao.repository.dao.CrashNotifyDao;
import com.reeman.dao.repository.dao.DeliveryRecordDao;
import com.reeman.dao.repository.dao.RouteWithPointsDao;
import com.reeman.dao.repository.entities.CrashNotify;
import com.reeman.dao.repository.entities.DeliveryRecord;
import com.reeman.dao.repository.entities.RouteWithPoints;


@Database(entities = {DeliveryRecord.class, RouteWithPoints.class, CrashNotify.class}, exportSchema = false, version = 2)
public abstract class AppDataBase extends RoomDatabase {

    private static final String DB_NAME = "db_delivery_record";

    private static volatile AppDataBase sInstance;

    public abstract DeliveryRecordDao deliveryRecordDao();

    public abstract RouteWithPointsDao routeWithPointsDao();

    public abstract CrashNotifyDao crashNotifyDao();

    public static AppDataBase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppDataBase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context, AppDataBase.class, DB_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return sInstance;
    }

    static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `t_crash_notify` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`t_notify` TEXT)");
        }
    };



}
