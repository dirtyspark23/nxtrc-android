/*
 * Copyright (c) 2014 - DeAngelo Mannie | Intravita LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techjoynt.android.nxt.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.techjoynt.android.nxt.R;

public class SplashScreenActivity extends SherlockFragmentActivity {
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		mContext = this;
		runAnimation();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			startActivity(new Intent(this, RemoteControl.class));
			finish();
		}
		return true;
	}
	
	private void runAnimation() {
		ImageView splash = (ImageView)findViewById(R.id.splash_logo);
		Animation appear = AnimationUtils.loadAnimation(this, R.anim.appear);
		splash.startAnimation(appear);
		
		appear.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationStart(Animation animation) {
				MediaPlayer player = MediaPlayer.create(mContext, R.raw.startdroid); // TODO: Thread Sensitive
				player.start();
				
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.release();
						mp = null;
						
						startActivity(new Intent(SplashScreenActivity.this, RemoteControl.class));
						finish();
					}
				});
			}
		});
	}
}