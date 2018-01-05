package org.hjf.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.hjf.util.ParamUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 便捷ListView的使用
 * @author huangjf
 */
public abstract class AbsListAdapter<T> extends BaseAdapter {

	protected Context mContextInAdapter;
	List<T> mDataList; // 默认权限是同一个包的能访问

	public AbsListAdapter(Context mContext) {
		super();
		this.mContextInAdapter = mContext;
		this.mDataList = new ArrayList<T>();
	}

	public void setDataList(@NonNull List<T> tempDataList) {
		this.mDataList.clear();
		this.addDataList(tempDataList);
	}

	public void addDataList(@NonNull List<T> tempDataList) {
		this.mDataList.addAll(tempDataList);
		this.notifyDataSetChanged();
	}

	public void addData(@NonNull T data) {
		this.mDataList.add(data);
		this.notifyDataSetChanged();
	}

	public void addData(int index, @NonNull T data) {
		this.mDataList.add(index, data);
		this.notifyDataSetChanged();
	}

	public void removeData(int index){
		if (index != ParamUtils.getValueInRange(index, 0, this.mDataList.size())) {
			return ;
		}
		this.mDataList.remove(index);
		this.notifyDataSetChanged();
	}

	public void removeData(T t){
		if (t == null) {
			return;
		}
		int index = this.mDataList.indexOf(t);
		removeData(index);
	}

	public void clearData() {
		this.mDataList.clear();
		this.notifyDataSetChanged();
	}

	public List<T> getDataList() {
		return new ArrayList<T>(this.mDataList);
	}

	@Nullable
	public T getData(int index) {
		if (index != ParamUtils.getValueInRange(index, 0, this.mDataList.size())) {
			return null;
		}
		return this.mDataList.get(index);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContextInAdapter).inflate(getItemLayoutResId(), parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		T data = null;
		// getCount() 可能被覆写
		if (position < this.mDataList.size()) {
			data = this.mDataList.get(position);
		}

		onBindData(position, data, holder);
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	public abstract int getItemLayoutResId();

	public abstract void onBindData(int position, T data, ViewHolder holder);

	public static class ViewHolder {
		private SparseArray<View> viewCache;
		private View itemView;

		public ViewHolder(View containerView) {
			this.itemView = containerView;
			this.viewCache = new SparseArray<>();
		}

		@SuppressWarnings("unchecked")
		public <T extends View> T getView(int resId) {
			View view = this.viewCache.get(resId);
			if (view == null) {
				view = this.itemView.findViewById(resId);
				this.viewCache.put(resId, view);
			}
			return (T) view;
		}

		public View getItemView() {
			return this.itemView;
		}

		public void setText(int resId, CharSequence text) {
			TextView textView = this.getView(resId);
			textView.setText(text);
		}
	}
}
