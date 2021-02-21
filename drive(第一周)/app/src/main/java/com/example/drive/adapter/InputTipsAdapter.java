package com.example.drive.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.help.Tip;
import com.example.drive.R;

import java.util.List;

public class InputTipsAdapter extends BaseAdapter {
    private Context mContext;
    private List<Tip> mListTips;
    public InputTipsAdapter(Context context, List<Tip> tipList) {
        mContext = context;
        mListTips = tipList;
    }
    class Holder {
        TextView mName;
        TextView mAddress;
    }
    @Override
    public int getCount() {
        if (mListTips != null) {
            return mListTips.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mListTips != null) {
            return mListTips.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_inputtips, null);
            holder.mName = (TextView) convertView.findViewById(R.id.name);
            holder.mAddress = (TextView) convertView.findViewById(R.id.adress);
            convertView.setTag(holder);
        } else{
            holder = (Holder)convertView.getTag();
        }
        if(mListTips == null){
            return convertView;
        }

        holder.mName.setText(mListTips.get(position).getName());
        String address = mListTips.get(position).getAddress();
        if(address == null || address.equals("")){
            holder.mAddress.setVisibility(View.GONE);
        }else{
            holder.mAddress.setVisibility(View.VISIBLE);
            holder.mAddress.setText(address);
        }

        return convertView;
    }
}
