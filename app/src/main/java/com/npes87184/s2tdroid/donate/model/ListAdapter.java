package com.npes87184.s2tdroid.donate.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.npes87184.s2tdroid.donate.R;

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
        @DrawableRes int iconRes;
        switch(position) {
            case 0:
                iconRes = R.drawable.ic_star_border_black_24dp;
                break;
            case 1:
                iconRes = R.drawable.ic_lightbulb_outline_black_24dp;
                break;
            case 2:
                iconRes = R.drawable.ic_settings_black_24dp;
                break;
            case 3:
            default:
                iconRes = R.drawable.ic_info_outline_black_24dp;
                break;
        }
        setIcon(holder.icon, iconRes);

        return convertView;
    }

    private void setIcon(ImageView icon, @DrawableRes int iconRes) {
        icon.setImageResource(iconRes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#ff424242")));
        }
    }
}