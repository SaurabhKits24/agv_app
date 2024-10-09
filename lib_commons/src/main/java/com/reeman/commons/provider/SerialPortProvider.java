package com.reeman.commons.provider;

import timber.log.Timber;

public class SerialPortProvider {

    /**
     * 底盘串口
     *
     * @param product
     * @return
     */
    public static String ofChassis(String product) {
        Timber.w("product : %s", product);
        if ("YF3568_XXXE".equals(product)) {
            return "/dev/ttyS4";
        } else if ("rk3399_all".equals(product)) {
            return "/dev/ttyXRUSB0";
        } else if ("rk312x".equals(product)) {
            return "/dev/ttyS1";
        } else if ("YF3566_XXXD".equals(product)){
            return "/dev/ttyS5";
        }else {
            throw new RuntimeException("unknown device");
        }
    }

    /**
     * ESP模块串口
     *
     * @param product
     * @return
     */
    public static String ofESPModule(String product) throws Exception {
        if ("YF3568_XXXE".equals(product)) {
            return "/sys/devices/platform/fd880000.usb/usb2/2-1/2-1.3/2-1.3:1.0/";
        } else if ("rk3399_all".equals(product)) {
            return "/sys/devices/platform/fe380000.usb/usb1/1-1/1-1.3/1-1.3:1.0";
        } else if ("rk312x".equals(product)){
            return "/sys/bus/usb/devices/1-1.3/1-1.3:1.0";
        }else if ("YF3566_XXXD".equals(product)) {
            return "/sys/bus/usb/devices/3-1/3-1:1.0";
        }else {
            throw new RuntimeException("unknown device");
        }
    }

    /**
     * 呼叫模块串口
     *
     * @param product
     * @return
     */
    public static String ofCallModule(String product){
        if ("YF3568_XXXE".equals(product)) {
            return "/sys/devices/platform/fd880000.usb/usb2/2-1/2-1.1/2-1.1:1.0";
        } else if ("rk3399_all".equals(product)) {
            return "/sys/devices/platform/fe380000.usb/usb1/1-1/1-1.2/1-1.2:1.0";
        } else if ("rk312x".equals(product)) {
            return "/sys/bus/usb/devices/1-1.2/1-1.2:1.0";
        }else if ("YF3566_XXXD".equals(product)) {
            return "/sys/bus/usb/devices/4-1/4-1:1.0";
        }else {
            throw new RuntimeException("unknown device");
        }
    }


    public static String ofDoorControl(String product) {
        if ("YF3568_XXXE".equals(product)) {
            return "/sys/devices/platform/fd880000.usb/usb2/2-1/2-1.2/2-1.2:1.0";
        } else if ("rk3399_all".equals(product)) {
//            return "/sys/devices/platform/fe380000.usb/usb1/1-1/1-1.6/1-1.6:1.0";
            return "/sys/devices/platform/fe3e0000.usb/usb4/4-1/4-1:1.0";
        } else if ("rk312x".equals(product)) {
            return "/sys/bus/usb/devices/1-1.4/1-1.4:1.0";
        } else if ("YF3566_XXXD".equals(product)) {
            return "/sys/bus/usb/devices/5-1/5-1:1.0";
        }else {
            throw new RuntimeException("unknown device");
        }
    }

}
