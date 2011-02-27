/**
 * 
 */
package org.duyi.ui;

import org.duyi.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Yi Du
 * 
 */
public class CongratulateDialog extends Dialog {

	public CongratulateDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialogcongratulate);
		setTitle("恭喜你");
	
		TextView text = (TextView) findViewById(R.id.text);
		text.setText("    猜对了!    ");
		// image.setImageResource(R.drawable);

		Button buttonYes = (Button) findViewById(R.id.buttonOk);
//		buttonYes.setHeight(5);
		buttonYes.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				dismiss();

			}
		});
	}

}
