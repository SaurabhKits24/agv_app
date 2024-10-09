package com.reeman.agv.utils;


public class OpenMusic {


    /**
     * 检查是否本地有这个音乐
     */
    public static boolean musicLocal(String file,String path){

        String[] list = GetFiles.getFilesWithExtensions(path);
        if(list.length > 0){
            for (String str : list) {
                if (str.equals(file)) {
                    return true;
                }
            }
        }
        return false;
    }


}
