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
 * DataLearner - a data-mining app for Android
 * MainActivity.java
 * (C) Copyright Darren Yates 2018-2019
 * Developed using a combination of Weka 3.6.15 and algorithms developed by Charles Sturt University
 * DataLearner is licensed GPLv3.0, source code is available on GitHub
 * Weka 3.6.15 is licensed GPLv2.0, source code is available on GitHub
 */
package au.com.darrenyates.datalearner;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import weka.core.Instances;

import weka.core.converters.ConverterUtils;
import weka.core.converters.DLCSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class MainActivity extends AppCompatActivity {
	
	/*
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private static Button btnCM;
	private static TextView tvStatus, cci, ici, kappa, mae, rmse, rae, rrse, tni;
	
	private static String nameClassifier;
	private static int validate;
	private static Uri uriDataset;
	private static Instances data;
	public static boolean killThread = false;
	static boolean isThreadRunning = false;
	static String statusUpdateStore = "Ready.";
	
	//DataAnalysis task = null;
	private static Thread thread;
	private static ThreadGroup threadGroup;
	static int alType = 1;
	static int viewCount = 0;
	static int classType = 0;
	
	LoadFragment fragL = new LoadFragment();
	SelectFragment fragS = new SelectFragment();
	RunFragment fragR = new RunFragment();
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		
		TabLayout tabLayout = findViewById(R.id.tabs);
		
		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

//		new SelectFragment.OnSelectionUpdateListener() {
//
//			public void changeInterface(int alType) {
//				fragR.setInterface(alType);
//			}
//
//		};
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.about) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("About DataLearner");
			builder1.setMessage("Version 1.1.6\r\n© Copyright Darren Yates, Supervisors: Zahid Islam, Junbin Gao\r\nDeveloped as part of a research PhD at the School of Computing and Mathematics, Charles Sturt University, 2018-2019." +
					"\r\n\r\nDataLearner is a data-mining app powered by the Weka data-mining core and includes " +
					"algorithms developed by Charles Sturt University.\r\nWeka was created by the University of Waikato.");
			AlertDialog alert1 = builder1.create();
			alert1.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alert1.show();
			return true;
		}
		if (id == R.id.clear) {
			tvStatus.setText(getResources().getString(R.string.str_ready));
			return true;
		}
		
		if (id == R.id.changelog) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("DataLearner - changes");
			builder1.setMessage(getResources().getString(R.string.str_changes));
			AlertDialog alert1 = builder1.create();
			alert1.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alert1.show();
			return true;
		}
		if (id == R.id.help) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("DataLearner - Help");
			builder1.setMessage(getText(R.string.str_help));
			AlertDialog alert1 = builder1.create();
			alert1.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alert1.show();
			return true;
		}
		if (id == R.id.yt_help) {
			Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:H-7pETJZf-g"));
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/H-7pETJZf-g"));
			try {
				startActivity(appIntent);
			} catch (ActivityNotFoundException ex) {
				startActivity(webIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// [1] ------------------------------------------------------------------------------------------------

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class LoadFragment extends Fragment {

		private static final int READ_REQUEST_CODE = 42;
		TextView tvFile, tvIntro, tvTest, tvStats;
		Spinner spinClassAtt;
		String tvFileName;
		Button btnForce;
		
		public LoadFragment() {
		}
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_tab1, container, false);
			tvStats = rootView.findViewById(R.id.tvStats);
			tvIntro = rootView.findViewById(R.id.tvIntro);
			spinClassAtt = rootView.findViewById(R.id.spinClass);
			btnForce = rootView.findViewById(R.id.btnForce);
			btnForce.setEnabled(false);
			Button btnLoad = rootView.findViewById(R.id.button1);
			Button btnDemo = rootView.findViewById(R.id.button);
			btnLoad.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					performFileSearch();

				}
			});
			btnDemo.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					uriDataset = Uri.parse("android.resource://au.com.darrenyates.datalearner/raw/rain.csv");
					tvStats.setText("");
					
					Instances newdata = null;
					try {
						InputStream inputStream = getContext().getResources().openRawResource(R.raw.rain);
						ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
						newdata = csvReader(inputStream, uriDataset.toString());
						inputStream.close();
						data = newdata;
						data.setClassIndex(data.numAttributes() - 1);
						displayData();
						spinClassAtt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
								data.setClassIndex(i);
								displayData();
								RunFragment.cleanDisplay();
							}
							
							@Override
							public void onNothingSelected(AdapterView<?> adapterView) {
							}
						});
						setSpinClass(newdata.numAttributes());
						spinClassAtt.setSelection(newdata.numAttributes() - 1);
						showDemoSteps();
						btnForce.setEnabled(true);
					} catch (Exception e) {
						statusUpdateStore += "\r\nERROR: " + e.getMessage() + "\r\n";
						tvStats.append("\r\nERROR: " + e.getMessage() + "\r\n");
					}
				}
				
			});
			btnForce.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					showLimits();
				}
			});
			return rootView;
		}
		
		void showDemoSteps() {
			AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
			alertDialog.setTitle("You've got this!");
			alertDialog.setIcon(R.mipmap.ic_launcher);
			alertDialog.setMessage(getText(R.string.demo_text));
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alertDialog.show();
			
		}
		
		void performFileSearch() {
			if (viewCount < 1) {
				viewCount++;
				AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
				alertDialog.setTitle("First-time users");
				alertDialog.setIcon(R.mipmap.ic_launcher);
				alertDialog.setMessage("If you can't find your folder on the next screen, tap the three-dot menu button top-right and select 'Show internal storage'," +
						" then navigate to your dataset file (you only need set this once).");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						loadOpenFile();
					}
				});
				alertDialog.show();
			} else loadOpenFile();
		}
		
		void loadOpenFile() {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			startActivityForResult(intent, READ_REQUEST_CODE);
		}
		
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
			if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
				uriDataset = null;
				if (returnIntent != null) {
					tvStats.setText("Retrieving data - please wait...");
					uriDataset = returnIntent.getData();
					System.out.println("FILE: " + uriDataset.toString());
					Cursor returnCursor = getActivity().getContentResolver().query(uriDataset, null, null, null, null);
					returnCursor.moveToFirst();
					int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					System.out.println(returnCursor.getString(nameIndex));
					String fileCut = returnCursor.getString(nameIndex);
					int split = fileCut.lastIndexOf('/');
					fileCut = fileCut.substring(split + 1);
					split = fileCut.lastIndexOf(':');
					System.out.println(fileCut);
					if (fileCut.endsWith("arff") || fileCut.endsWith("csv")) {
						tvStats.setText("");
						data = getData();
//						data.setClassIndex(data.numAttributes()-1);
//						displayData();
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setTitle("Warning");
						alertDialog.setMessage("DataLearner only accepts Weka-style ARFF files or CSV files.");
						alertDialog.setIcon(R.mipmap.ic_launcher);
						alertDialog.show();
					}
					//					tvIntro.getLayoutParams().height = 0;

				}
			} else {
				System.out.println("ERROR: Problem with file read.");
			}
		}
		
		
		void displayData() {
			tvStats.setText("");
			tvStats.append(data.toSummaryString());
			tvStats.append("\nClass attribute: (" + ((data.classIndex() + 1) + ") " + data.classAttribute().name()));
			if (data.classAttribute().isNominal())
				tvStats.append("\nAttribute type : Nominal/Categorical");
			else tvStats.append("\nAttribute type : Numeric");
			tvStats.append("\nDistinct values: " + data.numDistinctValues(data.classAttribute()) + "\n");
			tvIntro.setText("");
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvIntro.getLayoutParams();
			params.topMargin = 0;
			params.height = 0;
			tvIntro.setLayoutParams(params);
		}
		
		
		//		public Instances getData(String filePath) {
		public Instances getData() {
			Cursor returnCursor = getActivity().getContentResolver().query(uriDataset, null, null, null, null);
			returnCursor.moveToFirst();
			int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
			System.out.println(returnCursor.getString(nameIndex));
			String fileCut = returnCursor.getString(nameIndex);
			int split = fileCut.lastIndexOf('/');
			fileCut = fileCut.substring(split + 1);
			split = fileCut.lastIndexOf(':');
			System.out.println(fileCut);
			String filePath = fileCut;

			Instances newdata = null;
			try {
				InputStream inputStream = getContext().getContentResolver().openInputStream(uriDataset);
				ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
				if (filePath.endsWith("arff")) {
					newdata = dataSource.getDataSet();
				} else {
					newdata = csvReader(inputStream, filePath);
				}
				inputStream.close();
				data = new Instances(newdata);
				data.setClassIndex(data.numAttributes() - 1);
				spinClassAtt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						data.setClassIndex(i);
						displayData();
						RunFragment.cleanDisplay();
					}

					@Override
					public void onNothingSelected(AdapterView<?> adapterView) {

					}
				});
				setSpinClass(newdata.numAttributes());
				spinClassAtt.setSelection(newdata.numAttributes() - 1);
				btnForce.setEnabled(true);
				
			} catch (Exception e) {
				statusUpdateStore += "\r\nERROR: " + e.getMessage() + "\r\n";
				tvStats.append("\r\nERROR: " + e.getMessage() + "\r\n");
			}
			return newdata;
		}
		
		void showLimits() {
			double numInstances = data.numInstances();
			double numDistinct = data.numDistinctValues(data.classAttribute());
			double ratio = numInstances / numDistinct;
			if (ratio < 3 || numDistinct > 255) { // too many class values for number of instances = better as numeric
				AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
				alertDialog.setTitle("Sure about this?");
				alertDialog.setIcon(R.mipmap.ic_launcher);
				alertDialog.setMessage(getText(R.string.noconvert));
				if (numDistinct <= 255) {
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, do it.", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								data = convertClass(data);
								displayData();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else {
					alertDialog.setMessage("Sorry, your class attribute has more than 255 distinct values.");
				}
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, let's not.", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
				alertDialog.show();
			} else {
				try {
					data = convertClass(data);
					displayData();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		
		Instances csvReader(InputStream inputStream, String filePath) throws Exception {
			String fileCut = filePath;
			int split = fileCut.lastIndexOf('/');
			fileCut = fileCut.substring(split + 1);
			DLCSVLoader cl = new DLCSVLoader();
			cl.setSource(inputStream);
			Instances dataSet = cl.getDataSet();
//			NumericToNominal ntn = new NumericToNominal();
//			String[] options = new String[2];
//			options[0] = "-R";
//			options[1] = Integer.toString(dataSet.numAttributes());
//			ntn.setOptions(options);
//			ntn.setInputFormat(dataSet);
//			Instances newData = Filter.useFilter(dataSet, ntn);
//			newData.setRelationName(fileCut);
//			return newData;
			dataSet.setRelationName(fileCut);
			dataSet.setClassIndex(dataSet.numAttributes() - 1);
//			if (dataSet.numDistinctValues(dataSet.classAttribute()) < 256 ) dataSet = convertClass(dataSet);
			return dataSet;
		}
		
		Instances convertClass(Instances input) throws Exception {
			Instances output = new Instances(input);
			NumericToNominal ntn = new NumericToNominal();
			String[] options = new String[2];
			options[0] = "-R";
			options[1] = Integer.toString(output.classIndex() + 1);
			ntn.setOptions(options);
			ntn.setInputFormat(output);
			Instances newData = Filter.useFilter(output, ntn);
			return newData;
		}
		
		void setSpinClass(int number) {
			ArrayList<String> arraySpin = new ArrayList<>();
			for (int i = 0; i < number; i++) {
				arraySpin.add(Integer.toString(i + 1));
			}
			String[] arrayString = arraySpin.toArray(new String[0]);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, arrayString);
			spinClassAtt.setAdapter(adapter);
			spinClassAtt.setSelection(spinClassAtt.getCount());
		}
		
	}

	
	
	//-----------------------------------------------------------------------------------------------------
	// [2] ------------------------------------------------------------------------------------------------
	public static class SelectFragment extends Fragment {
		public SelectFragment() {
		}

//		private OnSelectionUpdateListener mCallback;
//
//		public interface OnSelectionUpdateListener {
//			void changeInterface(int intType);
//		}
//
//		@Override
//		public void onAttach(Context context) {
//			super.onAttach(context);
//			if (context instanceof OnSelectionUpdateListener) {
//				mCallback = (OnSelectionUpdateListener) context;
//			}
//
//		}
//
//		@Override
//		public void onDetach() {
//			super.onDetach();
//			mCallback = null;
//		}
		
		
		View rootView;
		TextView textView, tvClassifier;
		
		Spinner spinBayes, spinRules, spinTrees, spinMeta, spinLazy, spinFunctions, spinCluster, spinAssociate;
		Integer idBayes, idRules, idTrees, idMeta, idLazy, idFunctions, idCluster, idAssociate;

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			
			
			rootView = inflater.inflate(R.layout.fragment_main_tab2, container, false);
			textView = rootView.findViewById(R.id.section_label2);
			tvClassifier = rootView.findViewById(R.id.tvClass);
			spinBayes = rootView.findViewById(R.id.spinner1);
			spinRules = rootView.findViewById(R.id.spinner2);
			spinTrees = rootView.findViewById(R.id.spinner3);
			spinMeta = rootView.findViewById(R.id.spinner4);
			spinLazy = rootView.findViewById(R.id.spinner5);
			spinFunctions = rootView.findViewById(R.id.spinner6);
			spinCluster = rootView.findViewById(R.id.spinner);
			spinAssociate = rootView.findViewById(R.id.spinner7);
			idBayes = spinBayes.getId();
			idRules = spinRules.getId();
			idTrees = spinTrees.getId();
			idMeta = spinMeta.getId();
			idLazy = spinLazy.getId();
			idFunctions = spinFunctions.getId();
			idCluster = spinCluster.getId();
			idAssociate = spinAssociate.getId();

			AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
					if (adapterView.getId() == idBayes) {
						resetSpinner(spinRules);
						resetSpinner(spinMeta);
						resetSpinner(spinTrees);
						resetSpinner(spinLazy);
						resetSpinner(spinFunctions);
						resetSpinner(spinCluster);
						resetSpinner(spinAssociate);
						alType = 1;
					} else if (adapterView.getId() == idRules) {
						resetSpinner(spinBayes);
						resetSpinner(spinMeta);
						resetSpinner(spinTrees);
						resetSpinner(spinLazy);
						resetSpinner(spinFunctions);
						resetSpinner(spinCluster);
						resetSpinner(spinAssociate);
						alType = 1;
					} else if (adapterView.getId() == idTrees) {
						resetSpinner(spinRules);
						resetSpinner(spinMeta);
						resetSpinner(spinBayes);
						resetSpinner(spinLazy);
						resetSpinner(spinFunctions);
						resetSpinner(spinCluster);
						resetSpinner(spinAssociate);
						alType = 1;
					} else if (adapterView.getId() == idMeta) {
						resetSpinner(spinRules);
						resetSpinner(spinBayes);
						resetSpinner(spinTrees);
						resetSpinner(spinLazy);
						resetSpinner(spinFunctions);
						resetSpinner(spinCluster);
						resetSpinner(spinAssociate);
						alType = 1;
					} else if (adapterView.getId() == idLazy) {
						resetSpinner(spinRules);
						resetSpinner(spinMeta);
						resetSpinner(spinTrees);
						resetSpinner(spinBayes);
						resetSpinner(spinFunctions);
						resetSpinner(spinCluster);
						resetSpinner(spinAssociate);
						alType = 1;
					} else if (adapterView.getId() == idFunctions) {
						resetSpinner(spinLazy);
						resetSpinner(spinRules);
						resetSpinner(spinMeta);
						resetSpinner(spinTrees);
						resetSpinner(spinBayes);
						resetSpinner(spinCluster);
						resetSpinner(spinAssociate);
						alType = 1;
					} else if (adapterView.getId() == idCluster) {
						resetSpinner(spinLazy);
						resetSpinner(spinRules);
						resetSpinner(spinMeta);
						resetSpinner(spinTrees);
						resetSpinner(spinFunctions);
						resetSpinner(spinBayes);
						resetSpinner(spinAssociate);
						alType = 2;
					} else if (adapterView.getId() == idAssociate) {
						resetSpinner(spinLazy);
						resetSpinner(spinRules);
						resetSpinner(spinMeta);
						resetSpinner(spinTrees);
						resetSpinner(spinFunctions);
						resetSpinner(spinBayes);
						resetSpinner(spinCluster);
						alType = 2;
					}
//---------------------------------------------------------------------------------------------------------------
					RunFragment.changeFragment();
					btnCM.setEnabled(false);
//---------------------------------------------------------------------------------------------------------------
					tvClassifier.setText(adapterView.getItemAtPosition(pos).toString());
					nameClassifier = adapterView.getItemAtPosition(pos).toString();
					if (nameClassifier.trim().equals("Rotation Forest")) {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setMessage("Rotation Forest is accurate, but can be slow on mobile devices and anything but very small datasets. " +
								"Stopping it mid-process may take extra time.");
						showDialog(alertDialog);
					} else if (nameClassifier.trim().equals("ZeroR")) {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setMessage("ZeroR isn't really a classifier - it simply selects the majority class value. It's great for getting " +
								"a 'ground-level' accuracy score, but nothing more.");
						showDialog(alertDialog);
					} else if (nameClassifier.trim().equals("MultilayerPerceptron")) {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setMessage("MultilayerPerceptron is also known as a 'feed-forward artificial neural network' and as close to 'deep learning' " +
								"as we can do in DataLearner. But be warned - it'll likely be very slow even with smaller datasets and high-speed phones.");
						showDialog(alertDialog);
					}
				}
				
				public void showDialog(AlertDialog alertDialog) {
					alertDialog.setTitle("Warning");
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Got it.", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					alertDialog.setIcon(R.mipmap.ic_launcher);
					alertDialog.show();
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {

				}
				
				void resetSpinner(Spinner stf) {
					stf.setOnItemSelectedListener(null);
					stf.setSelection(0, false);
					stf.setOnItemSelectedListener(this);
				}
				


			};
			

			spinBayes.setOnItemSelectedListener(spinListener);
			spinRules.setOnItemSelectedListener(spinListener);
			spinTrees.setOnItemSelectedListener(spinListener);
			spinMeta.setOnItemSelectedListener(spinListener);
			spinLazy.setOnItemSelectedListener(spinListener);
			spinFunctions.setOnItemSelectedListener(spinListener);
			spinCluster.setOnItemSelectedListener(spinListener);
			spinAssociate.setOnItemSelectedListener(spinListener);

			return rootView;
		}
		
		
	}

	//-----------------------------------------------------------------------------------------------------
	// [3] ------------------------------------------------------------------------------------------------
	public static class RunFragment extends Fragment {

		public RunFragment() {
		}
		
		//		TextView cci, ici, kappa, mae, rmse, rae, rrse, tni, tvStatus, tvsl3;
		TextView tvsl3;
		Button btnRun;
		CheckBox checkBox;

//		public void setInterface(int uitype) {
//			changeFragment();
//		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_main_tab3, container, false);
			tvStatus = rootView.findViewById(R.id.tvStatus);
			checkBox = rootView.findViewById(R.id.checkBox);
			btnRun = rootView.findViewById(R.id.btnRun);
			btnCM = rootView.findViewById(R.id.btnCM);

			cci = rootView.findViewById(R.id.tvCCI);
			ici = rootView.findViewById(R.id.tvICI);
			kappa = rootView.findViewById(R.id.tvKappa);
			mae = rootView.findViewById(R.id.tvMAE);
			rmse = rootView.findViewById(R.id.tvRMSE);
			rae = rootView.findViewById(R.id.tvRAE);
			rrse = rootView.findViewById(R.id.tvRRSE);
			tni = rootView.findViewById(R.id.tvTNI);
			tvsl3 = rootView.findViewById(R.id.section_label3);
			tvStatus.setMovementMethod(new ScrollingMovementMethod());
			tvStatus.setHorizontallyScrolling(true);
			
			btnRun.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
				if (uriDataset != null && !nameClassifier.equals("-- none selected --")) {
					if (thread == null || !isThreadRunning) {
						launchTask(checkBox);
						System.out.println("Pressed for START");
						btnRun.setText("Stop");
						tvsl3.setText("Tap 'Stop' to stop process:");
						killThread = false;
					} else if (isThreadRunning) {
						System.out.println("Pressed for STOP");
						statusUpdateStore += "\r\n[" + nameClassifier + "] Stopping - please wait.";
						tvStatus.append("\r\n[" + nameClassifier + "] Stopping - please wait.");
						thread.interrupt();
						killThread = true;
					}

				} else {
					AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
					alertDialog.setTitle("You're missing something...");
					alertDialog.setIcon(R.mipmap.ic_launcher);
					alertDialog.setMessage("Please select a training set file and an algorithm before you try to model something.");
					alertDialog.show();
				}
				}
			});

			btnCM.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(getContext(), ConfusionMatrixActivity.class));
				}

			});


			return rootView;
		}
		
		static void changeFragment() {
			
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvStatus.getLayoutParams();
			if (alType == 2) {
				params.topMargin = 3;
				params.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
				params.topToBottom = R.id.btnRun;
//						params.topToTop = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
				params.bottomToBottom = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
				params.bottomMargin = 16;
			} else if (alType == 1) {
				params.topMargin = 16;
				params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
				params.bottomToTop = R.id.btnCM;
				params.topToBottom = R.id.textView14;
				params.bottomMargin = 8;
			}
			tvStatus.setLayoutParams(params);
			cleanDisplay();
			
		}
		
		void launchTask(CheckBox checkBox) {
			btnCM.setEnabled(false);
			cleanDisplay();
			if (checkBox.isChecked()) validate = 1;
			else validate = 0;
//			DataAnalysis task = new DataAnalysis(getContext());
//			DataAnalysis task = new DataAnalysis(getContext(), tvStatus, tvsl3, btnRun, btnCM, nameClassifier, validate, data,
//					cci, ici, kappa, mae, rmse, rae, rrse, tni);
			DataAnalysis task = new DataAnalysis(getContext(), nameClassifier, validate, data);
			threadGroup = new ThreadGroup("null");
			thread = new Thread(threadGroup, task, "dataRunnable", 64000);
			thread.start();
			isThreadRunning = true;
		}
		
		
		static void cleanDisplay() {
			tvStatus.setText("Ready.");
			cci.setText("---");
			ici.setText("---");
			kappa.setText("---");
			mae.setText("---");
			rmse.setText("---");
			rae.setText("---");
			rrse.setText("---");
			tni.setText("---");
		}

	}
	//------------------------------------------------------------------------------------------------

	
	
	//------------------------------------------------------------------------------------------------

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	class SectionsPagerAdapter extends FragmentStatePagerAdapter {
		
		
		SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return fragL;
//					return new LoadFragment();
				case 1:
					return fragS;
//					return new SelectFragment();
				case 2:
					return fragR;
//					return new RunFragment();
			}
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class below).
			//return PlaceholderFragment.newInstance(position + 1);
			return null;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

//        @Override
//		public int getItemPosition(Object object) {
//			if (alType == 1) return 1;
//        	else return PagerAdapter.POSITION_NONE;
//		}

//		@Override
//		public Object instantiateItem(ViewGroup container, int position) {
//
//		}
	}
}
