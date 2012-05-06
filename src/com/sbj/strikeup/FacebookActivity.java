package com.sbj.strikeup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.sbj.strikeup.facebook.BaseRequestListener;

public class FacebookActivity extends ListActivity {
	
	private static final String LOG_TAG = "FacebookActivity";
	
	Facebook facebook = new Facebook("347693635290071");
	private SharedPreferences mPrefs;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler;
	
	List<String>checkins;
	ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook);
		
		checkins = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,checkins);
		setListAdapter(adapter);
		
		doFacebookSetup();
		
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(LOG_TAG, "you click on item " + position);
				Intent intent = new Intent(FacebookActivity.this, StrikeupActivity.class);
				intent.putExtra("LOCATION_ID", adapter.getItem(position));
				startActivity(intent);
			}
		});
		
		mHandler = new Handler();
				
		
	}
	
	private void doFacebookSetup(){
    	/*
         * Get existing access_token if any
         */
    	mAsyncRunner = new AsyncFacebookRunner(facebook);
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
    	
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {
        
	    	facebook.authorize(this, new String[] {"user_checkins, user_status"},new DialogListener() {
	            @Override
	            public void onComplete(Bundle values) {}
	
	            @Override
	            public void onFacebookError(FacebookError error) {}
	
	            @Override
	            public void onError(DialogError e) {}
	
	            @Override
	            public void onCancel() {}
	        });
        }
    }
	
	public static long formatTimeForEvent(long pacificTime) {
        return (pacificTime + TimeZone.getTimeZone("America/Los_Angeles").getOffset(pacificTime))
            - TimeZone.getDefault().getOffset(pacificTime);
    }
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebook.authorizeCallback(requestCode, resultCode, data);
        
        Bundle parameters = new Bundle();
		parameters.putString("date_format", "U");
        mAsyncRunner.request("me/checkins", parameters, new BaseRequestListener() {
			
			@Override
			public void onComplete(final String response, Object state) {
				Log.i(LOG_TAG, "Response: " + response);
				mHandler.post(new Runnable() {
		            @Override
		            public void run() {
		            	try{
							JSONObject jsonResponse = new JSONObject(response);
							JSONArray array = jsonResponse.getJSONArray("data");
							JSONObject data;
							JSONObject place;
							long longDate;
							Calendar date = Calendar.getInstance();
							for(int i = 0; i<array.length(); i++){
								data = (JSONObject)array.get(i);
								
								/*longDate = data.getLong("created_time");
								try{
									date.setTimeInMillis(formatTimeForEvent(longDate));
									SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
									Log.i(LOG_TAG,"date = " + sdf.format(date));
								} catch(Exception e){
									Log.i(LOG_TAG,"there was a problem parsing the date from FB " + e.getMessage());
								}
								
								Calendar yesterday = Calendar.getInstance();
								yesterday.add(Calendar.DAY_OF_YEAR, -1);
								if(date != null && date.before(yesterday)){
									break;
								}*/
								
								place = (JSONObject)data.getJSONObject("place");
								adapter.add(place.getString("name"));
								Log.e(LOG_TAG, "place = " + place.getString("name"));
							}
						} catch(JSONException e){
							Log.e(LOG_TAG, "there was a problem parsing the FB response" + e.getMessage());
						}
		            }
				});
			}
		});
        
        
    }
    
    @Override
    public void onResume() {    
        super.onResume();
        facebook.extendAccessTokenIfNeeded(this, null);
    }
	
}
