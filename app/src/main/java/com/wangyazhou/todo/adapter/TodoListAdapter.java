package com.wangyazhou.todo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.wangyazhou.todo.ActionHelper;
import com.wangyazhou.todo.ImagePickerActivity;
import com.wangyazhou.todo.R;
import com.wangyazhou.todo.dataAccessor.TodoItem;
import com.wangyazhou.todo.dataAccessor.TodoItemAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoListAdapter extends SimpleAdapter {
    protected static int itemLayoutId = R.layout.to_do_list_item;
    protected static String[] from = new String[]{};
    protected static int[] to = new int[]{};
    protected static List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

    // The item is selected for actions, such as pick image.
    private int activeItemPosition;

    public static final String MAP_KEY_ITEM = "ITEM";

    public final class ViewHolder {
        CheckBox checkbox;
        EditText text;
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        mInflater = LayoutInflater.from(context);
        if (convertView == null) {
            convertView = mInflater.inflate(itemLayoutId, null);
            holder = new ViewHolder();
            holder.text = (EditText) convertView.findViewById(R.id.todo_list_item_editText);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.todo_list_item_checkBox);
            holder.image = (ImageView) convertView.findViewById(R.id.todo_list_item_imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
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
        return convertView;
    }

    protected void renderExistingLine(ViewHolder holder, TodoItem item) {
        holder.text.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.text.setText(item.getDescription());
        holder.text.setHint(null);
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
        holder.text.setHintTextColor(context.getResources().getColor(android.R.color.darker_gray));
        holder.text.setText(null);
        holder.text.setHint(R.string.new_task_hint);
        holder.image.setImageBitmap(null);
    }

    protected void bindClickEvents(final ViewHolder holder) {
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = (int) buttonView.getTag();
                Log.d("Todo", position + " onCheckedChanged " + isChecked);
                TodoItem item = getItem(position);
                item.setIsDone(isChecked ? TodoItem.VALUE_IS_DONE : TodoItem.VALUE_NOT_DONE);
                updateItem(item);
            }
        });
        // Bind click event for elements
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                TodoItem item = getItem(position);
                if(!item.isEmpty()) {
                    activeItemPosition = position;
                    addOrUpdateImage();
                }
            }
        });
        holder.text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int position = (int) v.getTag();
                Log.d("Todo", position + " onFocusChange " + hasFocus);
                if (hasFocus) {
                    activeItemPosition = position;
                } else {
                    String newText = holder.text.getText().toString();
                    // There is a editing item and the new content is not empty
                    if (newText.length() > 0 && activeItemPosition >= 0) {
                        TodoItem item = getItem(position);
                        if (!item.isEmpty()) {
                            //update
                            item.setDescription(newText);
                            updateItem(item);
                        } else {
                            item.setDescription(newText);
                            addItem(item);
                        }
                        activeItemPosition = -1;
                    } else {
                        //Notify user that content can't be empty
                        //TODO
                    }
                }
            }
        });
    }

    public void addOrUpdateImage() {
        Intent pickImageIntent = new Intent(context, ImagePickerActivity.class);
        context.startActivityForResult(pickImageIntent, ActionHelper.REQUEST_IMAGE_PICKER);
    }

    protected boolean isShowingArchived = false;

    public void setIsShowingArchived(boolean is){
        isShowingArchived = is;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        TodoItem[] items = todoItemAccessor.listTodoItem(-1, Integer.MAX_VALUE, isShowingArchived);
        data.clear();
        boolean isInputLineAdded = false;
        for (TodoItem item : items) {
            if (item.getIsDone() == TodoItem.VALUE_IS_DONE) {
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
        super.notifyDataSetChanged();
    }

    protected void insertInputItem() {
        if(!isShowingArchived) {
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
        todoItemAccessor.updateTodoItem(item);
        notifyDataSetChanged();
    }

    public void addItem(TodoItem item) {
        todoItemAccessor.insertTodoItem(item);
        notifyDataSetChanged();
    }
}
