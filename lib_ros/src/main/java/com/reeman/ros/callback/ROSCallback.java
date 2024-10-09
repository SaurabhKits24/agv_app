package com.reeman.ros.callback;

import com.reeman.commons.event.AGVDockResultEvent;
import com.reeman.commons.event.AGVTagPoseEvent;
import com.reeman.commons.event.ApplyMapEvent;
import com.reeman.commons.event.BaseVelEvent;
import com.reeman.commons.event.CoreDataEvent;
import com.reeman.commons.event.CurrentMapEvent;
import com.reeman.commons.event.FixedPathResultEvent;
import com.reeman.commons.event.GetPlanResultEvent;
import com.reeman.commons.event.GlobalPathEvent;
import com.reeman.commons.event.HostnameEvent;
import com.reeman.commons.event.IPEvent;
import com.reeman.commons.event.InitPoseEvent;
import com.reeman.commons.event.InitiativeLiftingModuleStateEvent;
import com.reeman.commons.event.MissPoseEvent;
import com.reeman.commons.event.MoveDoneEvent;
import com.reeman.commons.event.MoveStatusEvent;
import com.reeman.commons.event.NavPoseEvent;
import com.reeman.commons.event.NavigationResultEvent;
import com.reeman.commons.event.PowerOffEvent;
import com.reeman.commons.event.PowerOnTimeEvent;
import com.reeman.commons.event.ROSModelEvent;
import com.reeman.commons.event.RobotTypeEvent;
import com.reeman.commons.event.SensorsEvent;
import com.reeman.commons.event.SpecialPlanEvent;
import com.reeman.commons.event.VersionInfoEvent;
import com.reeman.commons.event.WheelStatusEvent;
import com.reeman.commons.event.WifiConnectResultEvent;

    public interface ROSCallback {
        default void onCurrentMapEvent(CurrentMapEvent event) {}
        default void onHostNameEvent(HostnameEvent event) {}
        default void onIPEvent(IPEvent event) {}
        default void onMoveStatusEvent(MoveStatusEvent event) {}
        default void onInitPoseEvent(InitPoseEvent event) {}
        default void onNavPoseEvent(NavPoseEvent event) {}
        default void onWifiConnectEvent(WifiConnectResultEvent event) {}
        default void onApplyMapEvent(ApplyMapEvent event) {}
        default void onVersionEvent(VersionInfoEvent event) {}
        default void onWheelStatusEvent(WheelStatusEvent event) {}
        default void onROSModelEvent(ROSModelEvent event) {}
        default void onCoreDataEvent(CoreDataEvent event) {}
        default void onNavResultEvent(NavigationResultEvent event) {}
        default void onPowerOffEvent(PowerOffEvent event) {}
        default void onSensorsEvent(SensorsEvent event) {}
        default void onMissPoseEvent(MissPoseEvent event) {}
        default void onMoveDoneEvent(MoveDoneEvent event) {}
        default void onBaseVelEvent(BaseVelEvent event) {}
        default void onGlobalPathEvent(GlobalPathEvent event) {}
        default void onSpecialPlanEvent(SpecialPlanEvent event) {}
        default void onAGVTagPoseEvent(AGVTagPoseEvent event) {}
        default void onAGVDockResultEvent(AGVDockResultEvent event) {}
        default void onGetPlanResultEvent(GetPlanResultEvent event) {}
        default void onFixedPathResultEvent(FixedPathResultEvent event) {}
        default void onRobotTypeEvent(RobotTypeEvent event) {}
        default void onInitiativeLiftingModuleStateEvent(InitiativeLiftingModuleStateEvent event) {}
        default void onROSPowerOnTimeEvent(PowerOnTimeEvent event){}
    }
