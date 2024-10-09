package com.reeman.agv.elevator.request.service;

import com.google.gson.JsonObject;
import com.reeman.agv.elevator.request.model.CancelElevator;
import com.reeman.agv.elevator.request.model.Passenger;
import com.reeman.agv.elevator.request.model.Response;
import com.reeman.agv.elevator.request.model.TakeElevator;
import com.reeman.agv.elevator.request.model.TakeElevatorInside;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("dispatching/robot/{hostname}")
    Call<Response> requestRobotId(@Path("hostname") String hostname);


    @PUT("thing/things/{thingId}/activate")
    Call<Response> activationInstance(@Path("thingId") String thingId, @Body JsonObject jsonObject);

    @PUT("thing/things/{thingId}/online")
    Call<Response> online(@Path("thingId") String thingId, @Body JsonObject jsonObject);

    @POST("dispatching/requests")
    Call<Response> takeElevator(@Body TakeElevator body);

    @POST("dispatching/robot/requests/leave")
    Call<Response> takeElevatorInside(@Body TakeElevatorInside body);

    //    @DELETE("dispatching/elevators/{elevatorId}/requests")
    @HTTP(method = "DELETE", path = "dispatching/elevators/{elevatorId}/requests", hasBody = true)
    Call<Response> cancelTakeElevatorExecute(@Path("elevatorId") String elevatorId, @Body CancelElevator body);

    @HTTP(method = "DELETE", path = "dispatching/elevators/{elevatorId}/requests", hasBody = true)
    Observable<Response> cancelTakeElevator(@Path("elevatorId") String elevatorId, @Body CancelElevator body);

    @PUT("dispatching/elevators/{elevatorId}/requests")
    Call<Response> completeExecute(@Path("elevatorId") String elevatorId, @Body Passenger passenger);

    @PUT("dispatching/elevators/{elevatorId}/requests")
    Observable<Response> complete(@Path("elevatorId") String elevatorId, @Body Passenger passenger);

    @GET("dispatching/elevators/{elevatorId}")
    Call<Response> getElevatorStatus(@Path("elevatorId")String elevatorId);

}
