package com.iremembr.jtraxxs;

public class ParentMessage {
    public static final ParentMessage INSTANCE = new ParentMessage();

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
