package com.wangyazhou.todo.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.wangyazhou.todo.ActionHelper;
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

    public final class ViewHolder{
        CheckBox checkbox;
        EditText text;
        ImageView image;
    }
    private Activity context;
    private LayoutInflater mInflater;
    private TodoItemAccessor todoItemAccessor;

    public TodoListAdapter(Context context, TodoItemAccessor todoItemAccessor) {
        super(context, data, itemLayoutId, from, to);
        this.todoItemAccessor = todoItemAccessor;
        this.context = (Activity)context;
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
        // Set data to display
        TodoItem item = (TodoItem) data.get(position).get(MAP_KEY_ITEM);
        holder.text.setText(item.getDescription());
        holder.checkbox.setChecked(item.getIsDone() == TodoItem.VALUE_IS_DONE);
        if(item.getThumbnailBitmap() != null){
            holder.image.setImageBitmap(item.getThumbnailBitmap());
        } else {
            holder.image.setImageBitmap(null);
        }
        // Bind click event for elements
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeItemPosition = position;
                addOrUpdateImage();
            }
        });
        return convertView;
    }

    public void addOrUpdateImage(){
        ActionHelper.showPickImageDialog(context);
    }

    @Override
    public void notifyDataSetChanged() {
        TodoItem[] items = todoItemAccessor.listTodoItem(-1, Integer.MAX_VALUE, false);
        data.clear();
        for(TodoItem item : items){
            Map<String, Object> map = new HashMap<>();
            map.put(MAP_KEY_ITEM, item);
            data.add(map);
        }
        super.notifyDataSetChanged();
    }

    public TodoItem getItem(int position){
        return (TodoItem) data.get(position).get(MAP_KEY_ITEM);
    }

    public int getActiveItemPosition(){
        return activeItemPosition;
    }

    public void updateItem (int position, TodoItem item){
        todoItemAccessor.updateTodoItem(item);
        notifyDataSetChanged();
    }
}
