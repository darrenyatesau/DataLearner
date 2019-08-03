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
	DataAnalysis.java
    (C) Copyright Darren Yates 2018-2019
	Developed using a combination of Weka 3.6.15 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.6.15 is licensed GPLv2.0, source code is available on GitHub
 */

package au.com.darrenyates.datalearner;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Random;

import weka.associations.AprioriTS;
import weka.associations.FPGrowthTS;
import weka.associations.FilteredAssociatorTS;
import weka.classifiers.EvaluationTS;
import weka.classifiers.bayes.BayesNetTS;
import weka.classifiers.bayes.NaiveBayesTS;
import weka.classifiers.functions.LogisticTS;
import weka.classifiers.functions.SimpleLogisticTS;
import weka.classifiers.functions.MultilayerPerceptronTS;
import weka.classifiers.lazy.IBkTS;
import weka.classifiers.lazy.KStarTS;
import weka.classifiers.meta.AdaBoostM1TS;
import weka.classifiers.meta.BaggingTS;
import weka.classifiers.meta.LogitBoostTS;
import weka.classifiers.meta.MultiBoostABTS;
import weka.classifiers.meta.RandomCommitteeTS;
import weka.classifiers.meta.RotationForestTS;
import weka.classifiers.rules.ConjunctiveRuleTS;
import weka.classifiers.rules.DTNBTS;
import weka.classifiers.rules.DecisionTableTS;
import weka.classifiers.rules.JRipTS;
import weka.classifiers.rules.OneRTS;
import weka.classifiers.rules.PARTTS;
import weka.classifiers.rules.RidorTS;
import weka.classifiers.rules.ZeroRTS;
import weka.classifiers.trees.BFTreeTS;
import weka.classifiers.trees.DecisionStumpTS;
import weka.classifiers.trees.J48TS;
import weka.classifiers.trees.LADTreeTS;
import weka.classifiers.trees.ADTreeTS;
import weka.classifiers.trees.REPTreeTS;
import weka.classifiers.trees.RandomForestTS;
import weka.classifiers.trees.RandomTreeTS;
import weka.classifiers.trees.SimpleCartTS;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.DBSCANTS;
import weka.clusterers.EMTS;
import weka.classifiers.trees.SysFor;
import weka.classifiers.trees.ForestPA;
import weka.classifiers.trees.SPAARC;
import weka.clusterers.FarthestFirstTS;
import weka.clusterers.FilteredClustererTS;
import weka.clusterers.SimpleKMeansTS;
import weka.classifiers.meta.RandomSubSpaceTS;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import static au.com.darrenyates.datalearner.MainActivity.alType;
import static au.com.darrenyates.datalearner.MainActivity.killThread;
import static au.com.darrenyates.datalearner.MainActivity.isThreadRunning;
import static au.com.darrenyates.datalearner.MainActivity.statusUpdateStore;

class DataAnalysis implements Runnable {
	
	private Context context;
	private TextView tv;
	private TextView tvsl3;
	private TextView cci;
	private TextView ici;
	private TextView kappa;
	private TextView mae;
	private TextView rmse;
	private TextView rae;
	private TextView rrse;
	private TextView tni;
	private Button btnRun;
	private Button btnCM;
	private String algorithm;
	private int validate;
	private Instances data;
	private boolean isRunning;
	private long timeBuildStart;
	private long timeBuildEnd;
	private long timeEvalStart;
	private long timeEvalEnd;
	private long timeBuild;
	private long timeEval;
	static EvaluationTS returnEval;
	static String classifierTree;
	private Handler handler = new Handler();
	private Instances clusterdata;
	private int dotCount = 0;
	
