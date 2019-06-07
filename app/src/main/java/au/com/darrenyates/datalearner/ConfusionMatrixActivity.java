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
	@author: Darren Yates, Md Zahidul Islam, Junbin Gao, 2018-2019
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
