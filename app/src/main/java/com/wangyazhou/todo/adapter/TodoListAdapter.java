package com.wangyazhou.todo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.wangyazhou.todo.ActionHelper;
import com.wangyazhou.todo.ImagePickerActivity;
import com.wangyazhou.todo.R;
import com.wangyazhou.todo.ViewFullImageActivity;
import com.wangyazhou.todo.dataAccessor.TodoItem;
import com.wangyazhou.todo.dataAccessor.TodoItemAccessor;
import com.wangyazhou.todo.util.BackgroundUITask;
import com.wangyazhou.todo.util.BackgroundUITask.Task;
import com.wangyazhou.todo.util.BackgroundUITask.TaskCallback;
import com.wangyazhou.todo.util.DialogUtil;
import com.wangyazhou.todo.view.UnderlineEditText;

@SuppressLint({ "ClickableViewAccessibility" })
public class TodoListAdapter extends SimpleAdapter {
    protected static int itemLayoutId = R.layout.to_do_list_item;
    protected static String[] from = new String[] {};
    protected static int[] to = new int[] {};
    protected static List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    // The item is selected for actions, such as pick image.
    private int activeItemPosition = -1;

    public static final String MAP_KEY_ITEM = "ITEM";

    public final class ViewHolder {
	CheckBox checkbox;
	UnderlineEditText text;
	ImageView image;
    }

    private Activity context;
    private LayoutInflater mInflater;
    private TodoItemAccessor todoItemAccessor;

    public TodoListAdapter(Context context) {
	super(context, data, itemLayoutId, from, to);
	this.todoItemAccessor = new TodoItemAccessor(context);
	this.context = (Activity) context;
    }

