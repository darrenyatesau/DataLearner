/*

DataLearner - a data-mining app for Android
ConfusionMatrixActivity.java
Copyright (C) Darren Yates, Md Zahidul Islam, Junbin Gao, 2018-2019
Developed using a combination of Weka 3.6.15 and algorithms developed by Charles Sturt University
DataLearner is licensed GPLv3.0, source code is available on GitHub
Weka 3.6.15 is licensed GPLv2.0, source code is available on GitHub

*/
package au.com.darrenyates.datalearner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import static au.com.darrenyates.datalearner.DataAnalysis.returnEval;
import static au.com.darrenyates.datalearner.DataAnalysis.classifierTree;


public class ConfusionMatrixActivity extends AppCompatActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matrix_main);
		
		TextView tv = findViewById(R.id.tvMatrix);
		tv.setMovementMethod(new ScrollingMovementMethod());
		try {
			tv.append(returnEval.toClassDetailsString("=== Detailed Accuracy by Class ===\r\n"));
			tv.append("\r\n");
			tv.append(returnEval.toMatrixString("=== Confusion Matrix ===\r\n"));
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
	
}