	DataAnalysis(Context context, String algorithm, int validate, Instances dataset) {
		
		this.context = context;
		this.tv = ((Activity) context).findViewById(R.id.tvStatus);
		this.tvsl3 = ((Activity) context).findViewById(R.id.section_label3);
		this.btnRun = ((Activity) context).findViewById(R.id.btnRun);
		this.btnCM = ((Activity) context).findViewById(R.id.btnCM);
		this.algorithm = algorithm;
		this.validate = validate;
		this.data = dataset;
		this.cci = ((Activity) context).findViewById(R.id.tvCCI);
		this.ici = ((Activity) context).findViewById(R.id.tvICI);
		this.kappa = ((Activity) context).findViewById(R.id.tvKappa);
		this.mae = ((Activity) context).findViewById(R.id.tvMAE);
		this.rmse = ((Activity) context).findViewById(R.id.tvRMSE);
		this.rae = ((Activity) context).findViewById(R.id.tvRAE);
		this.rrse = ((Activity) context).findViewById(R.id.tvRRSE);
		this.tni = ((Activity) context).findViewById(R.id.tvTNI);
		
	}
	
	class Classifiers {
		
		BayesNetTS bayesNet = new BayesNetTS();
		NaiveBayesTS naiveBayes = new NaiveBayesTS();
		J48TS j48 = new J48TS();
		SimpleCartTS simpleCart = new SimpleCartTS();
		OneRTS oner = new OneRTS();
		ZeroRTS zeror = new ZeroRTS();
		RandomTreeTS randomTree = new RandomTreeTS();
		RandomForestTS randomForest = new RandomForestTS();
		REPTreeTS reptree = new REPTreeTS();
		BFTreeTS bftree = new BFTreeTS();
		DecisionStumpTS decisionStump = new DecisionStumpTS();
		JRipTS jrip = new JRipTS();
		PARTTS part = new PARTTS();
		RidorTS ridor = new RidorTS();
		ConjunctiveRuleTS conjunctiveRule = new ConjunctiveRuleTS();
		DecisionTableTS decisionTable = new DecisionTableTS();
		AdaBoostM1TS adaBoostM1 = new AdaBoostM1TS();
		BaggingTS bagging = new BaggingTS();
		LogitBoostTS logitBoost = new LogitBoostTS();
		RandomCommitteeTS randomCommittee = new RandomCommitteeTS();
		IBkTS ibk = new IBkTS();
		LADTreeTS ladtree = new LADTreeTS();
		MultiBoostABTS multiBoostAB = new MultiBoostABTS();
		RotationForestTS rotationForest = new RotationForestTS();
		DTNBTS dtnb = new DTNBTS();
		LogisticTS logistic = new LogisticTS();
		SimpleLogisticTS simpleLogistic = new SimpleLogisticTS();
		KStarTS kstar = new KStarTS();
		SysFor sysFor = new SysFor();
		ForestPA forestPA = new ForestPA();
		SPAARC spaarc = new SPAARC();
		ADTreeTS adtree = new ADTreeTS();
		RandomSubSpaceTS randomSubSpace = new RandomSubSpaceTS();
		
		SimpleKMeansTS simpleK = new SimpleKMeansTS();
		EMTS em = new EMTS();
		DBSCANTS dbScan = new DBSCANTS();
		FarthestFirstTS farthestFirst = new FarthestFirstTS();
		FilteredClustererTS filteredClusterer = new FilteredClustererTS();
		
		AprioriTS apriori = new AprioriTS();
		FPGrowthTS fpGrowth = new FPGrowthTS();
		FilteredAssociatorTS filteredAssociator = new FilteredAssociatorTS();
		MultilayerPerceptronTS mlp = new MultilayerPerceptronTS();
		
		Classifiers() {
		}
		
	}
	
	@Override
	public void run() {
		
		runAlgorithm();
		
	}
	
	private Runnable progressRun = new Runnable() {
		@Override
		public void run() {
			statusUpdateStore += ".";
			dotCount++;
			if (dotCount == 1 || dotCount % 35 == 0) statusUpdate("\r\n");
			statusUpdate(".");
			handler.postDelayed(this, 2000);
		}
	};
	
