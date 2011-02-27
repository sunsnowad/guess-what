/**
 * 
 */
package org.duyi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.duyi.dataaccess.DBAccess;
import org.duyi.dataaccess.Word;
import org.duyi.ui.CongratulateDialog;
import org.duyi.ui.WrongDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Yi Du
 *
 */
public class GuessWhat extends Activity {
	private DBAccess dbAccess;
	private Cursor cur;
	
	private CongratulateDialog conDialog;
	private WrongDialog wrongDialog;
	
	private static final String TAG = "guesswhat";
	
	private int timesDesc = 3;
	private int cursorWord = -1;
	private int timesDescOfCurrentWord = 0;
	private int maxIndexWordOfLocalDb = -1;
	private boolean localApp;
	private Word currentWord;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.guesswhat);
        Log.i(TAG, "create GuessWhat");
        
        if(dbAccess == null)
        	dbAccess = new DBAccess(this);
        
        SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
        timesDesc = settings.getInt(Yestin_First.DESC_TIMES, 3);
        cursorWord = settings.getInt(Yestin_First.CURSOR_WORD, 0);
        maxIndexWordOfLocalDb = settings.getInt(Yestin_First.CURSOR_WORD_MAX, 0);
//        if(!settings.contains(Yestin_First.CURSOR_WORD) ||
//        		!settings.contains(Yestin_First.CURSOR_WORD_MAX))
//        	updateInitConfiguration();
        localApp = settings.getBoolean(Yestin_First.USE_LOCAL_ONLY, true);
        
        if(cursorWord >= maxIndexWordOfLocalDb){
        	boolean canFetch = fetchDataFromServer();
        	if(canFetch)
        		updateCursorWord();
        	else
        		cursorWord = 0;
        }
        fetchWordData();
        
        Button buttonReturn = (Button)findViewById(R.id.buttonReturnToMain);
        buttonReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(GuessWhat.this, Yestin_First.class);				
				startActivity(intent);
				finish();
			}
		});
        
        Button buttonNextWord = (Button)findViewById(R.id.buttonChangeObj);
        buttonNextWord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				timesDescOfCurrentWord = 0;
				fetchWordData();
				EditText text = (EditText)findViewById(R.id.textGuessValue);
				text.setText("");
				text.setEnabled(true);
				text.setClickable(true);
				findViewById(R.id.buttonSubmit).setEnabled(true);
				findViewById(R.id.buttonNextDesc).setEnabled(true);
			}
		});
        
        Button buttonNextDesc = (Button)findViewById(R.id.buttonNextDesc);
        buttonNextDesc.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(timesDescOfCurrentWord >= timesDesc ||
						currentWord.getDescriptions().size() <= timesDescOfCurrentWord){
					EditText text = (EditText)findViewById(R.id.textGuessValue);
					text.setEnabled(false);
					text.setClickable(false);
					text.setText(currentWord.getWord());
					findViewById(R.id.buttonSubmit).setEnabled(false);
					findViewById(R.id.buttonNextDesc).setEnabled(false);
				}				
				else{
					TextView text = (TextView)findViewById(R.id.textDesc);
					String nextDesc = currentWord.getDescriptionByIndex(timesDescOfCurrentWord++);
					if(nextDesc == null){
						return;
					}
					else
						text.setText(nextDesc);
				}
			}
        	
        });
        
        Button buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				EditText text = (EditText)findViewById(R.id.textGuessValue);
				if(text.getText() == null || text.getText().equals(""))
					return;
				else{
					currentWord.setGuessedWord(text.getText().toString());
					if(currentWord.getWord() != null && 
							currentWord.getGuessedWord() != null &&
							currentWord.getWord().equals(currentWord.getGuessedWord()))
					{
						// show congratulation message
						conDialog = new CongratulateDialog(GuessWhat.this);
						conDialog.show();
					}else{
						wrongDialog = new WrongDialog(GuessWhat.this, currentWord.getWord());
						wrongDialog.show();
					}
					// write to local db
					writeToLocalDB(currentWord);
					fetchWordData();
					EditText textGuessV = (EditText)findViewById(R.id.textGuessValue);
					textGuessV.setText("");
					//TODO new thread to write to remote db?
				}
			
			}

        	
        });
        
        Button buttonHelp = (Button)findViewById(R.id.buttonHelp);
        buttonHelp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditText text = (EditText)findViewById(R.id.textGuessValue);
				text.setEnabled(false);
				text.setClickable(false);
				text.setText(currentWord.getWord());
				findViewById(R.id.buttonSubmit).setEnabled(false);
				findViewById(R.id.buttonNextDesc).setEnabled(false);
			}
		});
        
	}


	/**
	 * write guess result to local db
	 * @param currentWord
	 */
	private void writeToLocalDB(Word currentWord) {
		SQLiteDatabase db = dbAccess.getReadableDatabase();
		String questions = "";
		for(String uesedQues : currentWord.getUsedDesc()){
			questions +="q";
			cur = db.rawQuery("select questionid from t_question"+
					" where questioncontent = '"+uesedQues+"';", null);
			if(cur.moveToFirst()){//???
				questions += cur.getInt(0);
				cur.close();
			}
		}
		db = dbAccess.getWritableDatabase();
		db.execSQL("insert into t_qa(userid, wordid, questions, answer) "+
				"values(1,"+currentWord.getWordid()+",'"+questions+"','"+
				currentWord.getGuessedWord()+"');");
	}
	
	/**
	 * get the word and description data 
	 * from current max num of word index(wordid)
	 */
	private void fetchWordData(){//逻辑有点混乱
		SQLiteDatabase db;
		if(dbAccess == null)
			dbAccess = new DBAccess(GuessWhat.this);
		db = dbAccess.getReadableDatabase();

		boolean canFetchServer = fetchDataFromServer();
		boolean fetchWordDataSuccessful = false;
		while(!fetchWordDataSuccessful){
			//no data exists
			if(cursorWord >= maxIndexWordOfLocalDb){
				if(!canFetchServer){
					cursorWord = 0;
					updateCursorWord();
				}
				else
					updateCursorWord();
			}
			
			//query local db
			//get the next part of word and description to local db
			currentWord = new Word(cursorWord);
			timesDescOfCurrentWord = 0;
			cur = db.rawQuery("select wordcontent from t_word where wordid = '"+cursorWord+"';", null);
			if(cur.moveToFirst()){
				currentWord.setWord(cur.getString(0));
			}else{
				cursorWord ++;
				cur.close();
				Log.e(TAG, "no word content of word "+currentWord.getWord());		
				continue;
			}
			cur.close();
			cur = db.rawQuery("select questioncontent from t_question where wordid = '"+cursorWord+"';", null);
			int numOfDesc = 0;
			
			//current word not exist, cursor increase.
			if(!cur.moveToFirst()){
				cursorWord++;
				cur.close();
				continue;
			}
			do{
				currentWord.addDescriptions(cur.getString(0));
				numOfDesc++;
			}while(cur.moveToNext());// && numOfDesc < timesDesc);		
			cur.close();
			//
			TextView text = (TextView)findViewById(R.id.textDesc);
			text.setText(currentWord.getDescriptionByIndex(timesDescOfCurrentWord++));
			cursorWord++;
			fetchWordDataSuccessful = true;
		}
		cur.close();
	}

	/**
	 * 
	 * @return true if the local db has max and min wordid
	 */
	private boolean updateCursorWord(){
		//set cursor word
		if(cursorWord == -1){
			String getMinLocal = "select min(wordid) from 't_word';";
			cur = dbAccess.getReadableDatabase().rawQuery(getMinLocal, null);
			Log.v(TAG, "query sucess");
			if(cur.moveToFirst()){
				cursorWord = cur.getInt(0);
			}else{
				cur.close();
				return false;
			}
			cur.close();
		}
		
		//get max index of word of local db.
		String getMaxLocal = "select max(wordid) from 't_word';";
		cur = dbAccess.getReadableDatabase().rawQuery(getMaxLocal, null);
		Log.v(TAG, "query max index 1 sucess");
		if(cur.moveToFirst()){
			maxIndexWordOfLocalDb = cur.getInt(0);
		}else{
			return false;
		}
		cur.close();
		return true;
	}
	/**
	 * get data from server
	 */
	private boolean fetchDataFromServer() {//TODO !@!避免因为连接不上而死机
		if(localApp)
			return false;
		// TODO change server
		Log.i(TAG, "fetch data from server");
		//TODO change parameter
		String url = "http://mildu.info/PHPForDu/index.php?userid=0&wordid="+cursorWord+"&qaid=0";
		HttpGet httpRequest = new HttpGet(url);
		try {
			HttpResponse response = new DefaultHttpClient().execute(httpRequest);
			if(response.getStatusLine().getStatusCode() == 200){
				Log.i(TAG, response.toString());
				HttpEntity entity = response.getEntity();
				long length = entity.getContentLength();
				if(length != -1){
					InputStream in = entity.getContent();
					StringBuilder string = new StringBuilder();
					InputStreamReader reader = null;
					try {
						reader = new InputStreamReader(in, "utf-8");
						int c;
						while ((c = reader.read()) != -1) {
							string.append((char) c);
						}
					} catch (UnsupportedEncodingException e1) {
						Log.e(TAG, e1.getMessage());
						return false;
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
								Log.e(TAG, "cann't close inputstream qa");
								return false;
							}
						}
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								Log.e(TAG, "cann't close inputstream ");
								return false;
							}
						}
					}
					
					if(string != null){
						String result = string.toString();
						parseServerDataAndInsert(result);
					}
				}
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
			return false;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param result data from server
	 */
	private void parseServerDataAndInsert(String data) {
		Log.i(TAG, data);
		if(data == null)
			return;
		//TODO data format!!
		String[] results = data.split(DBAccess.SPLIT_TAG);
//		String user = results[0];
		String word = results[1];
		String question = results[2];
		String qa = null;
		if(results.length == 4)
			qa = results[3];
		else
			qa = "";
		
		DBAccess dbAccess = new DBAccess(this);
		dbAccess.insertToWord(word, dbAccess.getWritableDatabase());
		
//		dbAccess = new DBAccess(this);
//		dbAccess.insertToQuestion(user, dbAccess.getWritableDatabase());
		
		dbAccess = new DBAccess(this);
		dbAccess.insertToQuestion(question, dbAccess.getWritableDatabase());
		
		dbAccess = new DBAccess(this);
		dbAccess.insertToQA(qa, dbAccess.getWritableDatabase());
		
		return;		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "destroy GuessWhat");
	}

	@Override
	protected void onPause() {
		super.onPause();
	    if(cur != null){
	    	cur.close();
	    	Log.i(TAG, "close cursor");
	    }
	    if(dbAccess != null){
	    	dbAccess.close();
	    	Log.i(TAG, "close data access");
	    }
	    //TODO new thread to post data to server or localdb	
	    
		//save cursor of word
        SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0);
	    Editor editor = settings.edit();
	    editor.putInt(Yestin_First.CURSOR_WORD, cursorWord);
	    editor.putInt(Yestin_First.CURSOR_WORD_MAX, maxIndexWordOfLocalDb);
	    editor.commit();
	    

	}


}
