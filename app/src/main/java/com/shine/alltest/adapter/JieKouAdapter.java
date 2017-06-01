package com.shine.alltest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.shine.alltest.R;
import com.shine.alltest.bean.JieKouBean;

import java.util.ArrayList;

/**
 * Created by 123 on 2017/5/19.
 */

public class JieKouAdapter extends BaseAdapter {
    ArrayList<JieKouBean> list = new ArrayList<>();
    Context context;

    public JieKouAdapter(ArrayList<JieKouBean> list, Context context) {
        this.list = list;
        this.context = context;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_one_textview, null);
        return view;
    }
}
