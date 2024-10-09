package com.reeman.commons.constants

object Constants {
    const val KEY_SP_NAME = "agv_sp"

    //guide
    const val KEY_IS_LANGUAGE_CHOSEN = "KEY_IS_LANGUAGE_CHOSEN"
    const val KEY_IS_NETWORK_GUIDE = "KEY_IS_NETWORK_GUIDE"
    const val KEY_IS_ALIAS_GUIDE = "KEY_IS_ALIAS_GUIDE"

    //语言
    const val DEFAULT_LANGUAGE_TYPE = 1
    const val KEY_LANGUAGE_TYPE = "KEY_LANGUAGE_TYPE"

    //音量
    const val KEY_MEDIA_VOLUME = "KEY_SYS_VOLUME"
    const val DEFAULT_MEDIA_VOLUME = 12

    //token
    const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"

    //低电
    const val KEY_LOW_POWER = "KEY_LOW_POWER"
    const val DEFAULT_LOW_POWER = 20

    //任务内容
    const val TASK_TARGET = "TASK_TARGET"
    const val TASK_TOKEN = "TASK_TOKEN"
    const val TASK_CHARGE_LOW_POWER = 1
    const val TASK_AUTO_WORK = 2

    //任务结束提示
    const val TASK_RESULT = "TASK_RESULT_PROMPT"

    const val WIFI_RESULT = "WIFI_RESULT"

    //startActivityForResult requestCode
    const val RESULT_CODE_OF_TASK = 1001

    // 锁屏密码相关
    const val KEY_SETTING_PASSWORD_CONTROL = "KEY_SETTING_PASSWORD_CONTROL"
    const val KEY_DEFAULT_SETTING_PASSWORD_CONTROL = 0
    const val KEY_SETTING_PASSWORD = "666777"

    //障碍物配置
    const val KEY_OBSTACLE_CONFIG = "KEY_OBSTACLE_CONFIG"
    const val KEY_DEFAULT_OBSTACLE_ASSETS_PREFIX = "/agv/assets/obstacle"

    //屏幕亮度
    const val KEY_SCREEN_BRIGHTNESS = "KEY_SCREEN_BRIGHTNESS"
    const val DEFAULT_SCREEN_BRIGHTNESS = 100

    //语音最多条数
    const val MAX_AUDIO_FILE_COUNT = 5
    const val WIFI_PASSWORD = "WIFI_PASSWORD"

    //机器从二维码离开的方向和距离,导航默认值0.7
    const val DEFAULT_ORIENTATION_AND_DISTANCE_CALIBRATION = 0.7f

    //机器到达二维码后开始导航时的朝向,默认0(倒退)
    const val DEFAULT_QRCODE_MODE_DIRECTION = 0
    const val DEFAULT_QRCODE_MODE_LIDAR_WIDTH_WITHOUT_THING = 0.05f

    //二维码模式配置
    const val KEY_QRCODE_MODE_CONFIG = "LIFT_MODE_CONFIG"

    //普通模式配置
    const val KEY_NORMAL_MODE_CONFIG = "NORMAL_MODE_CONFIG"

    //路线模式配置
    const val KEY_ROUTE_MODE_CONFIG = "ROUTE_MODE_CONFIG"

    //呼叫模式配置
    const val KEY_CALLING_MODE_CONFIG = "KEY_CALLING_MODE_CONFIG"
    //返航配置
    const val KEY_RETURNING_CONFIG ="KEY_RETURNING_CONFIG"

    //门控设置
    const val KEY_DOOR_CONTROL = "KEY_DOOR_CONTROL"

    //梯控信息
    const val KEY_ELEVATOR_SETTING = "KEY_ELEVATOR_SETTING"

    //导航模式
    const val KEY_NAVIGATION_MODEL = "KEY_NAVIGATION_MODEL"

    //上下班设置
    const val KEY_COMMUTING_TIME_SETTING = "KEY_COMMUTING_TIME_SETTING"

    const val KEY_LIFT_MODEL_INSTALLATION = "KEY_LIFT_MODEL_INSTALLATION"

    const val KEY_STOP_NEARBY_CONTROL = "KEY_STOP_NEARBY_CONTROL"

    const val KEY_TASK_ARRIVED_INFO = "KEY_TASK_ARRIVED_INFO"

    const val KEY_TASK_PAUSE_INFO = "KEY_TASK_PAUSE_INFO"

    const val KEY_TASK_RUNNING_INFO = "KEY_TASK_RUNNING_INFO"

    const val KEY_POINT_SHOW_MODE = "KEY_POINT_SHOW_MODE"

    const val DEFAULT_ACCOUNT = ""

    const val DEFAULT_PASSWORD = ""

    const val DEFAULT_TENANT_ID = ""

    const val DEFAULT_SECURITY_KEY = ""

    const val DEFAULT_SUBSCRIPTION_KEY = ""

    const val DEFAULT_MQTT_USERNAME = ""

    const val DEFAULT_MQTT_PASSWORD = ""
    //上次自检时间
    const val KEY_LAST_REBOOT_TIME = "KEY_LAST_REBOOT_TIME"
    //别名
    const val KEY_ROBOT_ALIAS = "KEY_ROBOT_ALIAS"

    const val KEY_BACKGROUND_MUSIC = "KEY_BACKGROUND_MUSIC"

    const val REQUEST_OF_OUTSIDE_NETWORK = 101
    const val REQUEST_OF_INSIDE_NETWORK = 102

    const val RESULT_OF_SUCCESS = 1

    const val RESULT_WIFI_INFO = "RESULT_WIFI_INFO"

    const val START_FROM_ELEVATOR_SETTING = "START_FROM_ELEVATOR_SETTING"

    const val KEY_BUTTON_MAP_PATH = "key-map.txt"
    const val KEY_BUTTON_MAP_WITH_ELEVATOR_PATH = "key-map-with-elevator.txt"
    const val KEY_BUTTON_MAP_WITH_QRCODE_TASK_PATH = "key-map-with-qrcode-task.txt"


}