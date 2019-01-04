package com.iremembr.jtraxxs;

public class ParentValue {
    public static final ParentValue INSTANCE = new ParentValue();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
