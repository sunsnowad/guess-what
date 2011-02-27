/**
 * 
 */
package org.duyi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @author Yi Du
 *
 */
public class GiveDescHelp extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.givedeschelp);
        
        CheckBox checkBox = (CheckBox)findViewById(R.id.checkboxGiveDescHelp);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        SharedPreferences settings = getSharedPreferences(Yestin_First.PREFS_NAME, 0); 
		        
		        Editor editor = settings.edit();
				editor.putBoolean(Yestin_First.SHOW_TIP_GIVE, !isChecked);
		        editor.commit();
				
			}
		});
        
        Button button = (Button)findViewById(R.id.buttonKnowGiveDescHelp);
        button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(GiveDescHelp.this, GiveDesc.class);
                startActivity(intent);
                finish();			
			}
        	
        });
	}
}
