package ru.msinchevskaya.vkfriends.requests;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;


import ru.msinchevskaya.vkfriends.datalayer.OnlineDataAccessor;
import ru.msinchevskaya.vkfriends.objects.User;

public class UsersRequest{
	public static final String TAG = "requests.UsersRequest";

	protected static final String QUERY_PATTERN = "https://api.vk.com/method/friends.get?user_id=%s&fields=nickname&count=%s&offset=%s&v=5.25";

	private final long id;
	private final int count;
	private final int offset;

	public UsersRequest(long id, int count, int offset) {
		this.id = id;
		this.count = count;
		this.offset = offset;
	}
	
	public ArrayList<User> loadDataFromNetwork() throws Exception {
		final String query = String.format(QUERY_PATTERN, id, count, offset);
		final String response = OnlineDataAccessor.getAsString(query);
		final JSONObject object= new JSONObject(response);

		final JSONArray array = object.getJSONObject("response").getJSONArray("items");

		final ArrayList<User> users = new ArrayList<User>();
		for (int i = 0; i < array.length(); i ++) {
			users.add(new User(array.optJSONObject(i)));
		}

		return users;
	}
}