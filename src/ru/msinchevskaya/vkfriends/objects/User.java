package ru.msinchevskaya.vkfriends.objects;

import org.json.JSONObject;

import ru.msinchevskaya.vkfriends.FieldNames;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class User implements Parcelable{
	
	public static final String TAG = ".objects.User";
	private long id;
	private String name;
	private String surname;
	
	private String fullname;
	
	public long getId(){
		return id;
	}
	
	public String getName(){
		return fullname;
	}
	
	public User(long id, String name, String surname){
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.fullname = String.format("%s%s%s", name, " ", surname);
	}
	
	public User(JSONObject json){
		
		id = json.optLong(FieldNames.ID);
		name = json.optString(FieldNames.NAME);
		surname = json.optString(FieldNames.SURNAME);

		this.fullname = String.format("%s%s%s", name, " ", surname);
		
		Log.i(TAG, fullname);
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(surname);
	}
	
	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
	    public User createFromParcel(Parcel in) {
	      return new User(in);
	    }

	    public User[] newArray(int size) {
	      return new User[size];
	    }
	  };
	  
	  private User(Parcel parcel) {
			id = parcel.readLong();
			name = parcel.readString();
			surname = parcel.readString();

			this.fullname = String.format("%s%s%s", name, " ", surname);
	  }

}
