package org.hjf.view.listview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * 便捷ListView的使用
 * 多布局的支持
 * @author huangjf
 */
public abstract class AbsListMultiAdapter<T> extends AbsListAdapter<T> {

	public AbsListMultiAdapter(Context mContext) {
		super(mContext);
	}

	@SuppressLint("ViewTag") @Override
	public View getView(int position, View convertView, ViewGroup parent) {

		T data = null;
		if (position < this.mDataList.size()) {// getCount() 可能被覆写
			data = this.mDataList.get(position);
		}
		int itemLayoutResId = getItemLayoutResId(position, data);

		if (convertView != null && (Integer) convertView.getTag() != itemLayoutResId) {
			convertView.setTag(((Integer) convertView.getTag()), null);
			convertView = null;
		}
		ViewHolder holder = null;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag(itemLayoutResId);
		}
		if (holder == null) {
			convertView = LayoutInflater.from(mContextInAdapter).inflate(itemLayoutResId, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(itemLayoutResId);
			convertView.setTag(itemLayoutResId, holder);
		}

		onBindData(position, data, holder);
		return convertView;
	}

	@Override
	@Deprecated
	public int getItemLayoutResId() {
		return 0;
	}

	public abstract int getItemLayoutResId(int position, T data);
}
