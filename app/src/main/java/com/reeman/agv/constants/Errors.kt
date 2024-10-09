package com.reeman.agv.constants

import android.content.Context
import com.reeman.agv.R
import com.reeman.commons.event.SensorsEvent
import com.reeman.commons.exceptions.ElevatorNetworkNotSettException
import com.reeman.commons.state.NavigationMode
import com.reeman.commons.state.RobotInfo
import com.reeman.points.exception.ChargingPointCountException
import com.reeman.points.exception.ChargingPointNotMarkedException
import com.reeman.points.exception.ChargingPointPositionErrorException
import com.reeman.points.exception.CurrentMapNotInLegalMapListException
import com.reeman.points.exception.ElevatorPointNotLegalException
import com.reeman.points.exception.MapListEmptyException
import com.reeman.points.exception.NoLegalMapException
import com.reeman.points.exception.PathListEmptyException
import com.reeman.points.exception.PointListEmptyException
import com.reeman.points.exception.RequiredMapNotFoundException
import com.reeman.points.exception.RequiredPointsNotFoundException
import java.io.IOException

object Errors {
    fun getSensorError(context: Context, event: SensorsEvent): String? {
        val stringBuilder = StringBuilder()
        if (event.isLidarWarn) stringBuilder.append(context.getString(R.string.exception_laser_error))
        if (event.isIMUWarn) {
            if (stringBuilder.isNotEmpty()) stringBuilder.append(",")
            stringBuilder.append(context.getString(R.string.exception_imu_error))
        }
        if (event.isOdomWarn) {
            if (stringBuilder.isNotEmpty()) stringBuilder.append(",")
            stringBuilder.append(context.getString(R.string.exception_speedometer_error))
        }
        if (event.isCam3DWarn) {
            if (stringBuilder.isNotEmpty()) stringBuilder.append(",")
            stringBuilder.append(context.getString(R.string.exception_3d_error))
        }
        if (event.isROSWarn) {
            if (stringBuilder.isNotEmpty()) stringBuilder.append(",")
            stringBuilder.append(context.getString(R.string.exception_ros_error))
        }
        return if (stringBuilder.isEmpty()) null else context.getString(
            R.string.text_check_sensor_state_error_cannot_use_application,
            stringBuilder.toString()
        )
    }

    fun getFaultReason(event: SensorsEvent): Int {
        if (event.isIMUWarn) return 4
        if (event.isLidarWarn) return 1
        if (event.isOdomWarn) return 2
        return if (event.isCam3DWarn) 7 else 0
    }

    fun getNavigationStartError(context: Context, code: Int): String {
        when (code) {
            -1 -> return context.getString(R.string.text_docking)
            -2 -> return context.getString(R.string.voice_scram_stop_turn_on)
            -3 -> return context.getString(R.string.text_ac_charging)
            -4 -> return context.getString(R.string.voice_not_found_target_point)
            -5 -> return context.getString(R.string.text_agv_dock_failed)
            -6 -> return context.getString(R.string.text_location_exception)
            -7, -8 -> return context.getString(R.string.text_cannot_arrive_this_target)
            -9 -> return context.getString(R.string.text_read_file_exception)
        }
        return context.getString(R.string.text_undefined_start_navigation_error_code, code)
    }

