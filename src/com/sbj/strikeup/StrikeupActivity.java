package com.sbj.strikeup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.emorym.android_pusher.Pusher;
import com.emorym.android_pusher.PusherCallback;
import com.emorym.android_pusher.PusherChannel;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class StrikeupActivity extends ListActivity {
	
	private static final String LOG_TAG = "StrikeupActivity";
	
	private static final String PUSHER_APP_KEY = "afeec415b35c58ed1151";
	private static final String PUSHER_APP_SECRET = "17e963a6448bb0b4b5e6";
	
	//UI Components 
	Button send;
	
	EditText message;
	
	List<String> conversation;
	
	private String channelName = null; 
	private Pusher pusher;
	
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom);
        
        StrictMode.setThreadPolicy(policy);
        
        
        pusher = new Pusher(PUSHER_APP_KEY, PUSHER_APP_SECRET);
		
		send = (Button)findViewById(R.id.send);
		

		conversation = new ArrayList<String>();
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,conversation);
		setListAdapter(adapter);
		
		
		pusher.connect();
		
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			channelName = "private-"+extras.getString("LOCATION_ID");
			channelName = channelName.replace(" ", "");
		}
		
		PusherChannel channel = pusher.subscribe(channelName);
		channel.bindAll(new PusherCallback() {
			@Override
			public void onEvent(String eventName, JSONObject eventData, String channelName) {
				Toast.makeText(StrikeupActivity.this,
							   "Received\nEvent: " + eventName + "\nChannel: " + channelName + "\nData: " + eventData.toString(),
							   Toast.LENGTH_LONG).show();
				
				Log.d(LOG_TAG, "Received " + eventData.toString() + " for event '" + eventName + "' on channel '" + channelName + "'.");
				try{
					String message = eventData.getString("message");
					adapter.add(message);
					adapter.notifyDataSetChanged();
					
				} catch(Exception e){
					Log.e(LOG_TAG, "there was a problem parsing the incomming message"+ e.getMessage());
				}
			}
		});
		
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				message = (EditText)findViewById(R.id.message);
				String text = message.getText().toString();
				try{
					pusher.sendEvent("client-my_event", new JSONObject("{\"message\": \""+text+"\"}"), channelName);
					adapter.add(text);
					message.setText("");
				} catch(Exception e){
					Log.e(LOG_TAG, "there was a problem parsing the outgoing message"+ e.getMessage());
				}
			}
		});
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(LOG_TAG, "onDestroy");
		pusher.disconnect();
	}
}