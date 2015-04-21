package com.wangyazhou.todo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wangyazhou.todo.ActionHelper;
import com.wangyazhou.todo.ImagePickerActivity;
import com.wangyazhou.todo.R;
import com.wangyazhou.todo.ViewFullImageActivity;
import com.wangyazhou.todo.dataAccessor.ExternalFileIOHelper;
import com.wangyazhou.todo.dataAccessor.TodoItem;
import com.wangyazhou.todo.dataAccessor.TodoItemAccessor;
import com.wangyazhou.todo.util.BackgroundUITask;
import com.wangyazhou.todo.util.BackgroundUITask.Task;
import com.wangyazhou.todo.util.BackgroundUITask.TaskCallback;
import com.wangyazhou.todo.util.DialogUtil;
import com.wangyazhou.todo.view.UnderlineEditText;

public class TodoListAdapter extends SimpleAdapter {
    protected static int itemLayoutId = R.layout.to_do_list_item;
    protected static String[] from = new String[] {};
    protected static int[] to = new int[] {};
    protected static List<Map<String, Object>> data = new ArrayList<>();
    protected ListView listView;

    // The item is selected for actions, such as pick image£¬ input text.
    private int activeItemPosition = -1;

    public static final String MAP_KEY_ITEM = "ITEM";

    public final class ViewHolder {
	ImageView checkbox;
	TextView text;
	UnderlineEditText editText;
	ImageView image;
    }

    private Activity context;
    private TodoItemAccessor todoItemAccessor;

    public TodoListAdapter(Context context, ListView listView) {
	super(context, data, itemLayoutId, from, to);
	this.todoItemAccessor = new TodoItemAccessor(context);
	this.context = (Activity) context;
	this.listView = listView;
    }