    fun getDataLoadFailedTip(context: Context, throwable: Throwable) =
        when (throwable) {
            is ElevatorNetworkNotSettException -> {
                if (throwable.isOutSideNetworkNotSet && throwable.isInsideNetWorkNotSet) {
                    context.getString(R.string.text_please_choose_inside_network_and_outside_network_to_start_task)
                } else if (throwable.isInsideNetWorkNotSet) {
                    context.getString(R.string.text_please_choose_inside_network_to_start_task)
                } else {
                    context.getString(R.string.text_please_choose_outside_network_to_start_task)
                }
            }

            is RequiredMapNotFoundException -> {
                if (throwable.isChargePointMapSelected) {
                    context.getString(R.string.text_please_choose_production_point_map_to_start_task)
                } else {
                    context.getString(R.string.text_please_choose_charging_pile_map_to_start_task)
                }
            }

            is NoLegalMapException -> {
                context.getString(R.string.text_loaded_success_map_illegal)
            }

            is ElevatorPointNotLegalException -> {
                val mapList = throwable.mapList
                val stringBuilder = StringBuilder()

                mapList.forEachIndexed { index, entry ->
                    val (first, second) = entry
                    stringBuilder.append(context.getString(R.string.exception_map_not_legal, first)).append("\n")

                    val exceptions = arrayOf(
                        R.string.exception_not_mark_waiting_elevator_point to R.string.exception_check_more_than_one_waiting_elevator_point,
                        R.string.exception_not_mark_enter_elevator_point to R.string.exception_check_more_than_one_enter_elevator_point,
                        R.string.exception_not_mark_inside_elevator_point to R.string.exception_check_more_than_one_inside_elevator_point,
                        R.string.exception_not_mark_leave_elevator_point to R.string.exception_check_more_than_one_leave_elevator_point
                    )

                    var firstException = true

                    second.forEachIndexed { idx, count ->
                        if (count == 0) {
                            if (!firstException) {
                                stringBuilder.append(", ")
                            }
                            stringBuilder.append(context.getString(exceptions[idx].first))
                            firstException = false
                        } else if (count > 1) {
                            if (!firstException) {
                                stringBuilder.append(", ")
                            }
                            stringBuilder.append(context.getString(exceptions[idx].second, count))
                            firstException = false
                        }
                    }

                    if (index == mapList.size - 1) {
                        stringBuilder.append(";")
                    } else {
                        stringBuilder.append(".")
                    }
                    stringBuilder.append("\n")
                }
                stringBuilder.toString()
            }

            is PointListEmptyException -> {
                when (throwable.code) {
                    PointListEmptyException.ROS_QRCODE_POINTS_EMPTY -> {
                        context.getString(R.string.text_loaded_failed_and_local_data_empty)
                    }

                    PointListEmptyException.LOCAL_QRCODE_POINTS_EMPTY -> {
                        context.getString(R.string.text_loaded_qrcode_points_success_with_no_data)
                    }

                    in arrayListOf(
                        PointListEmptyException.ROS_POINTS_EMPTY,
                        PointListEmptyException.ROS_NO_TARGET_TYPE_POINTS
                    ) -> {
                        context.getString(R.string.text_loaded_success_with_no_data)
                    }

                    in arrayListOf(
                        PointListEmptyException.LOCAL_POINTS_EMPTY,
                        PointListEmptyException.ROS_NO_TARGET_TYPE_POINTS
                    ) -> {
                        context.getString(R.string.text_loaded_failed_and_local_data_empty)
                    }

                    PointListEmptyException.ROS_NO_VALID_AGV_POINT -> {
                        context.getString(R.string.text_loaded_success_but_no_valid_agv_point)
                    }

                    PointListEmptyException.LOCAL_NO_VALID_AGV_POINT -> {
                        context.getString(R.string.text_loaded_failed_use_local_data_but_no_valid_agv_point)
                    }

                    else -> {
                        context.getString(R.string.text_loaded_success_with_no_data)
                    }
                }
            }

            is CurrentMapNotInLegalMapListException -> {
                context.getString(R.string.text_current_map_not_in_legal_map_list)
            }

            is RequiredPointsNotFoundException -> {
                if (!throwable.isChargePointMarked && !throwable.isProductionPointMarked) {
                    context.getString(R.string.text_not_mark_charge_point_and_product_point)
                } else if (!throwable.isProductionPointMarked) {
                    context.getString(R.string.text_not_mark_product_point)
                } else {
                    context.getString(R.string.text_not_mark_charge_point)
                }
            }

            is MapListEmptyException -> {
                when (throwable.code) {
                    MapListEmptyException.ROS_MAP_EMPTY -> {
                        context.getString(R.string.text_map_data_load_success_but_empty)
                    }

                    MapListEmptyException.LOCAL_MAP_EMPTY -> {
                        context.getString(R.string.text_local_map_data_load_success_but_empty)
                    }

                    MapListEmptyException.ROS_NO_VALID_MAP -> {
                        context.getString(R.string.text_map_data_load_success_but_no_valid_map)
                    }

                    else -> {
                        context.getString(R.string.text_local_map_data_load_success_but_no_valid_map)

                    }
                }
            }

            is ChargingPointPositionErrorException -> context.getString(R.string.exception_check_charge_point_position_error)
            is ChargingPointNotMarkedException -> context.getString(R.string.exception_not_mark_charge_point_in_mark_point_page)

            is ChargingPointCountException -> {
                if (throwable.count == 0) {
                    context.getString(R.string.text_not_mark_charge_point)
                } else {
                    context.getString(
                        R.string.exception_check_charge_point_count_error,
                        if (RobotInfo.navigationMode == NavigationMode.autoPathMode) {
                            context.getString(R.string.text_auto_model)
                        } else {
                            context.getString(R.string.text_fix_model)
                        },
                        throwable.count
                    )
                }
            }

            is PathListEmptyException -> {
                context.getString(R.string.text_not_mark_path)
            }

            is IOException -> {
                context.getString(R.string.text_point_loaded_failed)
            }

            else -> {
                context.getString(R.string.text_point_loaded_failed_please_check_map)
            }
        }
}