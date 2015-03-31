package com.wangyazhou.todo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wangyazhou.todo.adapter.TodoListAdapter;
import com.wangyazhou.todo.dataAccessor.TodoItem;
import com.wangyazhou.todo.dataAccessor.TodoItemAccessor;
import com.wangyazhou.todo.util.DatetimeUtil;
import com.wangyazhou.todo.util.ImageUtil;

import java.util.Date;
import java.util.Map;

/**
 * The main activity to display the to do list
 */
public class TodoListActivity extends ActionBarActivity {
    private ListView todoList = null;

    private Button testButtonAdd, testButtonDelete, testButtonUpdate;

    private TodoItemAccessor todoItemAccessor;

    private TodoListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        todoItemAccessor = new TodoItemAccessor(this);
        initializeWidgets();
    }

    protected void initializeWidgets(){
        todoList = (ListView) this.findViewById(R.id.todo_list_list);
        listAdapter = new TodoListAdapter(this, todoItemAccessor);
        todoList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        testButtonAdd = (Button) this.findViewById(R.id.button_add);
        testButtonDelete = (Button) this.findViewById(R.id.button_delete);
        testButtonUpdate = (Button) this.findViewById(R.id.button_update);

        testButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TodoItem item = new TodoItem();
                item.setDescription("Test" + DatetimeUtil.getNowDatetime());
                item.setCreateDatetime(DatetimeUtil.getNowDatetime());
                item.setIsDone(0);
                todoItemAccessor.insertTodoItem(item);
                listAdapter.notifyDataSetChanged();
            }
        });
        testButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listAdapter.getCount() <= 0){
                    return;
                }
                Map<String, Object> map = (Map<String, Object>) listAdapter.getItem(0);
                TodoItem item = (TodoItem) map.get(TodoListAdapter.MAP_KEY_ITEM);
                todoItemAccessor.deleteTodoItem(item);
                listAdapter.notifyDataSetChanged();
            }
        });
        testButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listAdapter.getCount() <= 0){
                    return;
                }
                TodoItem item = (TodoItem) listAdapter.getItem(0);
                item.setDescription("Updated!" + DatetimeUtil.getNowDatetime());
                item.setIsDone(TodoItem.VALUE_IS_DONE);
                todoItemAccessor.updateTodoItem(item);
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_todo_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        todoItemAccessor.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActionHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            TodoItem item = listAdapter.getItem(listAdapter.getActiveItemPosition());
            item.setThumbnail(ImageUtil.Bitmap2Bytes(imageBitmap));
            listAdapter.updateItem(listAdapter.getActiveItemPosition(), item);
        } else if (requestCode == ActionHelper.REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
        }
    }
}
