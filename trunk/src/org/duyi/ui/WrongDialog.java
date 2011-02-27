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
public class WrongDialog extends Dialog {
	private String rightResult;

	public WrongDialog(Context context, String string) {
		super(context);
		this.rightResult = string;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialogwrong);
		setTitle("很遗憾");

		TextView text = (TextView) findViewById(R.id.text);
		text.setText("正确答案是" + rightResult);
//		ImageView image = (ImageView) findViewById(R.id.imagewrongdialog);
		// image.setImageResource(R.drawable);

		Button buttonYes = (Button) findViewById(R.id.buttonWrongDiaOk);
		buttonYes.setHeight(5);
		buttonYes.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				dismiss();

			}
		});
	}
}
