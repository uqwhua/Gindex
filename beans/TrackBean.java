package org.example.beans;

import java.util.ArrayList;
import java.util.List;

public class TrackBean {

    private static final int initKValue = 1;

    private List<BasicBean> points;
    private int BBid;
    private int BBlevel;

    private List<Integer> gridSeq;
    private List<Integer> gridSeq2;
    private List<Integer> gridSeq4;
    private List<Integer> gridSeq8;
    private List<Integer> gridSeq16;
    private List<Integer> gridSeq32;
    //private List<Integer> gridSeq64;
    //private List<Integer> gridSeq128;
    //private List<Integer> gridSeq256;

    private int k;

    public TrackBean(List<BasicBean> points, int k) {
        this.points = points;
        this.gridSeq = new ArrayList<>();
        this.k = k;
    }

    public TrackBean() {
        this.points = new ArrayList<>();
        this.BBid=-1;
        this.BBlevel=0;
        this.gridSeq = new ArrayList<>();
        this.gridSeq2 = new ArrayList<>();
        this.gridSeq4 = new ArrayList<>();
        this.gridSeq8 = new ArrayList<>();
        this.gridSeq16 = new ArrayList<>();
        this.gridSeq32 = new ArrayList<>();
        //this.gridSeq64 = new ArrayList<>();
        //this.gridSeq128 = new ArrayList<>();
        //this.gridSeq256 = new ArrayList<>();
        this.k = initKValue;
    }

    public List<BasicBean> getPoints() {
        return points;
    }

    public void setPoints(List<BasicBean> points) {
        this.points = points;
    }

    public int getBBid(){ return BBid;}

    public void setBBid(int Bbid){ this.BBid = Bbid;}

    public int getBBlevel(){ return BBlevel;}

    public void setBBlevel(int Bblevel){ this.BBlevel = Bblevel;}

    public void setGridSeq(List<Integer> gridSeq) {this.gridSeq = gridSeq; }

    public void setOtherGridSeq(){
        for (Integer integer : gridSeq) {
            if (!gridSeq2.contains(integer / 4)) {
                gridSeq2.add(integer / 4);
            }
            if (!gridSeq4.contains(integer / 16)) {
                gridSeq4.add(integer / 16);
            }
            if (!gridSeq8.contains(integer / 64)) {
                gridSeq8.add(integer / 64);
            }
            if (!gridSeq16.contains(integer / 256)) {
                gridSeq16.add(integer / 256);
            }

            if (!gridSeq32.contains(integer / 1024)) {
                gridSeq32.add(integer / 1024);
            }
            /*
            if (!gridSeq64.contains(integer / 4096)) {
                gridSeq64.add(integer / 4096);
            }

            if (!gridSeq128.contains(integer / 16384)) {
                gridSeq128.add(integer / 16384);
            }
            if (!gridSeq256.contains(integer / 65536)) {
                gridSeq256.add(integer / 65536);
            }

             */
        }
        /*System.out.println(gridSeq);
        System.out.println(gridSeq2);
        System.out.println(gridSeq4);
        System.out.println(gridSeq8);
        System.out.println(gridSeq16);
        System.out.println(gridSeq32);
         */
    }

    public List<Integer> getGridSeq(){
        return  gridSeq;
    }

    public List<Integer> getGridSeq(int tag){
        if (tag == 64){//64
        return  gridSeq;
        }else if (tag == 32){//32
            return gridSeq2;
        } else if (tag == 16){//16
            return gridSeq4;
        }else if (tag == 8){//8
            return gridSeq8;
        }else if (tag == 4){//4
            return gridSeq16;
        } else {
            return gridSeq32;
        }
        /*else{
            return gridSeq64;
        }
        else if (tag == 4){
           return  gridSeq128;
        }else {
            return gridSeq256;
        }
         */
    }


    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