	private void runAlgorithm() {
		Classifiers cl = new Classifiers();
		
		try {
			isRunning = true;
//            Instances data = getData();
			System.out.println(data.toSummaryString());
			System.out.println("ALGORITHM TO BUILD: " + algorithm);
			statusUpdate("\r\n[" + algorithm + "] model build started.");
			dotCount = 0;
			handler.post(progressRun);
			timeBuildStart = System.nanoTime();
			if (algorithm.equals("BayesNet")) cl.bayesNet.buildClassifier(data);
			else if (algorithm.equals("NaiveBayes")) cl.naiveBayes.buildClassifier(data);
			else if (algorithm.equals("Conjunctive Rule")) cl.conjunctiveRule.buildClassifier(data);
			else if (algorithm.equals("Decision Table")) cl.decisionTable.buildClassifier(data);
			else if (algorithm.equals("JRip")) cl.jrip.buildClassifier(data);
			else if (algorithm.equals("OneR")) cl.oner.buildClassifier(data);
			else if (algorithm.equals("PART")) cl.part.buildClassifier(data);
			else if (algorithm.equals("Ridor")) cl.ridor.buildClassifier(data);
			else if (algorithm.equals("ZeroR")) cl.zeror.buildClassifier(data);
			else if (algorithm.equals("BFTree")) cl.bftree.buildClassifier(data);
			else if (algorithm.equals("Decision Stump")) cl.decisionStump.buildClassifier(data);
			else if (algorithm.equals("J48 (C4.5)")) cl.j48.buildClassifier(data);
			else if (algorithm.equals("RandomForest")) cl.randomForest.buildClassifier(data);
			else if (algorithm.equals("RandomTree")) cl.randomTree.buildClassifier(data);
			else if (algorithm.equals("REPTree")) cl.reptree.buildClassifier(data);
			else if (algorithm.equals("SimpleCART")) cl.simpleCart.buildClassifier(data);
			else if (algorithm.equals("AdaBoostM1")) cl.adaBoostM1.buildClassifier(data);
			else if (algorithm.equals("Bagging")) cl.bagging.buildClassifier(data);
			else if (algorithm.equals("LogitBoost")) cl.logitBoost.buildClassifier(data);
			else if (algorithm.equals("Random Committee")) cl.randomCommittee.buildClassifier(data);
			else if (algorithm.equals("IBk (KNN)")) cl.ibk.buildClassifier(data);
			else if (algorithm.equals("ADTree")) cl.adtree.buildClassifier(data);
			else if (algorithm.equals("LADTree")) cl.ladtree.buildClassifier(data);
			else if (algorithm.equals("MultiBoostAB")) cl.multiBoostAB.buildClassifier(data);
			else if (algorithm.equals("Rotation Forest")) cl.rotationForest.buildClassifier(data);
			else if (algorithm.equals("DTNB")) cl.dtnb.buildClassifier(data);
			else if (algorithm.equals("Logistic")) cl.logistic.buildClassifier(data);
			else if (algorithm.equals("SimpleLogistic")) cl.simpleLogistic.buildClassifier(data);
			else if (algorithm.equals("KStar")) cl.kstar.buildClassifier(data);
			else if (algorithm.equals("SysFor")) cl.sysFor.buildClassifier(data);
			else if (algorithm.equals("ForestPA")) cl.forestPA.buildClassifier(data);
			else if (algorithm.equals("SPAARC")) cl.spaarc.buildClassifier(data);
			else if (algorithm.equals("MultilayerPerceptron")) cl.mlp.buildClassifier(data);
			else if (algorithm.equals("RandomSubSpace")) cl.randomSubSpace.buildClassifier(data);
			else if (algorithm.equals("SimpleKMeans")) {
				clusterdata = removeClass(data);
				cl.simpleK.buildClusterer(new Instances(clusterdata));
			} else if (algorithm.equals("EM")) {
				clusterdata = removeClass(data);
				cl.em.buildClusterer(new Instances(clusterdata));
			} else if (algorithm.equals("DBSCAN")) {
				clusterdata = removeClass(data);
				cl.dbScan.buildClusterer(new Instances(clusterdata));
			} else if (algorithm.equals("FarthestFirst")) {
				clusterdata = removeClass(data);
				cl.farthestFirst.buildClusterer(new Instances(clusterdata));
			} else if (algorithm.equals("FilteredClusterer")) {
				clusterdata = removeClass(data);
				cl.filteredClusterer.buildClusterer(new Instances(clusterdata));
			} else if (algorithm.equals("Apriori")) {
				cl.apriori.buildAssociations(data);
				statusUpdate(cl.apriori.toString());
			} else if (algorithm.equals("FilteredAssociator")) {
				cl.filteredAssociator.buildAssociations(data);
				statusUpdate(cl.filteredAssociator.toString());
			} else if (algorithm.equals("FPGrowth")) {
				cl.fpGrowth.buildAssociations(data);
				statusUpdate(cl.fpGrowth.toString());
			}
			
			timeBuildEnd = System.nanoTime();
			timeBuild = timeBuildEnd - timeBuildStart;
			handler.removeCallbacks(progressRun);
			if (killThread == false) {
				statusUpdateStore += "\r\n[" + algorithm + "] model build complete.\r\n";
				statusUpdate("\r\n[" + algorithm + "] model build complete.\r\n");
			} else {
				statusUpdateStore += "\r\n[" + algorithm + "] model build stopped.\r\n";
				statusUpdate("\r\n[" + algorithm + "] model build stopped.\r\n");
				restoreCode();
			}
			classifierTree = "";
			if (algorithm.equals("BayesNet")) classifierTree = cl.bayesNet.toString();
			else if (algorithm.equals("NaiveBayes")) classifierTree = cl.naiveBayes.toString();
			else if (algorithm.equals("Conjunctive Rule"))
				classifierTree = cl.conjunctiveRule.toString();
			else if (algorithm.equals("Decision Table"))
				classifierTree = cl.decisionTable.toString();
			else if (algorithm.equals("JRip")) classifierTree = cl.jrip.toString();
			else if (algorithm.equals("OneR")) classifierTree = cl.oner.toString();
			else if (algorithm.equals("PART")) classifierTree = cl.part.toString();
			else if (algorithm.equals("Ridor")) classifierTree = cl.ridor.toString();
			else if (algorithm.equals("ZeroR")) classifierTree = cl.zeror.toString();
			else if (algorithm.equals("BFTree")) classifierTree = cl.bftree.toString();
			else if (algorithm.equals("Decision Stump"))
				classifierTree = cl.decisionStump.toString();
			else if (algorithm.equals("J48 (C4.5)")) classifierTree = cl.j48.toString();
			else if (algorithm.equals("RandomForest")) classifierTree = cl.randomForest.toString();
			else if (algorithm.equals("RandomTree")) classifierTree = cl.randomTree.toString();
			else if (algorithm.equals("REPTree")) classifierTree = cl.reptree.toString();
			else if (algorithm.equals("SimpleCART")) classifierTree = cl.simpleCart.toString();
			else if (algorithm.equals("AdaBoostM1")) classifierTree = cl.adaBoostM1.toString();
			else if (algorithm.equals("Bagging")) classifierTree = cl.bagging.toString();
			else if (algorithm.equals("LogitBoost")) classifierTree = cl.logitBoost.toString();
			else if (algorithm.equals("Random Committee"))
				classifierTree = cl.randomCommittee.toString();
			else if (algorithm.equals("IBk (KNN)")) classifierTree = cl.ibk.toString();
			else if (algorithm.equals("LADTree")) classifierTree = cl.ladtree.toString();
			else if (algorithm.equals("ADTree")) classifierTree = cl.adtree.toString();
			else if (algorithm.equals("MultiBoostAB")) classifierTree = cl.multiBoostAB.toString();
			else if (algorithm.equals("Rotation Forest"))
				classifierTree = cl.rotationForest.toString();
			else if (algorithm.equals("DTNB")) classifierTree = cl.dtnb.toString();
			else if (algorithm.equals("Logistic")) classifierTree = cl.logistic.toString();
			else if (algorithm.equals("SimpleLogistic"))
				classifierTree = cl.simpleLogistic.toString();
			else if (algorithm.equals("KStar")) classifierTree = cl.kstar.toString();
			else if (algorithm.equals("SysFor")) classifierTree = cl.sysFor.toString();
			else if (algorithm.equals("ForestPA")) classifierTree = cl.forestPA.toString();
			else if (algorithm.equals("SPAARC")) classifierTree = cl.spaarc.toString();
			else if (algorithm.equals("MultilayerPerceptron")) classifierTree = cl.mlp.toString();
			else if (algorithm.equals("RandomSubSpace"))
				classifierTree = cl.randomSubSpace.toString();
			
			if (validate == 1 && killThread == false) runEvaluation(algorithm, cl, data);
			else restoreSettings();
			
		} catch (Exception e) {
			if (killThread == true) {
				statusUpdateStore += "\r\n[" + algorithm + "] model build stopped.\r\nReady.";
				statusUpdate("\r\n[" + algorithm + "] model build stopped.\r\nReady.");
			} else {
				statusUpdate("\r\n=== ERROR: This dataset is not supported.\r\nReady.");
			}
			restoreCode();
			System.out.print("ERROR: " + Log.getStackTraceString(e));
//			statusUpdate("=== ERROR: "+ Log.getStackTraceString(e));
		}
	}
	
