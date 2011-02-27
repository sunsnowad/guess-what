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
public class ConformDialog extends Dialog {

	public ConformDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialogconform);
		setTitle("提示");

		TextView text = (TextView) findViewById(R.id.text_DialogConform);
		text.setText("    提交成功!    ");
		// image.setImageResource(R.drawable);

		Button buttonYes = (Button) findViewById(R.id.buttonOk_DialogConform);
		buttonYes.setHeight(5);
		buttonYes.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				dismiss();
			}
		});
	}

}
