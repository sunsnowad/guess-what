package org.duyi;

import org.duyi.ui.AboutDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class Yestin_First extends Activity {
	
	public static final String SHOW_TIP_GUESS = "SHOW_TIP";
	public static final String SHOW_TIP_GIVE = "show_tip_give_q";
//	public static final String FIRST_USE = "first use";

	// times of word description before changing a new word
	public static final String DESC_TIMES = "DESC_TIMES";

	// current word cursor
	public static final String CURSOR_WORD = "CURSOR_WORD";
	// current word cursor of give desc
	public static final String CURSOR_WORD_DESC = "cursor_word_desc";
	//current qa cursor
	public static final String CURSOR_QA = "CURSOR_QA";
	// max word cursor/index
	public static final String CURSOR_WORD_MAX = "cursor_word_max";
	// max description length
	public static final String MAX_LENGTH_DESC = "max length desc";
	//max qa cursor 
	public static final String CURSOR_QA_MAX = "max cursor qa";
	public static final String PREFS_NAME = "duyi.org";
	
	public static final String FIRST_USE = "first use";
	
	public static final String USE_LOCAL_ONLY = "use local only";

//	private static final String TAG = "yestin_firsttag";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.relativelayout);

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		// whether show tip of the ui
		//
		final boolean isShowTip = settings.getBoolean(SHOW_TIP_GUESS, true);
		final boolean isShowTipGieDesc = settings.getBoolean(SHOW_TIP_GIVE,
				true);
		

		Button buttonExit = (Button) findViewById(R.id.buttonExit);
		buttonExit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Button buttonAnswerQ = (Button) findViewById(R.id.buttonAnswerQ);
		buttonAnswerQ.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if (isShowTip) {
					intent.setClass(Yestin_First.this, GuessWhatHelp.class);
				} else {
					intent.setClass(Yestin_First.this, GuessWhat.class);
				}
				startActivity(intent);
				finish();
			}
		});

		Button buttonGiveDesc = (Button) findViewById(R.id.buttonGiveQ);
		buttonGiveDesc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if (isShowTipGieDesc) {
					intent.setClass(Yestin_First.this, GiveDescHelp.class);
				} else {
					intent.setClass(Yestin_First.this, GiveDesc.class);
				}
				startActivity(intent);
				finish();
			}
		});

		Button buttonLol = (Button) findViewById(R.id.buttonLol);
		buttonLol.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Yestin_First.this, LolLook.class);
				startActivity(intent);
				finish();
			}
		});

		Button buttonAbout = (Button) findViewById(R.id.buttonRelative);
		buttonAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AboutDialog dialog = new AboutDialog(Yestin_First.this);
				dialog.show();
			}
		});
	}

	

	// private static String unicodToString(String str){
	// Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
	// Matcher matcher = pattern.matcher(str);
	// char ch;
	// while(matcher.find()){
	// ch = (char)Integer.parseInt(matcher.group(2), 16);
	// str = str.replace(matcher.group(), ch+"");
	// }
	// return str;
	// }
}