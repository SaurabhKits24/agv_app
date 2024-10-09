package com.youngfeel;

public class yf_gpio_manager {
	public static native int open();
	public static native void set_humansensor_on(int fd);
	public static native void set_humansensor_off(int fd);
	public static native int get_humansensor_onoff(int fd);
	public static native void set_humansensor_time(int fd, int arg);
	public static native int get_humansensor_time(int fd);
	public static native int get_humansensor_value(int fd);
	public static native int set_gpio1_value(int fd, int value);
	public static native int get_gpio1_value(int fd);
	public static native int set_gpio2_value(int fd, int value);
	public static native int get_gpio2_value(int fd);
	public static native int set_gpio3_value(int fd, int value);
	public static native int get_gpio3_value(int fd);
	public static native int set_gpio4_value(int fd, int value);
	public static native int get_gpio4_value(int fd);
	public static native int set_sensor_value(int fd, int value);
	public static native int get_sensor_value(int fd);
	public static native void set_lcd_on(int fd);
	public static native void set_lcd_off(int fd);
	public static native int set_usbhost_value(int fd, int value);
	public static native int set_usbotg_value(int fd, int value);
	public static native int set_fan_value(int fd, int value);
	public static native int get_fan_status(int fd);
	public static native int get_fan_value(int fd);
	static {          
		System.loadLibrary("yfgpio_jni");      
	} 
}