    private int activatedEditTextPostion = -1;
    // For stop the change listener while setting widget value
    private boolean isRenderingView = false;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
	ViewHolder holder;
	mInflater = LayoutInflater.from(context);
	if (convertView == null) {
	    convertView = mInflater.inflate(itemLayoutId, null);
	    holder = new ViewHolder();
	    holder.text = (UnderlineEditText) convertView
		    .findViewById(R.id.todo_list_item_editText);
	    holder.checkbox = (CheckBox) convertView
		    .findViewById(R.id.todo_list_item_checkBox);
	    holder.image = (ImageView) convertView
		    .findViewById(R.id.todo_list_item_imageView);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolder) convertView.getTag();
	}
	isRenderingView = true;
	holder.text.setTag(position);
	holder.checkbox.setTag(position);
	holder.image.setTag(position);
	// Set data to display
	TodoItem item = getItem(position);
	if (!item.isEmpty()) {
	    renderExistingLine(holder, item);
	} else {
	    renderInputLine(holder);
	}
	bindClickEvents(holder);
	Log.d("Todo", "Repaiting");
	setupTextBox(position, holder);
	isRenderingView = false;
	return convertView;
    }

    private void setupTextBox(final int position, ViewHolder holder) {
	// Fix the focus problem of EditText-inside-ListView
	holder.text.setOnTouchListener(new OnTouchListener() {
	    @Override
	    public boolean onTouch(View arg0, MotionEvent arg1) {
		if (isRenderingView)
		    return false;
		if (arg1.getAction() == MotionEvent.ACTION_UP) {
		    Log.d("Todo", "set focus tag for text: " + position);
		    activatedEditTextPostion = position;
		}
		return false;
	    }
	});
	holder.text.setOnFocusChangeListener(new OnFocusChangeListener() {

	    @Override
	    public void onFocusChange(View v, boolean hasFocus) {
		int position = (int) v.getTag();
		Log.d("Todo", "onFocusChange " + hasFocus + " " + position
			+ " " + activeItemPosition);
		if (!hasFocus && activeItemPosition == position) {
		    Log.d("Todo", "onFocusChange " + position);
		    EditText et = (EditText) v;
		    saveCurrentChange(et.getText().toString());
		} else if (hasFocus) {
		    activeItemPosition = position;
		}
	    }
	});
	if (activatedEditTextPostion != -1
		&& activatedEditTextPostion == position) {
	    Log.d("Todo", "request focus for text " + activatedEditTextPostion);
	    holder.text.setHasUnderLine(true);
	    holder.text.requestFocus();
	    // if (holder.text.selection >= 0) {
	    // holder.text.setSelection(holder.text.selection);
	    // holder.text.selection = -1;
	    // }
	    holder.text.setSelection(holder.text.getText().length());
	    holder.text.setCursorVisible(true);
	} else {
	    holder.text.setHasUnderLine(false);
	}
    }

    /**
     * Should be called when the current text should be saved. Currently it's
     * triggered by touch event of transparent layer
     */
    public void saveCurrentChange(String newText) {
	if (activeItemPosition != -1 && activeItemPosition < getCount()) {
	    TodoItem item = getItem(activeItemPosition);
	    // no content for new todo
	    if (item.isEmpty() && newText.length() <= 0) {
		return;
	    }
	    // no change for existing todo
	    if (!item.isEmpty() && item.getDescription().equals(newText))
		return;
	    // There is a editing item and the new content is not empty
	    if (newText != null && newText.length() > 0) {
		if (!item.isEmpty()) {
		    // update
		    item.setDescription(newText);
		    updateItem(item);
		} else {
		    item.setDescription(newText);
		    addItem(item);
		}
		Log.d("Todo",
			activeItemPosition + " text saved " + item.toString());
	    } else {
		// Notify user that content can't be empty
		DialogUtil.showSimpleToastText(context,
			"Content can't be empty");
	    }
	    // Always clear the input flags when input box loss focus
	    clearTextEditFlags();
	}
    }

    private void clearTextEditFlags() {
	// Clear tags
	activeItemPosition = -1;
	activatedEditTextPostion = -1;// remove the focus tag on text
				      // edit
	View view = context.getWindow().peekDecorView();
	if (view != null) {
	    ((InputMethodManager) context
		    .getSystemService(Context.INPUT_METHOD_SERVICE))
		    .hideSoftInputFromWindow(view.getWindowToken(),
			    InputMethodManager.HIDE_NOT_ALWAYS);
	}
    }

    protected void renderExistingLine(ViewHolder holder, TodoItem item) {
	holder.text.setText(item.getDescription());
	holder.text.setHint(null);
	if (!item.isEmpty() && item.getIsDone() == TodoItem.VALUE_IS_DONE) {
	    holder.text.setTextColor(context.getResources().getColor(
		    android.R.color.darker_gray));
	    holder.text.getPaint().setFlags(
		    Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
	} else {
	    holder.text.setTextColor(context.getResources().getColor(
		    android.R.color.black));
	    holder.text.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
	}
	holder.checkbox.setChecked(item.getIsDone() == TodoItem.VALUE_IS_DONE);
	holder.checkbox.setEnabled(true);
	if (item.getThumbnailBitmap() != null) {
	    holder.image.setImageBitmap(item.getThumbnailBitmap());
	} else {
	    holder.image.setImageBitmap(null);
	}
    }

    protected void renderInputLine(ViewHolder holder) {
	holder.checkbox.setChecked(false);
	holder.checkbox.setEnabled(false);
	holder.text.setTextColor(context.getResources().getColor(
		android.R.color.black));
	holder.text.setHintTextColor(context.getResources().getColor(
		android.R.color.darker_gray));
	holder.text.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
	holder.text.setText(null);
	holder.text.setHint(R.string.new_task_hint);
	holder.image.setImageBitmap(null);
    }

    protected void bindClickEvents(final ViewHolder holder) {
	holder.checkbox
		.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView,
			    boolean isChecked) {
			if (isRenderingView)
			    return;
			int position = (int) buttonView.getTag();
			Log.d("Todo", position + " onCheckedChanged "
				+ isChecked);
			TodoItem item = getItem(position);
			item.setIsDone(isChecked ? TodoItem.VALUE_IS_DONE
				: TodoItem.VALUE_NOT_DONE);
			updateItem(item);
		    }
		});
	// Bind click event for elements
	holder.image.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		int position = (int) v.getTag();
		TodoItem item = getItem(position);
		if (!item.isEmpty()) {
		    activeItemPosition = position;
		    if (item.getThumbnail() == null) {
			addOrUpdateImage();
		    } else {
			viewFullImage(item.getThumbnail(), item.getFullImage());
		    }
		}
	    }
	});
    }

    public void addOrUpdateImage() {
	Intent pickImageIntent = new Intent(context, ImagePickerActivity.class);
	context.startActivityForResult(pickImageIntent,
		ActionHelper.REQUEST_IMAGE_PICKER);
    }

    public void viewFullImage(byte[] thumbnail, String fullImagePath) {
	Intent viewImageIntent = new Intent(context,
		ViewFullImageActivity.class);
	viewImageIntent.putExtra(ViewFullImageActivity.INTENT_KEY_THUMBNAIL,
		thumbnail);
	viewImageIntent.putExtra(ViewFullImageActivity.INTENT_KEY_FULL_IMAGE,
		fullImagePath);
	context.startActivityForResult(viewImageIntent,
		ActionHelper.REQUEST_VIEW_IMAGE);
    }

    protected boolean isShowingArchived = false;

    public void setIsShowingArchived(boolean is) {
	isShowingArchived = is;
	clearTextEditFlags();
	notifyDataSetChanged();
    }

    private BackgroundUITask uiTask;

    public void realNotifyDataSetChanged() {
	super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
	uiTask = new BackgroundUITask(context, true);
	uiTask.setOkCallback(new TaskCallback() {
	    @Override
	    public void perform() {
		realNotifyDataSetChanged();
	    }
	});
	uiTask.start(new Task() {
	    @Override
	    public boolean perform() {
		TodoItem[] items = todoItemAccessor.listTodoItem(-1,
			Integer.MAX_VALUE, isShowingArchived);
		data.clear();
		clearTextEditFlags();
		boolean isInputLineAdded = false;
		for (TodoItem item : items) {
		    if (!isInputLineAdded
			    && item.getIsDone() == TodoItem.VALUE_IS_DONE) {
			// Add an empty item to represent the input line.
			insertInputItem();
			isInputLineAdded = true;
		    }
		    Map<String, Object> map = new HashMap<>();
		    map.put(MAP_KEY_ITEM, item);
		    data.add(map);
		}
		if (!isInputLineAdded) {
		    // Add an empty item to empty list.
		    insertInputItem();
		}
		return true;
	    }
	});
    }

    protected void insertInputItem() {
	if (!isShowingArchived) {
	    TodoItem inputItem = new TodoItem();
	    Map<String, Object> map = new HashMap<>();
	    map.put(MAP_KEY_ITEM, inputItem);
	    data.add(map);
	}
    }

    public TodoItem getItem(int position) {
	return (TodoItem) data.get(position).get(MAP_KEY_ITEM);
    }

    public int getActiveItemPosition() {
	return activeItemPosition;
    }

    public void updateItem(TodoItem item) {
	Log.d("Todo", "update item " + item);
	todoItemAccessor.updateTodoItem(item);
	notifyDataSetChanged();
    }

    public void deleteItem(TodoItem item) {
	Log.d("Todo", "delete item " + item);
	todoItemAccessor.deleteTodoItem(item);
	notifyDataSetChanged();
    }

    public void addItem(TodoItem item) {
	Log.d("Todo", "add item " + item);
	todoItemAccessor.insertTodoItem(item);
	notifyDataSetChanged();
    }
}
