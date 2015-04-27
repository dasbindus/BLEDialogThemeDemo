package com.example.bledialogthemedemo;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyGridAdapter extends BaseAdapter {

	private static final String TAG = "MyGridAdapter";
	private Context mContext;
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mContentList;

	public MyGridAdapter(Context context, List<Map<String, Object>> list) {
		mContext = context;
		mContentList = list;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		Log.i(TAG, "getCount");
		return mContentList.size();
	}

	@Override
	public Object getItem(int position) {
		Log.i(TAG, "getItem");
		return mContentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		Log.i(TAG, "getItemId");
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "getView");
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.cartest_content_item, null);
			holder = new ViewHolder();
			holder.itemNameTx = (TextView) convertView
					.findViewById(R.id.itemNameTx);
			holder.itemValueTx = (TextView) convertView
					.findViewById(R.id.itemValueTx);
			holder.uintTx = (TextView) convertView.findViewById(R.id.unitTx);
			holder.itemCheckedPic = (ImageView) convertView.findViewById(R.id.itemCheckedPic); 
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.itemNameTx.setText("" + mContentList.get(position).get("name"));
		holder.itemValueTx.setText("" + mContentList.get(position).get("value"));
		holder.uintTx.setText("" + mContentList.get(position).get("unit"));
		
//		holder.itemCheckedPic.setImageDrawable((Drawable) mContentList.get(position).get("selectPic"));

		return convertView;
	}

	public class ViewHolder {
		public TextView itemNameTx;
		public TextView itemValueTx;
		public TextView uintTx;
		public ImageView itemCheckedPic;
	}

}
