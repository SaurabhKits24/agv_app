package com.reeman.dao.repository.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.reeman.dao.repository.entities.RouteWithPoints;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface RouteWithPointsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addRouteWithPoints(RouteWithPoints routeWithPoints);

    @Query("SELECT * FROM T_ROUTE_WITH_POINTS WHERE t_navigation_mode = :navigationMode")
    Single<List<RouteWithPoints>> getAllRouteWithPoints(int navigationMode);

    @Query("SELECT * FROM T_ROUTE_WITH_POINTS WHERE t_route_name = :name AND t_navigation_mode = :navigationMode LIMIT 1")
    Single<RouteWithPoints> getRouteWithPointsByNameAndNavigationMode(String name, int navigationMode);

    @Query("DELETE FROM T_ROUTE_WITH_POINTS WHERE t_navigation_mode = :navigationMode")
    Single<Integer> deleteAllRouteWithPoints(int navigationMode);

    @Insert
    void insertAllRouteWithPoints(List<RouteWithPoints> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertRoute(RouteWithPoints routeWithPoints);

    @Update
    Single<Integer> updateRoute(RouteWithPoints routeWithPoints);

    @Query("DELETE FROM T_ROUTE_WITH_POINTS WHERE id = :id")
    Single<Integer> deleteRouteById(long id);
}
