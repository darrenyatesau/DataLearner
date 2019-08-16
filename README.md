# DataLearner - Data Mining and Knowledge Discovery on Android

*** Some news... DataLearner has been accepted for presentation at ADMA 2019 (International Conference on Advanced Data Mining and Applications) and will be published in 'Lecture Notes in Artificial Intelligence' (Springer) ***

DataLearner is an open-source easy-to-use tool for data mining and knowledge discovery from your own compatible training datasets. It’s fully self-contained, requires no external storage or network connectivity – it builds machine-learning models directly on your phone or tablet.

DataLearner features classification, association and clustering algorithms from the open-source Weka (Waikato Environment for Knowledge Analysis) package, plus new algorithms developed by the Data Science Research Unit (DSRU) at Charles Sturt University. Combined, the app currently provides 40 machine-learning/data-mining algorithms, including RandomForest, C4.5 (J48) and NaiveBayes.

DataLearner collects no information – it requires access to your device storage simply to load your datasets and build your requested models.

App on Google Play: https://play.google.com/store/apps/details?id=au.com.darrenyates.datalearner

Short video tutorial on YouTube: https://youtu.be/H-7pETJZf-g

Research paper on arXiv: https://arxiv.org/abs/1906.03773

Researchers: if you use this work, please cite the research paper above. Thanks.

Algorithms include:
<br>•	Bayes – BayesNet, NaiveBayes
<br>•	Functions – Logistic, SimpleLogistic, MultilayerPerceptron
<br>•	Lazy – IBk (K Nearest Neighbours), KStar
<br>•	Meta – AdaBoostM1, Bagging, LogitBoost, MultiBoostAB, Random Committee, RandomSubSpace, RotationForest
<br>•	Rules – Conjunctive Rule, Decision Table, DTNB, JRip, OneR, PART, Ridor, ZeroR
<br>•	Trees – ADTree, BFTree, DecisionStump, ForestPA, J48 (C4.5), LADTree, Random Forest, RandomTree, REPTree, SimpleCART, SPAARC, SysFor.
<br>•	Clusterers – DBSCAN, Expectation Maximisation (EM), Farthest-First, FilteredClusterer, SimpleKMeans
<br>•	Associations – Apriori, FilteredAssociator, FPGrowth

Training datasets must conform to the Weka ARFF or standard CSV file format (CSV requires header row, class attribute is last column and will be configured as nominal).

<H3>CSV file support</H3>
To use CSV files in DataLearner, the file MUST include the following:
1. header row
2. class attribute must be the last column in the CSV table
3. class attribute will be forced to 'nominal' - that means the class values will be considered 'categorical' or 'nominal'.
(points 2 and 3 may change once we're sure that it works reliably).

<H3>Bug Report</H3>
If you wish to report a bug in DataLearner, please don't just say 'your app is rubbish - it crashed' - it might make you feel better, but it won't help us fix it.
Instead, tell us: <br>* the number of attributes and records in your dataset, <br>* the approximate file size, <br>* the algorithm you were attempting to use, and,
<br>* the device you're attempting to do this on.
<br>From that, we can get to work and see what the problem was.
While we've tested the app as thoroughly as we could, we can't claim to have found every possible way to crash an Android app.
At time of writing, there was no other app on Google Play like DataLearner, so we're discovering new things about locally-executed data-mining all the time.

<H3>Privacy Policy</H3>
Our privacy policy is pretty basic - the DataLearner app does not collect any user data. The Google Play site collects standard analytics on general app download stuff about DataLearner - but that's Google, not us.
(it's the same basic info that Google Play collects for every app you download). However, as for DataLearner itself, it doesn't collect anything.
