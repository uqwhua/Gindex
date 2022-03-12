package org.example.utils;

public class TrajGridLen implements Comparable<TrajGridLen>{
    private int SeqLenth;
    private String TrajId;
    public TrajGridLen( String Tid,int lenth){
        this.SeqLenth = lenth;
        this.TrajId = Tid;
    }
    public String getTrajId(){
        return TrajId;
    }
    @Override
    public int compareTo(TrajGridLen o){
        //升序
        return this.SeqLenth - o.SeqLenth;
    }
    @Override
    public String toString(){
        return "Traject{"+ "id="+TrajId+",lenth= "+SeqLenth+'\''+'}';
    }
}
