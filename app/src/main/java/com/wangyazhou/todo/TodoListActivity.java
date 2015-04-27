package com.wangyazhou.todo;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.wangyazhou.todo.adapter.TodoListAdapter;
import com.wangyazhou.todo.dataAccessor.ExternalFileIOHelper;
import com.wangyazhou.todo.dataAccessor.NativeImageLoader;
import com.wangyazhou.todo.dataAccessor.TodoItem;
import com.wangyazhou.todo.util.BackgroundUITask;
import com.wangyazhou.todo.util.BackgroundUITask.Task;
import com.wangyazhou.todo.util.BackgroundUITask.TaskCallback;
import com.wangyazhou.todo.util.DialogUtil;
import com.wangyazhou.todo.util.DialogUtil.ActionCallback;
import com.wangyazhou.todo.util.ImageUtil;
import com.wangyazhou.todo.view.SlideMenu;
import com.wangyazhou.todo.view.SlideMenu.MenuSelectedAction;
import com.wangyazhou.todo.view.TodoRelativeLayout;
import com.wangyazhou.todo.view.TopBar;

/**
 * The main activity to display the to do list
 */
public class TodoListActivity extends TodoBaseActivity {
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig);
	listAdapter.notifyDataSetChanged();
    }

    protected void initializeWidgets() {
	todoList = (ListView) this.findViewById(R.id.todo_list_list);
	listAdapter = new TodoListAdapter(this, todoList);
	todoList.setAdapter(listAdapter);
	listAdapter.notifyDataSetChanged();
	todoList.setOnItemLongClickListener(new OnItemLongClickListener() {
	    @Override
	    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
		    int arg2, long arg3) {
		final int position = (int) arg3;
		DialogUtil.confirmDialog(TodoListActivity.this, "Notice",
			"Delete this item?", new ActionCallback() {
			    @Override
			    public void perform() {
				TodoItem todo = listAdapter.getItem(position);
				listAdapter.deleteItem(todo);
				ExternalFileIOHelper.deleteImage(
					getApplicationContext(),
					todo.getFullImage());
			    }
			});
		return false;
	    }
	});
	todoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view,
		    int position, long id) {
		if (listAdapter.getActiveItemPosition() >= 0
			&& listAdapter.getActiveItemPosition() != position) {
		    listAdapter.saveCurrentText();
		    todoList.requestFocus();
		}
	    }
	});
	final View mainContent = this
		.findViewById(R.id.todo_list_main_content_layout);
	mainContent.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (listAdapter.getActiveItemPosition() >= 0) {
		    listAdapter.saveCurrentText();
		    mainContent.requestFocus();
		}
	    }
	});

	slideMenu = (SlideMenu) this.findViewById(R.id.todo_list_slide_menu);
	slideMenu.setMenuSelectedAction(new MenuSelectedAction() {

	    @Override
	    public void perform(int index) {
		if (index == 0) {
		    listAdapter.setIsShowingArchived(false);
		    topbar.setTitleText(R.string.activity_title_todo_list_1);
		} else if (index == 1) {
		    listAdapter.setIsShowingArchived(true);
		    topbar.setTitleText(R.string.activity_title_todo_list_2);
		}
	    }
	});

	topbar = (TopBar) this.findViewById(R.id.todo_list_top_bar);
	topbar.setTitleText(R.string.activity_title_todo_list_1);
	topbar.setButtonsDisplay(true, false, null, null);
	topbar.setLeftButtonBackground(R.drawable.menu);
	topbar.setLeftButtonZoneOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		listAdapter.clearEditFlags();
		// Show the slide menu
		slideMenu.forceSlideOut();
	    }
	});

	TodoRelativeLayout mainLayout = (TodoRelativeLayout) findViewById(R.id.todo_list_main_content_layout);
	mainLayout
		.setOnSizeChangedListener(new TodoRelativeLayout.OnSizeChangedListener() {

		    @Override
		    public void onSizeChanged(int w, int h, int oldw, int oldh) {
			// Detect the hide of soft input panel.
			// 200 is a magic number.... I just did the guess
			if (h - oldh > 200) {
			    listAdapter.saveCurrentText();
			}
		    }
		});
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if (uiTask != null) {
	    uiTask.stop();
	}
    }

    private BackgroundUITask uiTask;
    private TodoItem tempItem;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	Log.d("Todo", this.getClass() + "onActivityResult " + resultCode);
	if (requestCode == ActionHelper.REQUEST_IMAGE_PICKER
		&& resultCode == RESULT_OK) {
	    addOrUpdateImage(data);
	} else if (requestCode == ActionHelper.REQUEST_VIEW_IMAGE) {
	    if (resultCode == ActionHelper.RESULT_DELETE_IMAGE) {
		deleteImage();
	    } else if (resultCode == ActionHelper.RESULT_MODIFY_IMAGE) {
		addOrUpdateImage(data);
	    } else {
		listAdapter.clearEditFlags();
	    }
	} else {
	    listAdapter.clearEditFlags();
	}
    }

    private void deleteImage() {
	uiTask = new BackgroundUITask(this, true);
	uiTask.setOkCallback(new TaskCallback() {
	    @Override
	    public void perform() {
		tempItem.setThumbnail(null);
		tempItem.setFullImage(null);
		listAdapter.updateItem(tempItem);
	    }
	});
	uiTask.start(new Task() {
	    @Override
	    public boolean perform() {
		tempItem = listAdapter.getItem(listAdapter
			.getActiveItemPosition());
		return ExternalFileIOHelper.deleteImage(
			getApplicationContext(), tempItem.getFullImage());
	    }
	});
    }

    private void addOrUpdateImage(Intent data) {
	final String imagePath = data
		.getStringExtra(ImagePickerActivity.DATA_KEY);

	uiTask = new BackgroundUITask(this, true);
	uiTask.setOkCallback(new TaskCallback() {
	    @Override
	    public void perform() {
		listAdapter.updateItem(tempItem);
	    }
	});
	uiTask.start(new Task() {
	    @Override
	    public boolean perform() {
		Bitmap thumbnail = NativeImageLoader.getInstance()
			.decodeThumbBitmapForFile(imagePath,
				ImageUtil.THUMBNAIL_SIZE,
				ImageUtil.THUMBNAIL_SIZE);
		tempItem = listAdapter.getItem(listAdapter
			.getActiveItemPosition());
		tempItem.setThumbnail(ImageUtil.Bitmap2Bytes(thumbnail));
		// Save big image
		String internalImagePath = ExternalFileIOHelper.writeImage(
			TodoListActivity.this, imagePath);
		tempItem.setFullImage(internalImagePath);
		return true;
	    }
	});
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
	if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
	    listAdapter.saveCurrentText();
	}
	return super.dispatchKeyEvent(event);
    }
    
    
}
