package com.wangyazhou.todo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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

    public static final String MAP_KEY_ITEM = "ITEM";

    public final class ViewHolder{
        CheckBox checkbox;
        EditText text;
    }
    private Context context;
    private LayoutInflater mInflater;
    private TodoItemAccessor todoItemAccessor;

    public TodoListAdapter(Context context, TodoItemAccessor todoItemAccessor) {
        super(context, data, itemLayoutId, from, to);
        this.todoItemAccessor = todoItemAccessor;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        mInflater = LayoutInflater.from(context);
        if (convertView == null) {
            convertView = mInflater.inflate(itemLayoutId, null);
            holder = new ViewHolder();
            holder.text = (EditText) convertView.findViewById(R.id.todo_list_item_editText);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.todo_list_item_checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TodoItem item = (TodoItem) data.get(position).get(MAP_KEY_ITEM);
        holder.text.setText(item.getDescription());
        holder.checkbox.setChecked(item.getIsDone() == TodoItem.VALUE_IS_DONE);
        return convertView;
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
}
