package com.book.datamodel;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by ALt+Shift+UP on 2017/6/27.
 */

public class ItemDto implements Serializable {
    private long id;
    private String name;
    private String desc;
    private String comments;

    public ItemDto(long id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