	private void restoreCode() {
		resetRunBtn();
		handler.removeCallbacks(progressRun);
		isRunning = false;
		isThreadRunning = false;
	}
	
	private Instances removeClass(Instances inst) {
		Remove af = new Remove();
		Instances retI = null;
		
		try {
			if (inst.classIndex() < 0) {
				retI = inst;
			} else {
				af.setAttributeIndices("" + (inst.classIndex() + 1));
				af.setInvertSelection(false);
				af.setInputFormat(inst);
				retI = Filter.useFilter(inst, af);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retI;
	}
	
	
	private void runEvaluation(String algorithm, Classifiers cl, Instances data) {
//        int correct, incorrect, totalInst;
//		if (killThread == false) {
		try {
			EvaluationTS eval = new EvaluationTS(data);
			ClusterEvaluation ceval = new ClusterEvaluation();
			System.out.println("ORIG ALGORITHM: " + algorithm);
			statusUpdate("\r\n[" + algorithm + "] model evaluation started.");
			dotCount = 0;
			handler.post(progressRun);
			switch (algorithm) {
				case "BayesNet":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.bayesNet, data, 10, new Random(1));
					break;
				case "NaiveBayes":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.naiveBayes, data, 10, new Random(1));
					break;
				case "Conjunctive Rule":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.conjunctiveRule, data, 10, new Random(1));
					break;
				case "Decision Table":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.decisionTable, data, 10, new Random(1));
					break;
				case "JRip":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.jrip, data, 10, new Random(1));
					break;
				case "OneR":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.oner, data, 10, new Random(1));
					break;
				case "PART":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.part, data, 10, new Random(1));
					break;
				case "Ridor":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.ridor, data, 10, new Random(1));
					break;
				case "ZeroR":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.zeror, data, 10, new Random(1));
					break;
				case "BFTree":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.bftree, data, 10, new Random(1));
					break;
				case "Decision Stump":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.decisionStump, data, 10, new Random(1));
					break;
				case "J48 (C4.5)":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.j48, data, 10, new Random(1));
					break;
				case "RandomForest":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.randomForest, data, 10, new Random(1));
					break;
				case "RandomTree":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.randomTree, data, 10, new Random(1));
					break;
				case "REPTree":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.reptree, data, 10, new Random(1));
					break;
				case "SimpleCART":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.simpleCart, data, 10, new Random(1));
					break;
				case "AdaBoostM1":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.adaBoostM1, data, 10, new Random(1));
					break;
				case "Bagging":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.bagging, data, 10, new Random(1));
					break;
				case "LogitBoost":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.logitBoost, data, 10, new Random(1));
					break;
				case "Random Committee":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.randomCommittee, data, 10, new Random(1));
					break;
				case "IBk (KNN)":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.ibk, data, 10, new Random(1));
					break;
				case "ADTree":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.adtree, data, 10, new Random(1));
					break;
				case "LADTree":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.ladtree, data, 10, new Random(1));
					break;
				case "MultiBoostAB":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.multiBoostAB, data, 10, new Random(1));
					break;
				case "Rotation Forest":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.rotationForest, data, 10, new Random(1));
					break;
				case "DTNB":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.dtnb, data, 10, new Random(1));
					break;
				case "Logistic":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.logistic, data, 10, new Random(1));
					break;
				case "SimpleLogistic":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.simpleLogistic, data, 10, new Random(1));
					break;
				case "KStar":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.kstar, data, 10, new Random(1));
					break;
				case "SysFor":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.sysFor, data, 10, new Random(1));
					break;
				case "ForestPA":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.forestPA, data, 10, new Random(1));
					break;
				case "SPAARC":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.spaarc, data, 10, new Random(1));
					break;
				case "MultilayerPerceptron":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.mlp, data, 10, new Random(1));
					break;
				case "RandomSubSpace":
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(cl.randomSubSpace, data, 10, new Random(1));
					break;
				case "SimpleKMeans":
					timeEvalStart = System.nanoTime();
					ceval.setClusterer(cl.simpleK);
					ceval.evaluateClusterer(clusterdata);
					statusUpdate("\r\n" + ceval.clusterResultsToString());
					break;
				case "EM":
					timeEvalStart = System.nanoTime();
					ceval.setClusterer(cl.em);
					ceval.evaluateClusterer(clusterdata);
					statusUpdate("\r\n" + ceval.clusterResultsToString());
					break;
				case "DBSCAN":
					timeEvalStart = System.nanoTime();
					ceval.setClusterer(cl.dbScan);
					ceval.evaluateClusterer(clusterdata);
					statusUpdate("\r\n" + ceval.clusterResultsToString());
					break;
				case "FarthestFirst":
					timeEvalStart = System.nanoTime();
					ceval.setClusterer(cl.farthestFirst);
					ceval.evaluateClusterer(clusterdata);
					statusUpdate("\r\n" + ceval.clusterResultsToString());
					break;
				case "FilteredClusterer":
					timeEvalStart = System.nanoTime();
					ceval.setClusterer(cl.filteredClusterer);
					ceval.evaluateClusterer(clusterdata);
					statusUpdate("\r\n" + ceval.clusterResultsToString());
					break;
				
				
			}
			timeEvalEnd = System.nanoTime();
			timeEval = timeEvalEnd - timeEvalStart;
			handler.removeCallbacks(progressRun);
			isRunning = false;
			if (killThread == false) {
				if (alType == 1) {
					enableBtnCM();
					DecimalFormat df = new DecimalFormat("#.####");
					updateResults(cci, (int) eval.correct() + " (" + df.format(eval.pctCorrect()) + "%)");
					updateResults(ici, (int) eval.incorrect() + " (" + df.format(eval.pctIncorrect()) + "%)");
					updateResults(kappa, "" + df.format(eval.kappa()));
					updateResults(mae, df.format(eval.meanAbsoluteError()));
					updateResults(rmse, df.format(eval.rootMeanSquaredError()));
					updateResults(rae, df.format(eval.relativeAbsoluteError()) + "%");
					updateResults(rrse, df.format(eval.rootRelativeSquaredError()) + "%");
					updateResults(tni, "" + (int) eval.numInstances());
					//					if (!algorithm.equals("Apriori") && !algorithm.equals("FPGrowth")) {
					//						restoreSettings();
					//					}
				}
				statusUpdateStore += "\r\n[" + algorithm + "] model evaluation complete.";
				statusUpdate("\r\n[" + algorithm + "] model evaluation complete.");
				restoreSettings();
			} else {
				statusUpdateStore += "\r\n[" + algorithm + "] evaluation stopped.\r\nReady.";
				statusUpdate("\r\n[" + algorithm + "] evaluation stopped.\r\nReady.");
			}
			resetRunBtn();
			isThreadRunning = false;
			returnEval = eval;
		} catch (Exception e) {
			restoreCode();
			System.out.print("ERROR: " + Log.getStackTraceString(e));
//            statusUpdate("=== ERROR: "+ Log.getStackTraceString(e));
			if (killThread == false) {
				statusUpdate("=== ERROR: this dataset is not supported.");
			} else {
				statusUpdate("\r\n[" + algorithm + "] evaluation stopped.\r\nReady.");
			}
		}
	}
