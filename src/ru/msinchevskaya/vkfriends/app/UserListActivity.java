package ru.msinchevskaya.vkfriends.app;


import java.util.ArrayList;
import java.util.List;

import ru.msinchevskaya.vkfriends.ExtrasNames;
import ru.msinchevskaya.vkfriends.R;
import ru.msinchevskaya.vkfriends.helpers.StringHelper;
import ru.msinchevskaya.vkfriends.objects.User;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import ru.msinchevskaya.vkfriends.widgets.AppendWrapperAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UserListActivity extends ActionBarActivity{
	
	public static final String TAG = ".application.UserListActivity";
	
	private SearchView mSearchView;
	private SearchListener mSearchListener;
	private UsersReciever mReciever;
	private View mProgress;
	private String mId;
	private ListView mListView;
	private ArrayAdapter<User> mAdapter;
	private final AppendListener mAppendListener = new AppendListener();
	
	private boolean isAppendable;
	
	private final ArrayList<User> mUsers = new ArrayList<User>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		isAppendable = true;
		
		mSearchListener = new SearchListener();
		mProgress = findViewById(R.id.progress);
		mProgress.setVisibility(View.INVISIBLE);
		
		mReciever = new UsersReciever();
		
		mListView = (ListView) findViewById(R.id.lv_friends);
		
		mAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.row_friend, mUsers);
		final AppendWrapperAdapter append = new AppendWrapperAdapter(mAdapter, mAppendListener);
		mListView.setAdapter(append);
		
		UserClickListener userClickListener = new UserClickListener();
		mListView.setOnItemClickListener(userClickListener);
		
		IntentFilter filter = new IntentFilter(LoadUserService.LOAD_USER_ACTION);
		registerReceiver(mReciever, filter);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mId = savedInstanceState.getString(ExtrasNames.EXTRAS_ID);
		final List<User> list = savedInstanceState.getParcelableArrayList(ExtrasNames.EXTRAS_USER);
		mUsers.addAll(list);
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ExtrasNames.EXTRAS_ID, mId);
		outState.putParcelableArrayList(ExtrasNames.EXTRAS_USER, mUsers);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReciever);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.action_bar, menu);
	    MenuItem searchItem = menu.findItem(R.id.action_search);
	    mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	    mSearchView.setOnQueryTextListener(mSearchListener);
	    return true;
	}
	
	protected void restartData() {
		mUsers.clear();
		loadDataBegin();
	}
	
	protected void loadDataBegin() {
		mProgress.setVisibility(View.VISIBLE);
		Intent intent = new Intent(getApplicationContext(), LoadUserService.class);
		intent.putExtra(ExtrasNames.EXTRAS_ID, Long.valueOf(mId));
		intent.putExtra(ExtrasNames.EXTRAS_COUNT, 50);
		intent.putExtra(ExtrasNames.EXTRAS_OFFSET, mUsers.size());
		
		startService(intent);
	}

	protected void loadDataComplete(List<User> users) {
		isAppendable = !(users.size() == 0);

		if (isAppendable) {
			mUsers.addAll(users);
			mAdapter.notifyDataSetChanged();
		}

		mProgress.setVisibility(View.GONE);
	}
	
	protected void loadDataError(){
		Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();		
		mProgress.setVisibility(View.GONE);
	}
	
	private class UserClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mId = Long.toString(mUsers.get(position).getId());
			restartData();
		}
	}
	
	private class SearchListener implements OnQueryTextListener{

		@Override
		public boolean onQueryTextChange(String newChar) {
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String text) {
			if (StringHelper.isNumber(text)){
				mId = text;
				loadDataBegin();
			}
			else {
				Toast.makeText(getApplicationContext(), getString(R.string.invalid_id), Toast.LENGTH_LONG).show();
			}
			return false;
		}
	}
	
	private class UsersReciever extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getIntExtra(ExtrasNames.RESULT_CODE, 0)){
			case LoadUserService.RESULT_SUCCESS:
				List<User> users = intent.getParcelableArrayListExtra(ExtrasNames.EXTRAS_USER);
				loadDataComplete(users);
				break;
			case LoadUserService.RESULT_ERROR:
				loadDataError();
			}
		}
	}
	
	protected class AppendListener implements AppendWrapperAdapter.AppendListener {
		@Override
		public boolean isAppendable() {
			return isAppendable;
		}

		@Override
		public void append() {
			loadDataBegin();
		}
	}
}
