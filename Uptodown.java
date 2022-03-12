package org.example;

import org.apache.commons.lang3.StringUtils;
import org.example.algorith.Merge;
import org.example.beans.BasicBean;
import org.example.beans.StretchBean;
import org.example.beans.TrackBean;
import org.example.utils.BeanUtils;
import org.example.utils.DataSetUtils;
import org.example.utils.PropertiesLoaderTool;
import org.example.utils.TrajGridLen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Uptodown {
    /**
     * 算法开始的入口，所需要的数据我使用两个来源：配置文件config.properties和外部文件
     * 1.配置文件中有Anonymization_Level_k的预定值，可以通过配置文件修改
     * 2.配置文件中有配置参数MobileFingerprint_root参数，如果为空则读取配置文件自己的dataset，如果不为空则读取路径对应的文件
     * 3.配置文件中有配置参数AnonymizedFingerprint_root参数，算法输出结果会输入到该路径下
     */

    private static final Logger logger = LoggerFactory.getLogger(Uptodown.class);

    public static void main(String[] args) {

        //long startTime1 = System.currentTimeMillis();    //获取开始时间
        /*
          前期的参数配置读取，都可以调，改配置文件就是了
         */
            String path = System.getProperty("user.dir");
            Properties prop = PropertiesLoaderTool.springUtil(path);
            //读取Anonymization_Level_k的值
            int anonymizationLevelK = Integer.parseInt(prop.getProperty("Anonymization_Level_k"));
            int dataSize = Integer.parseInt(prop.getProperty("DataSize"));
            //读取需要计算的数据的路径，如果没有就读配置文件自带的,配置文件里如果有多个文件就用"|"隔开
            List<String> dataSet = StringUtils.isBlank(prop.getProperty("MobileFingerprint_root")) ?
                    DataSetUtils.readDataFromProperties(prop) :
                    DataSetUtils.readDataFromFiles(prop.getProperty("MobileFingerprint_root"), prop.getProperty("fileEncoding"),dataSize);
            //输出文件的地址
            String destinationroot = prop.getProperty("AnonymizedFingerprint_root")+ "toptodown_"+dataSize+"_"+anonymizationLevelK+".txt";


            File destination = new File(destinationroot);
            HashMap<String, TrackBean> dataMap = BeanUtils.dataSetToBasicBean(dataSet);
            logger.info("Successfully read trajectory number : {}. And Gridding begin.",dataMap.size());
            //Map范围
            double StartLo = 114.4;
            double EndLo = 118.4;
            double StartLa = 38.5;
            double EndLa = 40.7;//40.7,41.5
            /*
             *算法开始
             */
            //构造数据结构
            //获得所有轨迹按z-order编号的grid sequence同时获得grid Map
            HashMap<Integer, LinkedList<String>> FgridMap = new HashMap<>();
            int trajLenth=0;
            int gridseqlenth =0;
            HashMap<Integer, HashMap<Integer, LinkedList<String>>> BBMap = new HashMap<>();
            for(int lev =1;lev <= 64; lev=lev *2){
                HashMap<Integer, LinkedList<String>> bbmap = new HashMap<>();
                BBMap.put(lev,bbmap);
            }
            List<TrajGridLen> gridLens = new ArrayList<>();
            for (String id : dataMap.keySet()) {
                List<Integer> gridseq = new LinkedList<>();
                double MaxLo = 114.4;
                double MinLo = 118.4;
                double MaxLa = 38.5;
                double MinLa = 40.7;
                List<BasicBean> pointseq = dataMap.get(id).getPoints();
                trajLenth = pointseq.size()+trajLenth;
                for (BasicBean temp : pointseq) {
                    double Lo = temp.getLongitude(), La = temp.getLatitude();
                    if (Lo < StartLo) {
                        Lo = StartLo;
                    } else if (Lo > EndLo) {
                        Lo = EndLo;
                    }
                    if (La < StartLa) {
                        La = StartLa;
                    } else if (La > EndLa) {
                        La = EndLa;
                    }
                    //get coordinate for bounding box
                    if (Lo < MinLo) {
                        MinLo = Lo;
                    }
                    if (Lo > MaxLo) {
                        MaxLo = Lo;
                    }
                    if (La < MinLa) {
                        MinLa = La;
                    }
                    if (La > MaxLa) {
                        MaxLa = La;
                    }
                    double rowD = (La - StartLa) / ((EndLa - StartLa) / 64);
                    int row = (int) rowD;
                    if(row == 64){ row = row-1; }
                    double colD = (Lo - StartLo) / ((EndLo - StartLo) / 64);
                    int col = (int) colD;
                    if(col == 64){ col = col-1; }
                    int z = 0;
                    for (int i = 0; i < 31; i++) {
                        z = z | (row & 1 << i) << (i + 1) | (col & 1 << i) << i;
                    }
                    //System.out.println(row);
                    //System.out.println(col);
                    //System.out.println(z);
                    int tag = 0;
                    for (Integer integer : gridseq) {
                        if (integer == z) {
                            tag = 1;
                            break;
                        }
                    }
                    if (tag == 0) {
                        gridseq.add(z);
                    }
                    //对应轨迹id加入gridMap
                    if (FgridMap.containsKey(z)) {
                        tag = 0;
                        for (String jid : FgridMap.get(z)) {
                            if (Objects.equals(jid, id)) {
                                tag = 1;
                                break;
                            }
                        }
                        if (tag == 0) {
                            FgridMap.get(z).add(id);
                        }
                    } else {
                        LinkedList<String> jidseq = new LinkedList<>();
                        jidseq.add(id);
                        FgridMap.put(z, jidseq);
                    }
                    //System.out.println(Collections.max(FgridMap.keySet()));
                }
                //排序优化
                gridseq.sort(Comparator.naturalOrder());
                //System.out.println(MaxLo+","+MinLo+","+MaxLa+","+MinLa);
                int[] Bb = getBoundingbox(MaxLo,MinLo,MaxLa,MinLa);//double maxLo, double minLo, double maxLa, double minLa
                dataMap.get(id).setGridSeq(gridseq);
                dataMap.get(id).setOtherGridSeq();
                dataMap.get(id).setBBid(Bb[0]);
                dataMap.get(id).setBBlevel(Bb[1]);
                //System.out.println(Bb[1]+","+Bb[0]);
                //if(Bb[0]<0){ System.out.println("maxlo:"+MaxLo+",minlo:"+MinLo+",maxa:"+MaxLa+",minla:"+MinLa);}
                if (BBMap.get(Bb[1]).containsKey(Bb[0])) {
                    BBMap.get(Bb[1]).get(Bb[0]).add(id);
                } else {
                    LinkedList<String> Bbjidseq = new LinkedList<>();
                    Bbjidseq.add(id);
                    BBMap.get(Bb[1]).put(Bb[0], Bbjidseq);
                }
                gridseqlenth = gridseq.size()+gridseqlenth;
                gridLens.add(new TrajGridLen(id, gridseq.size()));
                if(gridLens.size()%100 == 0 ){
                    logger.info("Number of processed trajectory: {}",gridLens.size());
                }
            }

            /*for(int level: BBMap.keySet()){
                int trajnmu = 0;
                for (int znum: BBMap.get(level).keySet()) {
                    trajnmu = trajnmu+ BBMap.get(level).get(znum).size();
                }
                System.out.println("Before BBmap level+size:"+level+","+trajnmu);
            }*/
            //填充完整BBMap
            ComplateBBmap(BBMap);
            /*for(int level: BBMap.keySet()){
                int trajnmu = 0;
                for (int znum: BBMap.get(level).keySet()) {
                    trajnmu = trajnmu+ BBMap.get(level).get(znum).size();
                }
                System.out.println("After BBmap level+size:"+level+","+trajnmu );
            }*/
            //System.out.println(FgridMap.size());
            System.out.println("every trajLenth:"+trajLenth/dataMap.size()+", every gridseqlenth:"+gridseqlenth/dataMap.size());
            //排序优化
            //Collections.sort(gridLens);
            LinkedList<String> SortedTrajs = new LinkedList<>();
            for (TrajGridLen gridLen : gridLens) {
                SortedTrajs.add(gridLen.getTrajId());
            }
            //System.out.println(SortedTrajs);
            //构建包含所有size的grdMap的集合；
            HashMap<Integer, HashMap<Integer, LinkedList<String>>> gridMap = new HashMap<>();
            //gridMap.put(512, FgridMap);//512X512
            //HashMap<Integer, LinkedList<String>> Map256 = getDublesizeMap(FgridMap);//256X256
            //gridMap.put(256, Map256);
            //HashMap<Integer, LinkedList<String>> Map128 = getDublesizeMap(Map256);//128X128
            //gridMap.put(128, FgridMap);
            //HashMap<Integer, LinkedList<String>> Map64 = getDublesizeMap(FgridMap);//64X64
            gridMap.put(64, FgridMap);
            //System.out.println(Collections.max(FgridMap.keySet()));
            HashMap<Integer, LinkedList<String>> DoubleMap = getDublesizeMap(FgridMap);//32X32
            gridMap.put(32, DoubleMap);
            //System.out.println(DoubleMap.size());
            HashMap<Integer, LinkedList<String>> FourMap = getDublesizeMap(DoubleMap);//16X16
            gridMap.put(16, FourMap);
            //System.out.println(FourMap.size());
            HashMap<Integer, LinkedList<String>> EightMap = getDublesizeMap(FourMap);//8X8
            gridMap.put(8, EightMap);
            //System.out.println(EightMap.size());
            HashMap<Integer, LinkedList<String>> SixtMap = getDublesizeMap(EightMap);//4X4
            gridMap.put(4, SixtMap);
            //System.out.println(SixtMap.size());
            HashMap<Integer, LinkedList<String>> ThirtMap = getDublesizeMap(SixtMap);//2X2
            gridMap.put(2, ThirtMap);
            //System.out.println(gridMap.keySet());
            logger.info("Start Searching and Merging");

            //循环查找candidates并对其进行合并
            HashMap<String, TrackBean> outputMap = new HashMap<>();
            LinkedList<String> RemovList = new LinkedList<>();
            //System.out.println("SearchlistBegin："+SortedTrajs);
            long startTime1 = System.currentTimeMillis();    //获取开始时间
            long updatetime=0;
            long mergetime =0;
            //for (int tri = 0; tri < SortedTrajs.size(); tri++) {
            for (int tri = 0; tri < SortedTrajs.size(); ) {
                //System.out.println("SearchlistNow："+SortedTrajs);

                LinkedList<String> candidateId = new LinkedList<>();
                String id = SortedTrajs.get(tri);
                int tag = 64;
                TrackBean i = dataMap.get(id);
                //System.out.println("now trj:"+id);
                //System.out.println("Removelist before:"+RemovList);
                int level = i.getBBlevel();
                int bid = i.getBBid();
                LinkedList<String> Searchlist = BBMap.get(level).get(bid);
                //System.out.println(Searchlist.size());
                /*if(Searchlist.size() < anonymizationLevelK - 1){
                System.out.println("当前轨迹level和bid："+level+","+bid);
                System.out.println("size before"+Searchlist.size());
                //System.out.println("BBmapsize before"+BBMap.get(1).size());
                }*/
                while (Searchlist.size() < anonymizationLevelK - 1){

                    level = level/2;
                    bid = bid/4;
                    if(level == 0){ break;}
                    //System.out.println(level+","+bid);
                    Searchlist = BBMap.get(level).get(bid);
                    //System.out.println("size now:"+Searchlist.size());
                }
                while (candidateId.size() < anonymizationLevelK - 1 && tag> 1) {
                    //System.out.println(i.getGridSeq());
                    //System.out.println("RemovList:"+RemovList);
                    LinkedList<String> SearchList = getTrajIdfromBBMap(Searchlist,RemovList);
                    //LinkedList<String> SearchList = getTrajIdfromMap(i.getGridSeq(tag), gridMap.get(tag), RemovList, dataMap);
                    SearchList.remove(id);
                    //System.out.println("SearchList:"+SearchList);
                    for (String s : SearchList) {
                        if (MatchTraj(i.getGridSeq(tag), dataMap.get(s).getGridSeq(tag))) {
                            //System.out.println("now trj"+id+" and "+s+" Matched in tag "+tag);
                            candidateId.add(s);
                            RemovList.add(s);
                        }
                        if (candidateId.size() == anonymizationLevelK - 1) {
                            break;
                        }
                    }
                    tag = tag / 2;
                }
                /*
                if (candidateId.size() != anonymizationLevelK - 1){
                    tag = tag*2;
                    //System.out.println("now trj:"+id+",gridSeq"+dataMap.get(id).getGridSeq());
                    System.out.println("now trj:"+id+"now tag:"+tag+",gridSeq"+dataMap.get(id).getGridSeq(tag));
                    for (String s : candidateId) {
                        //System.out.println("candidate "+s+" candidate gridSeq"+dataMap.get(s).getGridSeq());
                        System.out.println("candidate "+s+" in tag:"+tag+"candidate gridSeq"+dataMap.get(s).getGridSeq(tag));
                    }
                    for(String map: SortedTrajs){
                        System.out.println("traj "+map+" in tag:"+tag+"candidate gridSeq"+dataMap.get(map).getGridSeq(tag));
                    }
                }

                 */
                //合并当前轨迹和它的candidates
                //System.out.println("now trj:"+id+",candidate"+candidateId);
                long updatetemp =0;
                long startmerge = System.currentTimeMillis();//合并
                if (candidateId.size() == anonymizationLevelK - 1 || tag <2) {
                    TrackBean m = dataMap.get(id);
                    String StrId = m.getPoints().get(0).getId();
                    //System.out.println("before merge:"+candidateId);
                    for (String s : candidateId) {
                        TrackBean c = dataMap.get(s);
                        StrId = StrId + "a" + c.getPoints().get(0).getId();
                        TrackBean tempt = Merge.mergeTrack(m, c, StrId);
                        tempt.setK(m.getK() + c.getK());
                        m = tempt;
                        long startupdate= System.currentTimeMillis();//合并
                        dataMap.remove(s);
                        SortedTrajs.remove(s);//移除已经在候选集合中出现的轨迹
                        long endupdate = System.currentTimeMillis();//合并
                        //RemovList.add(s);
                        updatetemp = endupdate - startupdate + updatetemp;
                    }
                    //System.out.println("new candidate:"+candidateId);
                    outputMap.put(m.getPoints().get(0).getId(), m);
                    //System.out.println("after merge:"+m.getPoints().get(0).getId());
                    //dataMap.remove(id);
                    long startupdate= System.currentTimeMillis();
                    SortedTrajs.remove(id);
                    RemovList.add(id);
                    //RemovefromGridmap(RemovList,gridMap);
                    //RemovefromBBmap(RemovList,BBMap);
                    //System.out.println(SortedTrajs);
                    long endupdate = System.currentTimeMillis();
                    updatetemp = endupdate - startupdate + updatetemp;
                }
                long endmerge = System.currentTimeMillis();
                updatetime= updatetime+updatetemp;
                mergetime = endmerge - startmerge - updatetemp + mergetime;
            }
            //System.out.println(dataMap.size());
            //System.out.println(outputMap.size());
        long endTime1 = System.currentTimeMillis();    //获取结束时间
        System.out.println("Runtime：" + (endTime1 - startTime1) + "ms");    //输出程序运行时间s
        System.out.println("Mergetime：" + mergetime + "ms");
        System.out.println(outputMap.size());
        //最后输出outputMap
        try {
            write(outputMap, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //long endTime1 = System.currentTimeMillis();    //获取结束时间
        //System.out.println("Runtime：" + (endTime1 - startTime1) + "ms");    //输出程序运行时间s
        logger.info("Finish");
    }

    private static void RemovefromBBmap(LinkedList<String> removList, HashMap<Integer, HashMap<Integer, LinkedList<String>>> bbMap) {
        int tag=64;
        while (tag > 1){
            for (String s : removList) {
                for (Integer z : bbMap.get(tag).keySet()) {
                    bbMap.get(tag).get(z).remove(s);
                }
            }
            tag = tag/2;
        }
    }

    private static void RemovefromGridmap(LinkedList<String> removList, HashMap<Integer, HashMap<Integer, LinkedList<String>>> gridMap) {
        int tag=64;
        while (tag > 1){
            for (String s : removList) {
                for (Integer z : gridMap.get(tag).keySet()) {
                    gridMap.get(tag).get(z).remove(s);
                }
            }
            tag = tag/2;
        }
    }

    private static LinkedList<String> getTrajIdfromBBMap(LinkedList<String> searchlist, LinkedList<String> removList) {
        LinkedList<String> result = searchlist;
        result.removeIf(removList::contains);
        return result;
    }

    private static void ComplateBBmap(HashMap<Integer, HashMap<Integer, LinkedList<String>>> bbMap) {
        int level =64;
        while (level>=2){
            for (Integer z:bbMap.get(level).keySet()) {
                LinkedList<String> bbtralist=bbMap.get(level).get(z);
                for(String traid: bbtralist) {
                    //System.out.println(level);
                    //System.out.println(z);
                    if (bbMap.get(level / 2).containsKey(z / 4)) {
                        if (!bbMap.get(level / 2).get(z / 4).contains(traid)) {
                            bbMap.get(level / 2).get(z / 4).add(traid);
                        }
                    }else {
                        LinkedList<String> tralist = new LinkedList<>();
                        tralist.add(traid);
                        bbMap.get(level / 2).put((z/4),tralist);
                    }
                }
            }
            level= level/2;
        }
    }

    private static int[] getBoundingbox(double maxLo, double minLo, double maxLa, double minLa) {
        int zmax = 0,zmin=0;
        double StartLo = 114.4;
        double EndLo = 118.4;
        double StartLa = 38.5;
        double EndLa = 40.7;//40.7,41.
        double rowD = (maxLa - StartLa) / ((EndLa - StartLa) / 64);
        int row = (int) rowD;
        if(row == 64){ row = row-1; }
        double colD = (maxLo - StartLo) / ((EndLo - StartLo) / 64);
        int col = (int) colD;
        if(col == 64){ col = col-1; }
        for (int i = 0; i < 30; i++) {
            zmax = zmax | (row & 1 << i) << (i + 1) | (col & 1 << i) << i;
        }
        rowD = (minLa - StartLa) / ((EndLa - StartLa) / 64);
        row = (int) rowD;
        if(row == 64){ row = row-1; }
        colD = (minLo - StartLo) / ((EndLo - StartLo) / 64);
        col = (int) colD;
        if(col == 64){ col = col-1; }
        //System.out.println(row+","+col);
        for (int i = 0; i < 30; i++) {
            zmin = zmin | (row & 1 << i) << (i + 1) | (col & 1 << i) << i;
        }
        int level =64 ;

        //System.out.println(zmax+","+zmin);
        while(!(zmax==zmin) && level>=2){
            zmax=zmax/4;
            zmin=zmin/4;
            level = level/2;
            //System.out.println(zmax+","+zmin);
            //System.out.println(level);
        }
        //System.out.println(zmax+","+zmin);
        return new int[]{zmax,level};
    }

    //两个序列匹配
    private static boolean MatchTraj(List<Integer> gridSeq1, List<Integer> gridSeq2) {
        List<Integer> longer = gridSeq1.size() > gridSeq2.size() ? gridSeq1 : gridSeq2;
        List<Integer> shorter = gridSeq1.size() < gridSeq2.size() ? gridSeq1 : gridSeq2;
        if (gridSeq1.size() == gridSeq2.size()){
            longer = gridSeq1;
            shorter = gridSeq2;
        }
        //int multiple = (int)Math.pow(4,(64/tag - 1));

        int flag =0;
        for(int i =0;i < shorter.size();i++){
            for(int j = 0 ;j < longer.size();j++) {
                if (shorter.get(i).equals(longer.get(j))) {
                    flag ++;
                    break;
                }
            }
        }
        if(flag == shorter.size()){
            return true;
        }else {
            return false;
        }

        /*
        int flag =0;
        int sum =0;
        for (int i =0;i < shorter.size();i++){
            int j;
            //System.out.println(shorter.get(i));

            for(j = flag ;j <longer.size();j++) {
                //System.out.println(shorter.get(i)+" "+longer.get(j));
                if (shorter.get(i).equals(longer.get(j))) {
                    flag = j;
                    sum++;
                    break;
                }
            }
            if((j == longer.size()-shorter.size()-i) && !shorter.get(i).equals(longer.get(j))){
                return false;
            }
        }
        if(sum == shorter.size()){
            return true;
        }else {
            return false;
        }
        */
        /*
        int flag =0;
        int sum =0;
        for (int i =0;i < shorter.size();i++){
            int j;
            for(j = flag ;j <= (longer.size()-shorter.size()+i );j++) {
                //System.out.println(shorter.get(i)+" "+longer.get(j));
                if (shorter.get(i).equals(longer.get(j))) {
                    flag = j;
                    sum++;
                    break;
                } else if (shorter.get(i) < (longer.get(j))) {
                    return false;
                }
            }
            if((j == longer.size()-shorter.size()-i) && !shorter.get(i).equals(longer.get(j))){
                return false;
            }
        }
        if(sum == shorter.size()){
            return true;
        }else {
            return false;
        }*/
    }


    //tag层次下轨迹i的方格序列gridseq包含的所有grid，获得经过这些grid的轨迹Id
    private static LinkedList<String> getTrajIdfromMap(List<Integer> gridSeq, HashMap<Integer, LinkedList<String>> gridMap, LinkedList<String> removelist, HashMap<String, TrackBean> dataMap)  {
        LinkedList<String> result = new LinkedList<>();
        LinkedList<String> templ = new LinkedList<>();
        List<TrajGridLen> sortLens = new ArrayList<>();

        for (Integer integer : gridSeq) {
            //int multiple = (int)Math.pow(4,(64/tag - 1));
            templ =  mergeAndDistinct(result, (gridMap.get(integer)));
            result = templ;
        }
        //System.out.println(result.size());
        for (int i =0;i <result.size();i++){
            if(removelist.contains(result.get(i))){
                result.remove(i);
                i--;
            }else {
                sortLens.add(new TrajGridLen(result.get(i),dataMap.get(result.get(i)).getGridSeq().size()));
            }
        }
        //排序优化
        Collections.sort(sortLens);
        LinkedList<String> returndata = new LinkedList<>();
        for(TrajGridLen gridLen : sortLens) {
            returndata.add(gridLen.getTrajId());
        }
        //System.out.println("returnlist"+returndata);
        return returndata;
    }


    private static HashMap<Integer, LinkedList<String>> getDublesizeMap(HashMap<Integer, LinkedList<String>> fgridMap) {
        HashMap<Integer,LinkedList<String>> newMap = new HashMap<>();
        for(Integer gridId : fgridMap.keySet()){
            int newGid = gridId/4;
            /*if(fgridMap.get(gridId).isEmpty()){
                System.out.println(fgridMap.get(gridId));
                System.out.println(gridId);
            }*/
            LinkedList<String> trajid = new LinkedList<>(fgridMap.get(gridId));
            if(newMap.containsKey(newGid)){
                LinkedList<String> tempList = mergeAndDistinct(newMap.get(newGid),trajid);
                newMap.replace(newGid, tempList);
            }else {
                newMap.put(newGid,trajid);
            }
        }
        return newMap;
    }

    public static LinkedList<String> mergeAndDistinct(LinkedList<String> a,LinkedList<String> b){
        HashSet<String> set = new HashSet<String>();
        if(a.isEmpty()){
            return b;
        }
        if(b.isEmpty()){
            return a;
        }
        set.addAll(a);
        set.addAll(b);
        LinkedList<String> result =  new LinkedList<String>();
        result.addAll(set);
        return  result;
    }


    /**
     * 只剩最后一个，或者只要原始数据集种存在还有两条，他们的k值小于给定值，就要继续merge
     *
     * @param dataMap             读出来的结果

     * @return 别管
     */

    public static void write(HashMap<String, TrackBean> dataMap, File destination) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (!destination.exists()) {
            boolean wasSuccessful = destination.createNewFile();
            if (!wasSuccessful) {
                System.out.println("File creation was not successful.");
            }
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(destination));
        for (TrackBean value : dataMap.values()) {
            for (BasicBean point : value.getPoints()) {
                String str = point.getId() + "," +
                        dtf.format(point.getTime()) + "," +
                        point.getDeltaSecond() + "," +
                        point.getLatitude() + "," +
                        point.getDeltaLatitude() + "," +
                        point.getLongitude() + "," +
                        point.getDeltaLongitude();
                out.write(str + "\r\n");
            }
        }
        out.flush();
        out.close();
    }

    private static void removeTraceRecord(String traceName, HashMap<String, TrackBean> dataMap, LinkedList<StretchBean> matrix) {
        matrix.removeIf(bean -> {
            String[] split = bean.getParents().split("\\+");
            return Arrays.asList(split).contains(traceName);
        });
        dataMap.remove(traceName);
    }


    private static boolean isSatisfyLoopCondition(HashMap<String, TrackBean> dataMap, int anonymizationLevelK) {
        int count = 0;
        if (dataMap.size() < 2)
            return false;
        for (String id : dataMap.keySet()) {
            if (dataMap.get(id).getK() < anonymizationLevelK) {
                count++;
                if (count > 2)
                    break;
            }
        }
        return count >= 2;
    }
}
