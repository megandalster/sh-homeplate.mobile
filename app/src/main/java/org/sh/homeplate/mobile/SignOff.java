package org.sh.homeplate.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class SignOff extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signoff);
	}
	
	public void routeFinish(View view)
	{
		// in the future, maybe add a sound here. who knows
		finish();
	}
}