//	}
	
	private void restoreSettings() {
		if (killThread == false) {
			DecimalFormat df2 = new DecimalFormat("#.#####");
			statusUpdateStore += "\r\n[" + algorithm + "] build: " + (df2.format(timeBuild / 1000000000.0) + "s");
			statusUpdate("\r\n[" + algorithm + "] build: " + (df2.format(timeBuild / 1000000000.0)) + "s");
			if (validate == 1) {
				statusUpdateStore += "\r\n[" + algorithm + "]  eval: " + (df2.format(timeEval / 1000000000.0) + "s");
				statusUpdate("\r\n[" + algorithm + "]  eval: " + (df2.format(timeEval / 1000000000.0)) + "s");
			}
		}
		statusUpdateStore += "\r\nReady.";
		statusUpdate("\r\nReady.");
		resetRunBtn();
		handler.removeCallbacks(progressRun);
		isRunning = false;
		isThreadRunning = false;
		System.out.println("Ready.");
	}

//    public Instances getData() {
//        Instances data = null;
//        try {
//            InputStream inputStream = context.getContentResolver().openInputStream(dataset);
//            ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
//            System.out.println("PATH: "+dataset.getPath());
//            data = dataSource.getDataSet();
//            data.setClassIndex(data.numAttributes() - 1);
//            inputStream.close();
//        } catch (Exception e) {
//            statusUpdateStore += "\r\nERROR: "+e.getMessage()+"\r\n";
//            statusUpdate("\r\nERROR: "+e.getMessage()+"\r\n");
//        }
//        return data;
//    }
	
	private void statusUpdate(String status) {
		final String newStatus;
		newStatus = status;
		tv.post(new Runnable() {
			@Override
			public void run() {
//                tvStatus.append(newStatus);
				tv.append(newStatus);
				tv.setMovementMethod(new ScrollingMovementMethod());
			}
		});
	}
	
	private void updateResults(TextView tvui, String text) {
		final String displayText = text;
		final TextView tvGUI = tvui;
		tvui.post(new Runnable() {
			@Override
			public void run() {
				tvGUI.setText(displayText);
			}
		});
	}
	
	private void resetRunBtn() {
		btnRun.post(new Runnable() {
			@Override
			public void run() {
				btnRun.setText(context.getString(R.string.str_run));
				tvsl3.setText("Tap 'Run' to model your data:");
			}
		});
	}
	
	private void enableBtnCM() {
		btnCM.post(new Runnable() {
			@Override
			public void run() {
				btnCM.setEnabled(true);
			}
		});
	}
	
}
