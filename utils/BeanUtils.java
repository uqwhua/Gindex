package org.example.utils;

import org.example.beans.BasicBean;
import org.example.beans.TrackBean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class BeanUtils {

    /**
     * 这种格式是时间的表达式是类似于2020-5-31 19:43:23这种形式的，如果不对要再调整
     * 这里的经度和纬度万一反了，可能要记得调整
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static HashMap<String, TrackBean> dataSetToBasicBean(List<String> content) {
        HashMap<String, TrackBean> map = new HashMap<>();
        //int num = 0;
        for (String s : content) {
            String[] dataLine = s.split(",");
            String id = dataLine[0];
            if (map.get(id) == null) {
                TrackBean bean = new TrackBean();
                map.put(id, bean);
            }
            BasicBean temp = new BasicBean();
            temp.setId(dataLine[0]);
            //时间
            temp.setTime(LocalDateTime.parse(dataLine[1], formatter));
            //经度
            temp.setLongitude(Double.parseDouble(dataLine[2]));
            //纬度
            temp.setLatitude(Double.parseDouble(dataLine[3]));
            //初次读入数据，Δ量都设置为0
            temp.setDeltaLatitude(0f);
            temp.setDeltaLongitude(0f);
            temp.setDeltaSecond(0L);
            map.get(id).getPoints().add(temp);
        }
        return map;
    }
}
