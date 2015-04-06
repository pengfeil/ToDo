package com.wangyazhou.todo.dataAccessor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wangyazhou.todo.util.DatetimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodoItemAccessor {
    private TodoDBHelper helper;
    private SQLiteDatabase database;

    public TodoItemAccessor(Context context) {
        helper = new TodoDBHelper(context);
        database = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    public boolean insertTodoItem(TodoItem item) {
        ContentValues contentValues = todoItem2ContentValues(item);
        item.setCreateDatetime(DatetimeUtil.getNowDatetime());
        long rowId = database.insert(TodoItem.TABLE_NAME, null, contentValues);
        return rowId >= 0;
    }

    public TodoItem[] listTodoItem(long startId, int limit, boolean isArchived) {
        //TODO: may need to exclude the full image from selection.
        long archiveThreshold = new Date().getTime() - DatetimeUtil.TWO_DAY_MILLI_SEC;
        String whereClause = TodoItem.KEY_ID + ">? AND (" + ( isArchived
                // items have been done before two days
                ? TodoItem.KEY_IS_DONE + "=? AND " + TodoItem.KEY_DONE_DATETIME + "<?)"
                // items have not been done or been done in nearest two days
                : TodoItem.KEY_IS_DONE + "=? OR " + TodoItem.KEY_DONE_DATETIME + ">=?)" );
        String[] whereArgs = isArchived
                ? new String[]{startId + "", TodoItem.VALUE_IS_DONE + "", archiveThreshold + ""}
                : new String[]{startId + "", TodoItem.VALUE_NOT_DONE + "", archiveThreshold + ""};
        String orderBy = TodoItem.KEY_IS_DONE + " ASC, " + TodoItem.KEY_CREATE_DATETIME + " ASC";

        Cursor cursor = database.query(true, TodoItem.TABLE_NAME, null, whereClause, whereArgs, null, null, orderBy, limit + "");
        cursor.moveToFirst();
        List<TodoItem> items = new ArrayList<TodoItem>(cursor.getCount());
        while(!cursor.isAfterLast()){
            TodoItem item = new TodoItem();
            item.setId(cursor.getLong(cursor.getColumnIndex(TodoItem.KEY_ID)));
            item.setDescription(cursor.getString(cursor.getColumnIndex(TodoItem.KEY_DESCRIPTION)));
            item.setIsDone(cursor.getInt(cursor.getColumnIndex(TodoItem.KEY_IS_DONE)));
            item.setThumbnail(cursor.getBlob(cursor.getColumnIndex(TodoItem.KEY_THUMBNAIL)));
            item.setCreateDatetime(cursor.getLong(cursor.getColumnIndex(TodoItem.KEY_CREATE_DATETIME)));
            item.setUpdateDatetime(cursor.getLong(cursor.getColumnIndex(TodoItem.KEY_UPDATE_DATETIME)));
            item.setDoneDatetime(cursor.getLong(cursor.getColumnIndex(TodoItem.KEY_DONE_DATETIME)));
            items.add(item);
            cursor.moveToNext();
        }
        return items.toArray(new TodoItem[0]);
    }

    public boolean updateTodoItem(TodoItem item) {
        item.setUpdateDatetime(DatetimeUtil.getNowDatetime());
        if(item.getIsDone() == TodoItem.VALUE_IS_DONE){
            item.setDoneDatetime(DatetimeUtil.getNowDatetime());
        }
        ContentValues contentValues = todoItem2ContentValues(item);
        int updateCount = database.update(TodoItem.TABLE_NAME, contentValues, TodoItem.KEY_ID + "=?", new String[]{item.getId() + ""});
        return updateCount > 0;
    }

    public boolean deleteTodoItem(TodoItem item) {
        int deleteCount = database.delete(TodoItem.TABLE_NAME, TodoItem.KEY_ID + "=?", new String[]{item.getId() + ""});
        return deleteCount > 0;
    }

    private ContentValues todoItem2ContentValues(TodoItem item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TodoItem.KEY_IS_DONE, item.getIsDone());
        contentValues.put(TodoItem.KEY_DESCRIPTION, item.getDescription());
        contentValues.put(TodoItem.KEY_THUMBNAIL, item.getThumbnail());
        contentValues.put(TodoItem.KEY_FULL_IMAGE, item.getFullImage());
        contentValues.put(TodoItem.KEY_CREATE_DATETIME, item.getCreateDatetime());
        contentValues.put(TodoItem.KEY_UPDATE_DATETIME, item.getUpdateDatetime());
        contentValues.put(TodoItem.KEY_DONE_DATETIME, item.getDoneDatetime());
        return contentValues;
    }
}
