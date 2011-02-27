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
public class AboutDialog extends Dialog {

	/**
	 * @param context
	 */
	public AboutDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialogabout);
		setTitle("关于");

		TextView text = (TextView) findViewById(R.id.textDialogAbout);
		text.setText("作者：杜一\n日期:2010.11");
//		ImageView image = (ImageView) findViewById(R.id.imageDialogAbout);
		// image.setImageResource(R.drawable);

		Button buttonYes = (Button) findViewById(R.id.buttonDialogAbout);
		buttonYes.setHeight(5);
		buttonYes.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				dismiss();

			}
		});
	}
}
