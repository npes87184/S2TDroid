package com.npes87184.s2tdroid.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.npes87184.s2tdroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by npes87184 on 2015/5/17.
 */
public class ListAdapter extends BaseAdapter {

    private LayoutInflater myInflater;
    private List<String> list;

    public ListAdapter(Context context,List<String> list){
        myInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return list.indexOf(getItem(position));
    }

    private class ViewHolder {
        TextView value;
        ImageView icon;
        public ViewHolder(TextView value, ImageView icon) {
            this.value = value;
            this.icon = icon;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.textView),
                    (ImageView) convertView.findViewById(R.id.imageView)
            );
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.value.setText(list.get(position));
        switch(position) {
            case 0:
                holder.icon.setImageResource(R.drawable.ic_menu_star);
                break;
            case 1:

                break;
            case 2:
                holder.icon.setImageResource(R.drawable.perm_group_system_tools);
                break;
            case 3:
                holder.icon.setImageResource(R.drawable.ic_menu_info_details);
                break;
        }


        return convertView;
    }

}