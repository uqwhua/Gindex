package org.example.utils;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoaderTool {

    public static Properties springUtil(String path) {
        Properties props = new Properties();
        try {
            String configpath = path + File.separator+ "config.properties";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configpath));
            props.load(bufferedReader);
            //props = PropertiesLoaderUtils.loadAllProperties("config.properties");
            for (Object key : props.keySet()) {
                System.out.print(key + ":");
                System.out.println(props.get(key));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return props;
    }
}
