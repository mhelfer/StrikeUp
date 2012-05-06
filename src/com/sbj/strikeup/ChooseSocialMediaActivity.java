package com.sbj.strikeup;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChooseSocialMediaActivity extends Activity{

	private Button facebook;
	private Button fourSquare;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.socialmedia);

		facebook = (Button)findViewById(R.id.facebook);
		fourSquare = (Button)findViewById(R.id.fourSquare);
		
		facebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChooseSocialMediaActivity.this, FacebookActivity.class);
		    	startActivity(intent);
			}
		});
		
		fourSquare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChooseSocialMediaActivity.this, FacebookActivity.class);
		    	startActivity(intent);
			}
		});
	}
	
}
