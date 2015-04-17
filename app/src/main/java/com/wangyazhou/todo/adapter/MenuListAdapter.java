package com.wangyazhou.todo.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wangyazhou.todo.R;
import com.wangyazhou.todo.dataAccessor.TodoItemAccessor;
import com.wangyazhou.todo.util.BackgroundUITask;

public class MenuListAdapter extends SimpleAdapter {
    protected boolean[] isSelected;

    protected int[] numbers;
    protected TodoItemAccessor dataAccessor;
    protected Context context;

    public MenuListAdapter(Context context,
                           List<? extends Map<String, ?>> data, int resource, String[] from,
                           int[] to, boolean[] isSelected) {
        super(context, data, resource, from, to);
        this.isSelected = isSelected;
        this.context = context;
        this.numbers = new int[]{0, 0};
        dataAccessor = new TodoItemAccessor(context);

    }

    public boolean isMenuSelected(int position) {
        return isSelected[position];
    }

    public void selectMenu(int position) {
        for (int i = 0; i < isSelected.length; i++) {
            isSelected[i] = false;
        }
        isSelected[position] = true;
        notifyDataSetChanged();
    }

    public void reloadNumbers() {
        BackgroundUITask task = new BackgroundUITask(context, false);
        task.setOkCallback(new BackgroundUITask.TaskCallback(){
            @Override
            public void perform(){
                notifyDataSetChanged();
            }
        });
        task.start(new BackgroundUITask.Task() {
            @Override
            public boolean perform() {
                //Only load undo count
                numbers[0] = dataAccessor.countUndoItem();
                return true;
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = super.getView(position, convertView, parent);
        int colorRes = isSelected[position] ? android.R.color.darker_gray
                : android.R.color.white;
        view.setBackgroundColor(view.getResources().getColor(colorRes));
        String numberText = numbers[position] > 0 ? "" + numbers[position] : "";
        ((TextView) view.findViewById(R.id.slide_menu_item_number))
                .setText(numberText);
        return view;
    }

}
