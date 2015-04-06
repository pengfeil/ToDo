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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wangyazhou.todo.adapter.TodoListAdapter;
import com.wangyazhou.todo.dataAccessor.TodoItem;
import com.wangyazhou.todo.dataAccessor.TodoItemAccessor;
import com.wangyazhou.todo.util.DatetimeUtil;
import com.wangyazhou.todo.util.ImageUtil;
import com.wangyazhou.todo.view.SlideMenu;
import com.wangyazhou.todo.view.TopBar;

import java.util.Date;
import java.util.Map;

/**
 * The main activity to display the to do list
 */
public class TodoListActivity extends Activity {
    private ListView todoList = null;
    private TopBar topbar = null;
    private SlideMenu slideMenu = null;

    private TodoListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        initializeWidgets();
    }

    protected void initializeWidgets(){
        todoList = (ListView) this.findViewById(R.id.todo_list_list);
        listAdapter = new TodoListAdapter(this);
        todoList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        slideMenu = (SlideMenu) this.findViewById(R.id.todo_list_slide_menu);
        slideMenu.setMenuOnItemClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // It's ugly code here. Need refactor if there will be more menu items.
                boolean isSelected = view.getTag() != null ? (boolean) view.getTag() : false;
                if(!isSelected) {
                    if (position == 0) {
                        parent.getChildAt(0).setTag(true);
                        parent.getChildAt(1).setTag(false);
                        parent.getChildAt(0).setBackgroundColor(view.getResources().getColor(android.R.color.darker_gray));
                        parent.getChildAt(1).setBackgroundColor(view.getResources().getColor(android.R.color.white));
                        listAdapter.setIsShowingArchived(false);
                    } else {
                        parent.getChildAt(0).setTag(false);
                        parent.getChildAt(1).setTag(true);
                        parent.getChildAt(0).setBackgroundColor(view.getResources().getColor(android.R.color.white));
                        parent.getChildAt(1).setBackgroundColor(view.getResources().getColor(android.R.color.darker_gray));
                        listAdapter.setIsShowingArchived(true);
                    }
                }
                slideMenu.forceSlideIn();
            }
        });

        topbar = (TopBar) this.findViewById(R.id.todo_list_top_bar);
        topbar.setTitleText(R.string.activity_title_todo_list_1);
        topbar.setButtonsDisplay(true, false, getResources().getText(R.string.activity_menu_left_1), null);
        topbar.setLeftButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the slide menu
                slideMenu.forceSlideOut();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActionHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            TodoItem item = listAdapter.getItem(listAdapter.getActiveItemPosition());
            item.setThumbnail(ImageUtil.Bitmap2Bytes(imageBitmap));
            listAdapter.updateItem(item);
        } else if (requestCode == ActionHelper.REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
        }
    }
}
