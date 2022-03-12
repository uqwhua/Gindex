package org.example.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DataSetUtils {
    public static List<String> readDataFromProperties(Properties prop) {
        String content = prop.getProperty("DataSet");
        String[] contents = content.split("\\|");
        return Arrays.asList(contents);
    }

    public static List<String> readDataFromFiles(String path, String encoding, int dataSize) {
        int num =0;
        List<String> list = new ArrayList<>();
        if (path.contains(",")) {
            String[] paths = path.split(",");
            for (String s : paths) {
                list.addAll(readFromFile(s, encoding));
            }
        } else if (isDirectory(path)) {
            for (File file : Objects.requireNonNull(new File(path).listFiles())) {
                list.addAll(readFromFile(file.getAbsolutePath(), encoding));
                num++;
                if(num== dataSize){
                    break;
                }
            }
        } else {
            list = readFromFile(path, encoding);
        }
        return list;
    }

    private static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    private static List<String> readFromFile(String filePath, String encoding) {
        List<String> list = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    list.add(lineTxt);
                }
                bufferedReader.close();
                read.close();
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return list;
    }
}
