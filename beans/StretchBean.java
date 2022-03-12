package org.example.beans;

public class StretchBean implements Comparable<StretchBean>{
    String parents;

    Double value;

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public int compareTo(StretchBean o) {
        return value.compareTo(o.getValue());
    }
}
