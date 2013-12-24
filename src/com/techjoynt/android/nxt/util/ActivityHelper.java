package com.techjoynt.android.nxt.util;

import android.content.Context;
import android.content.Intent;

import com.techjoynt.android.nxt.activity.AboutActivity;

public class ActivityHelper {
	public static void toAbout(Context context) {
		context.startActivity(new Intent(context, AboutActivity.class));
	}
}
