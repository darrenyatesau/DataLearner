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

        TextView tv = (TextView) findViewById(R.id.tvMatrix);
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
