package com.example.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.mediaactivity.R;
import com.example.msg.audioMsg;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListAdapter extends BaseAdapter {

	private List<audioMsg> list;
	private Context context;

	public MyListAdapter(FragmentActivity activity, ArrayList<audioMsg> arrayList) {
		list = arrayList;
		context = activity;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AppItem appItem = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.audio_adapter, null);
			appItem = new AppItem();
			appItem.tView = (TextView) convertView.findViewById(R.id.textView);
			convertView.setTag(appItem);
		} else {
			appItem = (AppItem) convertView.getTag();
		}
		appItem.tView.setText(list.get(position).getName());
		return convertView;
	}

	public class AppItem {
		public TextView tView;
		public ImageView iView;
	}
}
