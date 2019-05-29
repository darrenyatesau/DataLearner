/*
 * DataLearner - a data-mining app for Android
 * MainActivity.java
   @author: Darren Yates, Md Zahidul Islam, Junbin Gao, 2018-2019
 * Developed using a combination of Weka 3.6.15 and algorithms developed by Charles Sturt University
 * DataLearner is licensed GPLv3.0, source code is available on GitHub
 * Weka 3.6.15 is licensed GPLv2.0, source code is available on GitHub
 */
package au.com.darrenyates.datalearner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
//import weka.gui.GenericObjectEditor;

public class MainActivity extends AppCompatActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;
	//	TextView textView;
	private static TextView cci;
	private static TextView ici;
	private static TextView kappa;
	private static TextView mae;
	private static TextView rmse;
	private static TextView rae;
	private static TextView rrse;
	private static TextView tni;
	private static TextView tvsl3;
	private static TextView tvStats;
	private static TextView tvStatus;
	//	TextView tvStatus;
	static HorizontalScrollView idHS;
	private static Button btnRun;
	private static Button btnCM;
	private static CheckBox checkBox;
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
			builder1.setMessage("Version 0.9.20190526-2042\r\n© Darren Yates, Zahid Islam, Junbin Gao\r\nPhD program, School of Computing and Mathematics, Charles Sturt University, 2018-2019." +
					"\r\n\r\nDataLearner is a data-mining app powered by the Weka data-mining core and includes " +
					"algorithms developed by Charles Sturt University. Weka was created by the University of Waikato.");
			AlertDialog alert1 = builder1.create();
			alert1.show();
			return true;
		}
		if (id == R.id.clear) {
			tvStatus.setText("");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// [1] ------------------------------------------------------------------------------------------------

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class LoadFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
//        private final String ARG_SECTION_NUMBER = "section_number";

		private static final int READ_REQUEST_CODE = 42;
		TextView tvFile, tvIntro, tvTest;
		Spinner spinClassAtt;
		String tvFileName;

		public LoadFragment() {
		}


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_tab1, container, false);
//			TextView textView = rootView.findViewById(R.id.section_label1);
			tvStats = rootView.findViewById(R.id.tvStats);
			tvFile = rootView.findViewById(R.id.textViewFile);
			tvIntro = rootView.findViewById(R.id.tvIntro);
			tvTest = rootView.findViewById(R.id.tvTest);
			spinClassAtt = rootView.findViewById(R.id.spinClass);
//            if (tvFileName != null) tvFile.setText(tvFileName);
			Button btnLoad = rootView.findViewById(R.id.button1);
			btnLoad.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					performFileSearch();

				}
			});

			return rootView;
		}
		
		void performFileSearch() {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			startActivityForResult(intent, READ_REQUEST_CODE);
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
			String fileCut;
			if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
				uriDataset = null;
				if (resultData != null) {
					uriDataset = resultData.getData();
					System.out.println("FILE: " + uriDataset.toString());
					fileCut = uriDataset.getPath();
					int split = fileCut.lastIndexOf('/');
					fileCut = fileCut.substring(split + 1);
					split = fileCut.lastIndexOf(':');
					tvFileName = fileCut.substring(split + 1);
					tvFile.setText(tvFileName);
					tvStats.setText("");
					data = getData(fileCut);
					tvStats.append(data.toSummaryString());
					tvIntro.setText("");
					ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvIntro.getLayoutParams();
					params.topMargin = 16;
					params.height = 0;
					tvIntro.setLayoutParams(params);
					tvTest.setLayoutParams(params);
					//					tvIntro.getLayoutParams().height = 0;

				}
			} else {
				System.out.println("ERROR: Problem with file read.");
			}
		}
		
		Instances getData(String filePath) {
			Instances newdata = null;
			File file = new File(uriDataset.getPath());
			try {
				InputStream inputStream = getContext().getContentResolver().openInputStream(uriDataset);
				ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);

//				CSVLoader csvLoader = new CSVLoader();
//				csvLoader.setSource(inputStream);
//				ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(csvLoader);

				newdata = dataSource.getDataSet();
				if (newdata == null)
					System.out.println("PROBLEM------------------------------------------------->>>>>>>");
				inputStream.close();
				spinClassAtt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						data.setClassIndex(i);
					}

					@Override
					public void onNothingSelected(AdapterView<?> adapterView) {

					}
				});
				setSpinClass(newdata.numAttributes());
				spinClassAtt.setSelection(newdata.numAttributes() - 1);
			} catch (Exception e) {
				statusUpdateStore += "\r\nERROR: " + e.getMessage() + "\r\n";
				tvStats.append("\r\nERROR: " + e.getMessage() + "\r\n");
			}
			return newdata;
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


		View rootView;
		TextView textView, tvClassifier;
		
		Spinner spinBayes, spinRules, spinTrees, spinMeta, spinLazy, spinFunctions, spinCluster, spinAssociate;
		Integer idBayes, idRules, idTrees, idMeta, idLazy, idFunctions, idCluster, idAssociate;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			
			rootView = inflater.inflate(R.layout.fragment_main_tab2, container, false);
			tvStatus = rootView.findViewById(R.id.tvStatus);
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
					changeFragment();
					btnCM.setEnabled(false);
//---------------------------------------------------------------------------------------------------------------
					tvStatus.setText("Ready.");
					tvClassifier.setText(adapterView.getItemAtPosition(pos).toString());
					nameClassifier = adapterView.getItemAtPosition(pos).toString();
					if (nameClassifier.trim().equals("Rotation Forest")) {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setTitle("Warning");
						alertDialog.setMessage("Rotation Forest is accurate, but can be slow on mobile devices and anything but very small datasets. " +
								"Stopping it mid-process may take extra time.");
						alertDialog.show();
					} else if (nameClassifier.trim().equals("ZeroR")) {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setTitle("Warning");
						alertDialog.setMessage("ZeroR isn't really a classifier - it simply selects the majority class value. It's great for getting " +
								"a 'ground-level' accuracy score, but nothing more.");
						alertDialog.show();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {

				}
				
				void resetSpinner(Spinner stf) {
					stf.setOnItemSelectedListener(null);
					stf.setSelection(0, false);
					stf.setOnItemSelectedListener(this);
				}
				
				void changeFragment() {
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
					RunFragment.cleanDisplay();

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
					return new LoadFragment();
				case 1:
					return new SelectFragment();
				case 2:
					return new RunFragment();
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
