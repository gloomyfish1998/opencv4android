package com.book.datamodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import gloomyfish.opencvdemo.R;

/**
 * Created by gloomy fish on 2016/11/12.
 */

public class SectionsListViewAdaptor extends BaseAdapter {
    private Context appContext;
    private List<ItemDto> dataModel;

    public SectionsListViewAdaptor(Context appContext) {
        this.appContext = appContext;
        dataModel = new ArrayList<>();
    }

    public List<ItemDto> getDataModel() {
        return this.dataModel;
    }

    @Override
    public int getCount() {
        return dataModel.size();
    }

    @Override
    public Object getItem(int position) {
        return dataModel.get(position);
    }

    @Override
    public long getItemId(int position) {
        return dataModel.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) appContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);
        TextView textView = (TextView)rowView.findViewById(R.id.row_textView);
        // populate the text into UI
        textView.setText(dataModel.get(position).getDesc());
        return rowView;
    }
}
