/**
 * 
 */
package org.duyi;

import org.duyi.dataaccess.DBAccess;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Yi Du
 *
 */
public class LolLook extends Activity {
	
	private int currentQACursor;
	private int maxQACursor;
	private DBAccess dataAccess;
	private static final String TAG = "LOLLook";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.lollook);
		
		dataAccess = new DBAccess(LolLook.this);
		
		//init currentQACursor and max cursor
		SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
		currentQACursor = settings.getInt(Yestin_First.CURSOR_QA, 0);
		maxQACursor = settings.getInt(Yestin_First.CURSOR_QA_MAX, 0);
		//calculate for first time
		if(!settings.contains(Yestin_First.CURSOR_QA) || 
				!settings.contains(Yestin_First.CURSOR_QA_MAX))
			calculateCursor();
		else
			calculateMaxCursor();
		
		//fetch data only when no data in the current
		if(currentQACursor >= maxQACursor)
			fetchData();
		refreshData();
		Button buttonReturn = (Button)findViewById(R.id.buttonLolBack);
        buttonReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(LolLook.this, Yestin_First.class);				
				startActivity(intent);
				finish();
			}
		});
        
        Button ButtonNext = (Button)findViewById(R.id.buttonLolNext);
        ButtonNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refreshData();
			}
		});
	}

	private void calculateCursor() {
		if(dataAccess == null)
			dataAccess = new DBAccess(LolLook.this);
		String getMinLocal = "select min(id) from 't_qa';";
		Cursor cursor = dataAccess.getReadableDatabase().rawQuery(getMinLocal, null);
		Log.v(TAG, "query sucess");
		if(cursor.moveToFirst()){
			currentQACursor = cursor.getInt(0);
		}else{
			Log.e(TAG, "no min local data");
			currentQACursor = 0;
			return;
		}
		cursor.close();
		
		String getMaxLocal = "select max(id) from 't_qa';";
		cursor = dataAccess.getReadableDatabase().rawQuery(getMaxLocal, null);
		Log.v(TAG, "query sucess");
		if(cursor.moveToFirst()){
			maxQACursor = cursor.getInt(0);
		}else{
			Log.e(TAG, "no max local data");
			maxQACursor = 0;
			return;
		}
		cursor.close();
		SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
		settings.edit().putInt(Yestin_First.CURSOR_QA, currentQACursor).commit();
		settings.edit().putInt(Yestin_First.CURSOR_QA_MAX, maxQACursor).commit();
	}

	private void refreshData(){
		if(dataAccess == null)
			dataAccess = new DBAccess(LolLook.this);
		Cursor cursor;
		
		TextView textSource = (TextView)findViewById(R.id.textSourceWord);
		
		TextView textDesc = (TextView)findViewById(R.id.textLolDesc);
		
		TextView textGuessResult = (TextView)findViewById(R.id.textGuessResult);
		
		String sqlSelectQA1= "select id, wordcontent, questions, answer"+
			" from t_qa,t_word "+
			"where t_qa.wordid = t_word.wordid " +
			"and id = "+ currentQACursor+";";
		Log.i(TAG, sqlSelectQA1);
		String questionsContet = null;
		SQLiteDatabase db = dataAccess.getReadableDatabase();
		cursor = db.rawQuery(sqlSelectQA1, null);
		if(cursor == null || !cursor.moveToFirst()){
			Log.e(TAG, "cann't get next, cursor is null");
			if(!increaseAndReload()){
				cursor.close();
				return;
			}				
			else{
				cursor.close();
				refreshData();
				return;
			}
		}
		textSource.setText(cursor.getString(1));
		textGuessResult.setText("被猜成了:"+cursor.getString(3));
		questionsContet = cursor.getString(2);
		currentQACursor = cursor.getInt(0);
		
		if(questionsContet == null){
			if(!increaseAndReload()){
				cursor.close();
				return;
			}
			else{
				cursor.close();
				refreshData();
				return;
			}
		}
		String[] splits = questionsContet.split("q");
		if(splits == null || splits.length == 0){
			Log.e(TAG, "splits is null");
			if(!increaseAndReload()){
				cursor.close();
				return;
			}
			else{
				cursor.close();
				refreshData();
				return;
			}
		}
		textDesc.setText("");
		cursor.close();
		for(int i = 0; i < splits.length; i ++){
			String temp = splits[i];
			if(temp == null || temp.length() == 0 || temp.equals(""))
				continue;
			cursor = db.rawQuery("select questioncontent from t_question "+
					" where questionid = "+temp, null);
			if(cursor == null)
				continue;
			else if(!cursor.moveToFirst()){
				cursor.close();
				continue;
			}
			else{
				String result = cursor.getString(0);
				textDesc.setText(textDesc.getText() +"\n"+ (i)+"."+result);
				cursor.close();
			}
		}
		
		currentQACursor++;		
	}
	
	/**
	 * 
	 * @return false if no data in local database
	 */
	private boolean increaseAndReload(){
		SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
		if(currentQACursor == 0 &&
				maxQACursor == 0 &&
				settings.contains(Yestin_First.CURSOR_QA) &&
				settings.contains(Yestin_First.CURSOR_QA_MAX)
				)
			return false;
		currentQACursor++;
		if(currentQACursor > maxQACursor){
			//TODO this should first fetch data from server,
			//if cann't get data from server, 
			//then get set currentQACursor to 0
			currentQACursor = 0;
			return false;
		}
		return true;
	}
	private void fetchData() {
		//TODO fetch data from server
//		TextView textSource = (TextView)findViewById(R.id.textSourceWord);
//		
//		TextView textDesc = (TextView)findViewById(R.id.textLolDesc);
//		
//		TextView textGuessResult = (TextView)findViewById(R.id.textGuessResult);
	}
	
	private void calculateMaxCursor(){
		String getMaxLocal = "select max(id) from 't_qa';";
		Cursor cursor = dataAccess.getReadableDatabase().rawQuery(getMaxLocal, null);
		Log.v(TAG, "query max sucess");
		if(cursor.moveToFirst()){
			maxQACursor = cursor.getInt(0);
		}else{
			Log.e(TAG, "no max local data");
			maxQACursor = 0;
			return;
		}
		SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
		settings.edit().putInt(Yestin_First.CURSOR_QA_MAX, maxQACursor).commit();
		cursor.close();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
		settings.edit().putInt(Yestin_First.CURSOR_QA, currentQACursor).commit();
		settings.edit().putInt(Yestin_First.CURSOR_QA_MAX, maxQACursor).commit();
	    if(dataAccess != null)
	    	dataAccess.close();
	}

	
}