    // For stop the change listener while setting widget value
    private boolean isRenderingView = false;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
	ViewHolder holder;
	LayoutInflater mInflater = LayoutInflater.from(context);
	if (convertView == null) {
	    convertView = mInflater.inflate(itemLayoutId, null);
	    holder = buildViewHolder(convertView);
	    convertView.setTag(holder);
	} else {
	    holder = (ViewHolder) convertView.getTag();
	}
	isRenderingView = true;
	holder.text.setTag(position);
	holder.editText.setTag(position);
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
	setupTextBox(position, holder);
	isRenderingView = false;
	return convertView;
    }

    protected ViewHolder buildViewHolder(View convertView) {
	ViewHolder holder;
	holder = new ViewHolder();
	holder.text = (TextView) convertView
		.findViewById(R.id.todo_list_item_text);
	holder.editText = (UnderlineEditText) convertView
		.findViewById(R.id.todo_list_item_editText);
	holder.checkbox = (ImageView) convertView
		.findViewById(R.id.todo_list_item_checkBox);
	holder.image = (ImageView) convertView
		.findViewById(R.id.todo_list_item_imageView);
	return holder;
    }

    private void setupTextBox(final int position, final ViewHolder holder) {
	holder.text.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Log.d("Todo", "Click on TextView" + position);
		if (activeItemPosition >= 0 && activeItemPosition != position) {
		    Log.d("Todo", "clear edit state from " + activeItemPosition);
		    saveCurrentText();
		} else if (!isRenderingView) {
		    Log.d("Todo", "add edit state to " + position);
		    activeItemPosition = position;
		    ViewHolder activeHolder = getViewHolderByPosition(activeItemPosition);
		    activeHolder.editText.setVisibility(View.VISIBLE);
		    activeHolder.text.setVisibility(View.INVISIBLE);
		    delayToRequestFocus();
		}
	    }
	});
	holder.text.setOnLongClickListener(new View.OnLongClickListener() {
	    @Override
	    public boolean onLongClick(View v) {
		DialogUtil.confirmDialog(context, "Notice",
			"Delete this item?", new DialogUtil.ActionCallback() {
			    @Override
			    public void perform() {
				TodoItem todo = getItem(position);
				deleteItem(todo);
				ExternalFileIOHelper.deleteImage(context,
					todo.getFullImage());
			    }
			});
		return true;
	    }
	});

	if (activeItemPosition == position) {
	    Log.d("Todo", "request focus for editText " + activeItemPosition);
	    holder.editText.setVisibility(View.VISIBLE);
	    holder.text.setVisibility(View.INVISIBLE);
	    delayToRequestFocus();
	} else {
	    holder.editText.clearFocus();
	    holder.text.setVisibility(View.VISIBLE);
	    holder.editText.setVisibility(View.INVISIBLE);
	}
    }

    protected void delayToRequestFocus() {
	Handler handler = new Handler();
	handler.postDelayed(new Runnable() {
	    @Override
	    public void run() {
		if (activeItemPosition >= 0 && activeItemPosition < getCount()) {
		    InputMethodManager imm = (InputMethodManager) context
			    .getSystemService(Context.INPUT_METHOD_SERVICE);
		    EditText et = getViewHolderByPosition(activeItemPosition).editText;
		    et.requestFocus();
		    et.setSelection(et.getText().length());
		    imm.showSoftInput(et, InputMethodManager.SHOW_FORCED);
		    Log.d("Todo", "Delayed " + activeItemPosition);
		}
	    }
	}, 200);
    }

    /**
     * Should be called when the current text should be saved
     */
    public void saveCurrentText() {
	if (activeItemPosition >= 0 && activeItemPosition < getCount()) {
	    ViewHolder activeHolder = getViewHolderByPosition(activeItemPosition);
	    String newText = activeHolder.editText.getText().toString();
	    TodoItem item = getItem(activeItemPosition);
	    // no change for existing text
	    if (!newText.equals(item.getDescription()) && newText.length() > 0) {
		if (!item.isEmpty()) {
		    item.setDescription(newText);
		    updateItem(item);
		} else {
		    item.setDescription(newText);
		    addItem(item);
		}
		Log.d("Todo",
			activeItemPosition + " text saved " + item.toString());
		DialogUtil.showSimpleToastText(context, "Todo content saved");
	    } else if (newText.length() <= 0) {
		// Notify user that content can't be empty
		DialogUtil.showSimpleToastText(context,
			"Content can't be empty");
	    }
	    // Always clear the input flags when input box loss focus
	    clearEditFlags();
	}
    }

    public void clearEditFlags() {
	// Clear tags
	if (activeItemPosition >= 0) {
	    Log.d("Todo", "clear edit state of " + activeItemPosition);
	    ViewHolder activeHolder = getViewHolderByPosition(activeItemPosition);
	    activeHolder.editText.setVisibility(View.INVISIBLE);
	    activeHolder.text.setVisibility(View.VISIBLE);
	    activeItemPosition = -1;
	}
	View view = context.getWindow().peekDecorView();
	if (view != null) {
	    InputMethodManager imm = (InputMethodManager) context
		    .getSystemService(Context.INPUT_METHOD_SERVICE);
	    if (imm.isActive()) {
		imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
	    }
	}
    }

    protected void renderExistingLine(ViewHolder holder, TodoItem item) {
	holder.text.setText(item.getDescription());
	holder.editText.setText(item.getDescription());
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
	int checkboxImgId = item.getIsDone() == TodoItem.VALUE_IS_DONE ? R.drawable.checkboxselected
		: R.drawable.checkboxunselected;
	holder.checkbox.setImageResource(checkboxImgId);
	if (item.getThumbnailBitmap() != null) {
	    holder.image.setImageBitmap(item.getThumbnailBitmap());
	} else {
	    holder.image.setImageResource(R.drawable.defaultimage);
	}
    }

    protected void renderInputLine(ViewHolder holder) {
	holder.checkbox.setImageResource(R.drawable.checkboxunselected);
	holder.text.setTextColor(context.getResources().getColor(
		android.R.color.darker_gray));
	holder.text.setText(R.string.new_task_hint);
	holder.text.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
	holder.text.setText(context.getResources().getString(
		R.string.input_text_hint));
	holder.editText.setText(null);
	holder.image.setImageBitmap(null);
    }

    protected void bindClickEvents(final ViewHolder holder) {
	holder.checkbox.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View buttonView) {
		if (isRenderingView)
		    return;
		if (activeItemPosition >= 0) {
		    clearEditFlags();
		    return;
		}
		int position = (int) buttonView.getTag();
		TodoItem item = getItem(position);
		if (!item.isEmpty()) {
		    item.setIsDone(item.getIsDone() == TodoItem.VALUE_IS_DONE ? TodoItem.VALUE_NOT_DONE
			    : TodoItem.VALUE_IS_DONE);
		    updateItem(item);
		}
	    }
	});
	// Bind click event for elements
	holder.image.setOnClickListener(new View.OnClickListener() {
	    @Override
	    public void onClick(View v) {
		if (activeItemPosition >= 0) {
		    clearEditFlags();
		    return;
		}
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
	clearEditFlags();
	notifyDataSetChanged();
    }

    public void realNotifyDataSetChanged() {
	super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
	BackgroundUITask uiTask = new BackgroundUITask(context, true);
	uiTask.setOkCallback(new TaskCallback() {
	    @Override
	    public void perform() {
		clearEditFlags();
		realNotifyDataSetChanged();
	    }
	});
	uiTask.start(new Task() {
	    @Override
	    public boolean perform() {
		TodoItem[] items = todoItemAccessor.listTodoItem(-1,
			Integer.MAX_VALUE, isShowingArchived);
		data.clear();
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

    protected ViewHolder getViewHolderByPosition(int pos) {
	final int firstListItemPosition = listView.getFirstVisiblePosition();
	final int lastListItemPosition = firstListItemPosition
		+ listView.getChildCount() - 1;

	if (pos < firstListItemPosition || pos > lastListItemPosition) {
	    return buildViewHolder(listView.getAdapter().getView(pos, null,
		    listView));
	} else {
	    final int childIndex = pos - firstListItemPosition;
	    return buildViewHolder(listView.getChildAt(childIndex));
	}
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
