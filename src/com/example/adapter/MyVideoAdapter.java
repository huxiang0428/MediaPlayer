package com.example.adapter;

import java.util.ArrayList;
import java.util.List;

import com.example.adapter.MyListAdapter.AppItem;
import com.example.mediaactivity.R;
import com.example.msg.videoMsg;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class MyVideoAdapter extends BaseAdapter {

	private Context context;
	private List<videoMsg> list;
	public MyVideoAdapter(FragmentActivity activity, ArrayList<videoMsg> arrayList) {
		context=activity;
		list=arrayList;
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
					R.layout.video_adapter, null);
			appItem = new AppItem();
			appItem.tView = (TextView) convertView.findViewById(R.id.textView1);
			convertView.setTag(appItem);
		} else {
			appItem = (AppItem) convertView.getTag();
		}
		appItem.tView.setText(list.get(position).getName());
		return convertView;
	}

	public class AppItem {
		public TextView tView;
	}
}