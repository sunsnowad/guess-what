/**
 * 
 */
package org.duyi;

import org.duyi.dataaccess.DBAccess;
import org.duyi.ui.ConformDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Yi Du
 *
 *
 */
//TODO Only support auto generate words 
//because I have no enough time to input words by user/player
public class GiveDesc extends Activity {
	
	private static final String TAG="GiveDesc";
	private static final int MAX_LENGTH_DEFAULT = 15;
	
	private DBAccess dbAccess;
	
	private int cursorGiveDesWord = -1;
	private int maxIndexWordOfLocalDb = 0;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.givedesc);
        
        SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
        cursorGiveDesWord = settings.getInt(Yestin_First.CURSOR_WORD_DESC, -1);
        maxIndexWordOfLocalDb = settings.getInt(Yestin_First.CURSOR_WORD_MAX, -1);
        
        if(dbAccess == null)
        	dbAccess = new DBAccess(this);
        findViewById(R.id.buttonWordOpt).setEnabled(false);
        
        fetchWordToEditText();
        
        final EditText text = (EditText)findViewById(R.id.editTextGDesc);
        text.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
		        SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);         
		        final int maxLength = 
		        	settings.getInt(Yestin_First.MAX_LENGTH_DESC, MAX_LENGTH_DEFAULT); 
				if(text.getText() != null && text.getText().toString().length() > maxLength)
					text.setText(text.getText().toString().subSequence(0, MAX_LENGTH_DEFAULT-1));
				return false;//TODO
			}
		});
        
        Button buttonReturn = (Button)findViewById(R.id.buttonGReturnToMain);
        buttonReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeConnection();
				Intent intent = new Intent();
				intent.setClass(GiveDesc.this, Yestin_First.class);
				startActivity(intent);
				finish();
			}

		});
        
        Button buttonSubmit = (Button)findViewById(R.id.buttonGSubmit);
        buttonSubmit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new Thread(new Runnable(){

					@Override
					public void run() {

						pushDataToServer();
					}
					
				}).run();
				ConformDialog dialog = new ConformDialog(GiveDesc.this);
				dialog.show();
				pushDataToLocal();
				fetchWordToEditText();
				text.setText("");
			}
        });
	}

	private void fetchWordToEditText() {
		Cursor cur = null;
		SQLiteDatabase db = dbAccess.getReadableDatabase();
		
		//set cursor word
		if(cursorGiveDesWord == -1){
			String getMinLocal = "select min(wordid) from 't_word'";
			cur = db.rawQuery(getMinLocal, null);
			Log.v(TAG, "query sucess");
			if(cur.moveToFirst()){
				cursorGiveDesWord = cur.getInt(0);
			}
		}
		if(cur != null)
			cur.close();
		//get max index of word
		String getMaxLocal = "select max(wordid) from 't_word'";
		cur = db.rawQuery(getMaxLocal, null);
		Log.v(TAG, "query sucess");
		if(cur.moveToFirst()){
			maxIndexWordOfLocalDb = cur.getInt(0);
		}
		cur.close();
		
		if(cursorGiveDesWord >= maxIndexWordOfLocalDb)
			fetchWordToEditTextFromServer();
		
		//query local db
		cur = db.rawQuery("select wordcontent from t_word where wordid = '"+cursorGiveDesWord+"'", null);
		if(cur.moveToFirst()){
			EditText text = (EditText)findViewById(R.id.textGWordDisp);
			text.setText(cur.getString(0));
		}else{
			//TODO
		}
		cursorGiveDesWord++;
		cur.close();
	}

	private void fetchWordToEditTextFromServer() {
		// TODO fetch word to edit text from server
		
	}

	private void pushDataToLocal() {
		if(dbAccess == null)
			return;
		SQLiteDatabase db = dbAccess.getWritableDatabase();
		EditText textWord = (EditText)findViewById(R.id.textGWordDisp);
		int wordid;
		
		if(textWord.getText() == null || textWord.getText().equals("")){
			return;//TODO show message
		}
		String queryString = "select wordid from t_word where wordcontent = '"+
		textWord.getText().toString()+"';";
		Log.i(TAG, queryString);
		Cursor cur = 
			db.rawQuery(queryString, null);
		
		if(!cur.moveToFirst()){
			//TODO not exist ,need to insert into t_word
			wordid = -1;
		}
		else{
			wordid = cur.getInt(0);
		}
		cur.close();
		
		EditText textDesc = (EditText)findViewById(R.id.editTextGDesc);
		if(textDesc.getText() == null || textDesc.getText().equals("")){
			return;//TODO show message
		}
		db.execSQL("insert into t_question(wordid, questioncontent,provideduser)"+
				"values("+wordid+",'"+textDesc.getText().toString()+"',1);");
		Log.i(TAG, "push data to local");
		cur.close();
	}
	
	private void pushDataToServer(){
		//TODO push data to server; new thread
	}

	private void closeConnection() {
		if(dbAccess != null)
			dbAccess.close();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(dbAccess != null){
			dbAccess.close();
			Log.i(TAG, "close dbaccess");
		}
		//save cursor of word
        SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
	    Editor editor = settings.edit();
	    //TODO change
	    editor.putInt(Yestin_First.CURSOR_WORD_DESC, cursorGiveDesWord);
	    editor.putInt(Yestin_First.CURSOR_WORD_MAX, maxIndexWordOfLocalDb);
	    editor.commit();	    
	    //TODO new thread to post data to server or localdb	    
	}
	
	
}
