/**
 * 
 */
package org.duyi.dataaccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.duyi.R;
import org.duyi.Yestin_First;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Yi Du
 * 
 */

public class DBAccess extends SQLiteOpenHelper {
	private static final String DB_NAME = "guesswhat.db";
	private static final int DATABASE_VERSION = 1;

	private static final String TAG = "dbAccess";

	// some final string about http request data
	public static final String SPLIT_TAG = "l____l";
	public static final String D_HTTP_TWORD = "t_word";
	public static final String D_HTTP_TWORDID = "wordid";
	public static final String D_HTTP_WORDCONTENT = "wordcontent";
	public static final String D_HTTP_TQ = "t_question";
	public static final String D_HTTP_QID = "questionid";
	public static final String D_HTTP_QCONTENT = "questioncontent";
	public static final String D_HTTP_QPUser = "provideduser";
	public static final String D_HTTP_TQA = "t_qa";
	public static final String D_HTTP_QAID = "id";
	public static final String D_HTTP_USERID = "userid";
	public static final String D_HTTP_QAQS = "questions";
	public static final String D_HTTP_QAA = "answer";

	private Context userContext;

	public DBAccess(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		this.userContext = context;
	}

	DBAccess(Context context, String name, CursorFactory cursorFactory,
			int version) {
		super(context, name, cursorFactory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCreate = "CREATE TABLE if not exists [t_qa] ("
				+ "[id] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[userid] INTEGER  NULL," + "[wordid] INTEGER  NULL,"
				+ "[questions] VARCHAR(200)  NULL,"
				+ "[answer] VARCHAR(20)  NULL" + ");";
		db.execSQL(sqlCreate);
		sqlCreate = "CREATE TABLE if not exists [t_question] ("
				+ "[questionid] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[wordid] INTEGER  NOT NULL,"
				+ "[questioncontent] VARCHAR(50)  NULL,"
				+ "[provideduser] INTEGER  NULL" + ");";
		db.execSQL(sqlCreate);
		sqlCreate = "CREATE TABLE if not exists [t_user] ("
				+ "[userid] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[username] VARCHAR(20)  NULL,"
				+ "[userpassword] VARCHAR(100)  NULL" + ");";
		db.execSQL(sqlCreate);
		sqlCreate = "CREATE TABLE if not exists [t_word] ("
				+ "[wordid] INTEGER  NOT NULL PRIMARY KEY AUTOINCREMENT,"
				+ "[wordcontent] VARCHAR(40)  NULL" + ");";
		db.execSQL(sqlCreate);
		loadDataIfFirstUse(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// use to test
		SharedPreferences settings = userContext.getSharedPreferences(
				Yestin_First.PREFS_NAME, 0);
		settings.edit().putBoolean(Yestin_First.FIRST_USE, true).commit();
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly())
			return;
		// SharedPreferences settings = userContext.getSharedPreferences(
		// Yestin_First.PREFS_NAME, 0);
		// final boolean isFirstUse =
		// settings.getBoolean(Yestin_First.FIRST_USE,
		// true);
		// if (isFirstUse) {
		// loadDataIfFirstUse(db);
		// settings.edit().putBoolean(Yestin_First.FIRST_USE, false).commit();
		// }
	}

	private void loadDataIfFirstUse(SQLiteDatabase db) {
		//
		db.beginTransaction();
		try {
			String wordData = fetchRawData(R.raw.word);
			Log.i(TAG, wordData);
			insertToWord(wordData, db);
			// use dialog to test chinese chacters
			// Dialog d = new Dialog(userContext);
			// d.setTitle(wordData);
			// d.show();

			// /////////////////////////////
			String questionData = fetchRawData(R.raw.question);
			Log.i(TAG, questionData);
			insertToQuestion(questionData, db);
			// /////////////////////////////
			String qaData = fetchRawData(R.raw.qa);
			Log.i(TAG, qaData);
			insertToQA(qaData, db);
			db.setTransactionSuccessful();
			Log.i(TAG, "insert sucessful!");
		} finally {
			db.endTransaction();
		}
		// updateInitConfiguration(db);
	}

	// public void updateInitConfiguration(SQLiteDatabase db) {
	// SharedPreferences settings = userContext.getSharedPreferences(
	// Yestin_First.PREFS_NAME, 0);
	// Editor editor = settings.edit();
	// Cursor cursor;
	// // set cursor word
	// int cursorWord;
	// String getMinLocal = "select min(wordid) from 't_word';";
	// cursor = db.rawQuery(getMinLocal, null);
	// Log.v(TAG, "query sucess");
	// if (cursor.moveToFirst()) {
	// cursorWord = cursor.getInt(0);
	// } else {
	// cursorWord = 0;
	// }
	//
	// // get max index of word of local db.
	// int maxIndexWordOfLocalDb;
	// String getMaxLocal = "select max(wordid) from 't_word';";
	// cursor = db.rawQuery(getMaxLocal, null);
	// Log.v(TAG, "query max index 1 sucess");
	// if (cursor.moveToFirst()) {
	// maxIndexWordOfLocalDb = cursor.getInt(0);
	// cursor.close();
	// } else {
	// maxIndexWordOfLocalDb = 0;
	// }
	//
	// editor.putInt(Yestin_First.CURSOR_WORD, cursorWord);
	// editor.putInt(Yestin_First.CURSOR_WORD_MAX, maxIndexWordOfLocalDb);
	// editor.putBoolean(Yestin_First.FIRST_USE, false);
	// editor.commit();
	// cursor.close();
	// }

	/**
	 * get string data from raw data
	 * 
	 * @param id
	 * @return
	 */
	private String fetchRawData(int id) {
		InputStream in = userContext.getResources().openRawResource(id);
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
			return null;
		} catch (IOException e) {
			Log.e(TAG, "no file word");
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(TAG, "cann't close inputstream ");
					e.printStackTrace();
					return null;
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					Log.e(TAG, "cann't close inputstream ");
					e.printStackTrace();
					return null;
				}
			}
		}
		return string.toString();
	}

	public void insertToQA(String json, SQLiteDatabase database) {
		try {
			// insert question value to local db if first use
			JSONArray array = new JSONArray(json);
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					int qaid = obj.getInt(D_HTTP_QAID);
					int userid = obj.getInt(D_HTTP_USERID);
					String questions = obj.getString(D_HTTP_QAQS);
					String answer = obj.getString(D_HTTP_QAA);
					database.execSQL("insert into " + D_HTTP_TQA + "("
							+ D_HTTP_QAID + "," + D_HTTP_USERID + ","
							+ D_HTTP_QAQS + "," + D_HTTP_QAA + ") values("
							+ qaid + "," + userid + ",'" + questions + "','"
							+ answer + "');");
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "cann't parse local file qa:" + json);
		} catch (SQLiteConstraintException e) {
			Log.e(TAG, "qaid is unique");
		} finally {
			// if(database != null && database.isOpen())
			// database.close();
		}
	}

	public void insertToQuestion(String json, SQLiteDatabase database) {
		try {
			// insert question value to local db if first use
			JSONArray array = new JSONArray(json);
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					int questionid = obj.getInt(D_HTTP_QID);
					int wordid = obj.getInt(D_HTTP_TWORDID);
					String question = obj.getString(D_HTTP_QCONTENT);
					int providedUser = -1;
					try {
						providedUser = obj.getInt(D_HTTP_QPUser);
					} catch (JSONException e) {
						Log.e(TAG,
								"cann't parse local file question:provieded user is null");
					}
					database.execSQL("insert into " + D_HTTP_TQ + "("
							+ D_HTTP_QID + "," + D_HTTP_TWORDID + ","
							+ D_HTTP_QCONTENT + "," + D_HTTP_QPUser
							+ ") values(" + questionid + "," + wordid + ",'"
							+ question + "'," + providedUser + ");");
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "cann't parse local file question:" + json);
		} catch (SQLiteConstraintException e) {
			Log.e(TAG, "qid is unique");
		} finally {
			// if(database != null && database.isOpen())
			// database.close();
		}
	}

	public void insertToWord(String json, SQLiteDatabase database) {
		try {
			// insert word value to local db if first use
			JSONArray array = new JSONArray(json);
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					int wordid = obj.getInt(D_HTTP_TWORDID);
					String word = obj.getString(D_HTTP_WORDCONTENT);
					database.execSQL("insert into " + D_HTTP_TWORD + "("
							+ D_HTTP_TWORDID + "," + D_HTTP_WORDCONTENT
							+ ") values(" + wordid + ",'" + word + "');");
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "cann't parse local file word:" + json);
		} catch (SQLiteConstraintException e) {
			Log.e(TAG, "wordid is unique");
		} finally {
			// if(database != null && database.isOpen())
			// database.close();
		}
	}

}