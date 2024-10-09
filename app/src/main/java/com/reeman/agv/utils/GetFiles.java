package com.reeman.agv.utils;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GetFiles {


    /**
     * 获取文件名
     */
    public static String[] getFilesWithExtensions(String path) {

        File directory = new File(path);
        String[] extensions = {".mp3",".wav"};
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (String extension : extensions) {
                    if (name.toLowerCase().endsWith(extension.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            }
        });

        if (files == null) {
            return new String[0]; // 如果文件夹为空或者不是一个文件夹，返回空数组
        }

        String[] matchingFiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            int index = files[i].getAbsolutePath().indexOf("music/");
            if (index != -1) {
                String result = files[i].getAbsolutePath().substring(index + 6);
                matchingFiles[i] = result;
            }
        }
        return matchingFiles;
    }


//    /**
//     * 获取到完整路径
//     */
//    public String[] getFilesWithPath(String directoryPath) {
//        File directory = new File(directoryPath);
//        String[] extensions = {".mp3",".wav"};
//        File[] files = directory.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                for (String extension : extensions) {
//                    if (name.toLowerCase().endsWith(extension.toLowerCase())) {
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
//
//        if (files == null) {
//            return new String[0]; // 如果文件夹为空或者不是一个文件夹，返回空数组
//        }
//
//        String[] matchingFiles = new String[files.length];
//        for (int i = 0; i < files.length; i++) {
//            matchingFiles[i] = files[i].getAbsolutePath();
//        }
//
//        return matchingFiles;
//    }

    /**
     * 歌曲对比
     */
    public static boolean containsAllRemoteSongs(List<String> remoteList, List<String> localList) {
        // 将远程列表转换为HashSet以提高查找效率
        HashSet<String> remoteSet = new HashSet<>(localList);

        // 遍历本地列表，检查每首歌曲是否都在远程集合中
        for (String localSong : remoteList) {
            if (!remoteSet.contains(localSong)) {
                return false;
            }
        }

        // 如果遍历完本地列表后都没有返回false，说明本地列表包含远程列表中的所有歌曲
        return true;
    }

//    public static boolean saveFile(List<String> file){
//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
//        File testDir = new File(dir, "musicList");
//        if (!testDir.exists()) {
//            testDir.mkdirs();
//        }
//        for (String files : file){
//            File file1 = new File(testDir.getAbsolutePath() + "/" + files);
//            try {
//                file1.createNewFile();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return true;
//    }

    public static List<String> getFile(){
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File testDir = new File(dir, "musicList");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        List<String> fileNames = new ArrayList<>();
        File[] files = testDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    // 只添加文件，不添加子文件夹
                    fileNames.add(file.getName());
                }
            }
        }

        return fileNames;
    }
}
