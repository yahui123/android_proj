package com.tang.trade.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo on 11/23/15.
 *
 */
public abstract class BaseAdapter<T, VH> extends android.widget.BaseAdapter {
    protected List<T> mList;
    protected Context mContext;

    public BaseAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<>();
    }

    public Context getContext() {
        return mContext;
    }

    public BaseAdapter setContext(Context context) {
        mContext = context;
        return this;
    }

    public void setItems(List<T> data) {
        if (data == null) {
            return;
        }

        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public void appendItems(List<T> data) {
        if (data == null) {
            return;
        }

        mList.addAll(data);
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(viewGroup.getContext()).inflate(getViewLayoutId(), viewGroup, false);
            convertView.setTag(createViewHolder(convertView));
        }

        showData((VH) convertView.getTag(), getItem(position), position);

        return convertView;
    }

    protected abstract int getViewLayoutId();

    protected abstract VH createViewHolder(View view);

    protected abstract void showData(VH holder, T item, int position);
}
