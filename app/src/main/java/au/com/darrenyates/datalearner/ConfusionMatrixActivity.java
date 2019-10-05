/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
	DataLearner - a data-mining app for Android
	ConfusionMatrixActivity.java
    (C) Copyright Darren Yates 2018-2019
	Developed using a combination of Weka 3.6.15 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.6.15 is licensed GPLv2.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static au.com.darrenyates.datalearner.DataAnalysis.returnEval;
import static au.com.darrenyates.datalearner.DataAnalysis.classifierTree;
import static au.com.darrenyates.datalearner.DataAnalysis.classType;


public class ConfusionMatrixActivity extends AppCompatActivity {
	TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matrix_main);
		
		tv = findViewById(R.id.tvMatrix);
		Button btnCopy = findViewById(R.id.btnCopy);
		btnCopy.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard();
			}
		});
		
		tv.setMovementMethod(new ScrollingMovementMethod());
		try {
			if (classType == 0) {
				tv.append(returnEval.toClassDetailsString("=== Detailed Accuracy by Class ===\r\n"));
				tv.append("\r\n");
				tv.append(returnEval.toMatrixString("=== Confusion Matrix ===\r\n"));
			}
			if (classifierTree != "") {
				tv.append("\r\n");
				tv.append("=== Generated Classifier Model ===\r\n");
				tv.append(classifierTree);
			}
			tv.append("\r\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void copyToClipBoard() {
		ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData cd = ClipData.newPlainText("Confusion Matrx/Model Data", tv.getText());
		cb.setPrimaryClip(cd);
		showCopySteps();
	}
	
	void showCopySteps() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Model data bas been copied");
		alertDialog.setIcon(R.mipmap.ic_launcher);
		alertDialog.setMessage(getText(R.string.copy));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		alertDialog.show();
		
	}
	
}
