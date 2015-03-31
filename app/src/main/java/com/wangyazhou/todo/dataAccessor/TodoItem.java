package com.wangyazhou.todo.dataAccessor;

public class TodoItem {
    public static final String TABLE_NAME = "TODO_ITEMS_TABLE";
    public static final String KEY_ID = "ID";
    public static final String KEY_IS_DONE = "IS_DONE";
    public static final String KEY_DESCRIPTION = "DESCRIPTION";
    public static final String KEY_THUMBNAIL = "THUMBNAIL";
    public static final String KEY_FULL_IMAGE = "FULL_IMAGE";
    public static final String KEY_CREATE_DATETIME = "CREATE_DATETIME";
    public static final String KEY_UPDATE_DATETIME = "UPDATE_DATETIME";
    public static final String KEY_DONE_DATETIME = "DONE_DATETIME";

    public static final int VALUE_IS_DONE = 1;
    public static final int VALUE_NOT_DONE = 0;

    private long id;
    private int isDone;
    private String description;
    private byte[] thumbnail;
    private byte[] fullImage;
    private long createDatetime;
    private long updateDatetime;
    private long doneDatetime;

    public TodoItem() {
        // empty constructor
    }

    public int getIsDone() {
        return isDone;
    }

    public void setIsDone(int isDone) {
        this.isDone = isDone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public long getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(long createDatetime) {
        this.createDatetime = createDatetime;
    }

    public long getUpdateDatetime() {
        return updateDatetime;
    }

    public void setUpdateDatetime(long updateDatetime) {
        this.updateDatetime = updateDatetime;
    }

    public long getDoneDatetime() {
        return doneDatetime;
    }

    public void setDoneDatetime(long doneDatetime) {
        this.doneDatetime = doneDatetime;
    }

    public byte[] getFullImage() {
        return fullImage;
    }

    public void setFullImage(byte[] fullImage) {
        this.fullImage = fullImage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "id=" + id +
                ", isDone=" + isDone +
                ", description='" + description + '\'' +
                ", createDatetime=" + createDatetime +
                ", updateDatetime=" + updateDatetime +
                ", doneDatetime=" + doneDatetime +
                '}';
    }
}
