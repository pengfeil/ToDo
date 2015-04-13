package com.wangyazhou.todo.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wangyazhou.todo.R;

public class MenuListAdapter extends SimpleAdapter {
    protected boolean[] isSelected;

    protected int[] numbers;

    public MenuListAdapter(Context context,
	    List<? extends Map<String, ?>> data, int resource, String[] from,
	    int[] to, boolean[] isSelected) {
	super(context, data, resource, from, to);
	this.isSelected = isSelected;
	this.numbers = new int[] { 1, 0 };
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
	// TODO load numbers from DB;
	notifyDataSetChanged();
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
