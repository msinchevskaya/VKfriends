package ru.msinchevskaya.vkfriends.widgets;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

public class AppendWrapperAdapter implements WrapperListAdapter {
	public static final String TAG = "widgets.AppendWrapperAdapter";

	public interface AppendListener {
		/**
		 * Требуется ли догрузка новых данных
		 * @return
		 */
		public boolean isAppendable();

		/**
		 * Запрос на догрузку новых данных
		 */
		public void append();
	}

	public static final int DEFAULT_PORTION_FOR_PENDING = 5 + 1;

	private final DataSetObservable mDataSetObservable = new DataSetObservable();

	private final ListAdapter mAdapter;
	private final AppendListener mAppendListener;
	private final int mPortionForPending;

	public AppendWrapperAdapter(ListAdapter adapter, AppendListener listener) {
		this(adapter, DEFAULT_PORTION_FOR_PENDING, listener);
	}

	public AppendWrapperAdapter(ListAdapter adapter, int portionForPending, AppendListener listener) {
		mAdapter = adapter;
		mPortionForPending = portionForPending;
		mAppendListener = listener;
	}

	@Override
	public ListAdapter getWrappedAdapter() {
		return mAdapter;
	}

	@Override
	public int getCount() {
		return getWrappedAdapter().getCount();
	}

	@Override
	public Object getItem(int position) {
		return getWrappedAdapter().getItem(position);
	}

	@Override
	public int getItemViewType(int position) {
		return getWrappedAdapter().getItemViewType(position);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return getWrappedAdapter().areAllItemsEnabled();
	}

	@Override
	public int getViewTypeCount() {
		return getWrappedAdapter().getViewTypeCount();
	}

	@Override
	public boolean hasStableIds() {
		return getWrappedAdapter().hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return getWrappedAdapter().isEmpty();
	}

	@Override
	public boolean isEnabled(int position) {
		return getWrappedAdapter().isEnabled(position);
	}

	@Override
	public long getItemId(int position) {
		return getWrappedAdapter().getItemId(position);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mDataSetObservable.registerObserver(observer);
		getWrappedAdapter().registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		getWrappedAdapter().unregisterDataSetObserver(observer);
		mDataSetObservable.unregisterObserver(observer);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mAppendListener.isAppendable() && mPortionForPending == getCount() - position) {
			mAppendListener.append();
		}

		return getWrappedAdapter().getView(position, convertView, parent);
	}




	public void appended() {
		mDataSetObservable.notifyChanged();
	}
}