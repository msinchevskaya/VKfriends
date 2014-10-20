package ru.msinchevskaya.vkfriends.app;

import java.util.ArrayList;

import ru.msinchevskaya.vkfriends.ExtrasNames;
import ru.msinchevskaya.vkfriends.helpers.NetworkHelper;
import ru.msinchevskaya.vkfriends.objects.User;
import ru.msinchevskaya.vkfriends.requests.UsersRequest;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LoadUserService extends Service {
	
	public static final String TAG = ".app.LoadUserService";
	public static final String LOAD_USER_ACTION = "loaduseraction";
	
	public static final int RESULT_SUCCESS = 1;
	public static final int RESULT_ERROR = 2;

	private long id;
	private int count;
	private int offset;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		id = intent.getLongExtra(ExtrasNames.EXTRAS_ID, 1);
		count = intent.getIntExtra(ExtrasNames.EXTRAS_COUNT, 0);
		offset = intent.getIntExtra(ExtrasNames.EXTRAS_OFFSET, 0);
		
		startLoading();

		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void startLoading(){
		final UsersRequest request = new UsersRequest(id, count, offset);
		
		if (!NetworkHelper.isNetworkAvailable(getApplicationContext()))
		{
			sendErrorIntent();
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					final ArrayList<User> users = request.loadDataFromNetwork();
					sendIntent(users);
				} catch (Exception e) {
					e.printStackTrace();
					sendErrorIntent();
				}
			}
		}).start();
	}
	
	private void sendErrorIntent(){
		Intent intent = new Intent();
		intent.setAction(LOAD_USER_ACTION);
		intent.putExtra(ExtrasNames.RESULT_CODE, RESULT_ERROR);
		sendBroadcast(intent);
	}
	
	private void sendIntent(ArrayList<User> users){
		Intent intent = new Intent();
		intent.setAction(LOAD_USER_ACTION);
		intent.putExtra(ExtrasNames.RESULT_CODE, RESULT_SUCCESS);
		intent.putParcelableArrayListExtra(ExtrasNames.EXTRAS_USER, users);
		sendBroadcast(intent);
	}
}
