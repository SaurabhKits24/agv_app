package com.reeman.dao.repository;


import com.reeman.dao.repository.db.AppDataBase;
import com.reeman.dao.repository.entities.CrashNotify;
import com.reeman.dao.repository.entities.DeliveryRecord;
import com.reeman.dao.repository.entities.RouteWithPoints;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class DbRepository {
    private static DbRepository sInstance;
    private final AppDataBase database;

    public DbRepository(AppDataBase database) {
        this.database = database;
    }

    public static DbRepository getInstance(final AppDataBase database) {
        if (sInstance == null) {
            synchronized (DbRepository.class) {
                if (sInstance == null) {
                    sInstance = new DbRepository(database);
                }
            }
        }
        return sInstance;
    }

    public static DbRepository getInstance(){
        return sInstance;
    }

    public long addCrashNotify(CrashNotify crashNotify){
        return database.crashNotifyDao().addCrashNotify(crashNotify);
    }

    public Single<List<CrashNotify>> getAllCrashNotify(){
        return database.crashNotifyDao().getAllCrashNotify();
    }

    public void deleteNotify(int id){
        database.crashNotifyDao().deleteNotify(id);
    }

    public long addDeliveryRecord(DeliveryRecord record) {
        return database.deliveryRecordDao().addDeliveryRecord(record);
    }

    public Single<List<DeliveryRecord>> getAllDeliveryRecords() {
        return database.deliveryRecordDao().getAllDeliveryRecords();
    }

    public void deleteAllRecords() {
        database.deliveryRecordDao().deleteAllRecords();
    }

    public void addAllRouteWithPoints(List<RouteWithPoints> routeWithPoints){
         database.routeWithPointsDao().insertAllRouteWithPoints(routeWithPoints);
    }

    public Single<Integer> deleteAllRouteWithPoints(int navigationMode){
        return database.routeWithPointsDao().deleteAllRouteWithPoints(navigationMode);
    }

    public Single<List<RouteWithPoints>> getAllRouteWithPoints(int navigationMode){
        return database.routeWithPointsDao().getAllRouteWithPoints(navigationMode);
    }

    public Single<RouteWithPoints> getRouteWithPointsByRouteNameAndNavigationMode(String routeName,int navigationMode){
        return database.routeWithPointsDao().getRouteWithPointsByNameAndNavigationMode(routeName, navigationMode);
    }

    public Single<Long> addRoute(RouteWithPoints routeWithPoints){
        return database.routeWithPointsDao().insertRoute(routeWithPoints);
    }

    public Single<Integer> updateRoute(RouteWithPoints routeWithPoints){
        return database.routeWithPointsDao().updateRoute(routeWithPoints);
    }

    public Single<Integer> deleteRouteById(long id){
        return database.routeWithPointsDao().deleteRouteById(id);
    }

}
