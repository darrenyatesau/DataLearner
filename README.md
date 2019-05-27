# DataLearner
DataLearner is an easy-to-use tool for machine-learning and knowledge discovery from your own compatible training datasets. It’s fully self-contained, requires no external storage or network connectivity – it builds machine-learning models directly on your phone or tablet. 

DataLearner features classification, association and clustering algorithms from the open-source Weka (Waikato Environment for Knowledge Analysis) package, plus new algorithms developed by the Data Science Research Unit (DSRU) at Charles Sturt University. Combined, the app provides over 30 machine-learning/data-mining algorithms, including RandomForest, C4.5 (J48) and NaiveBayes.

DataMiner collects no information – it requires access to your device storage simply to load your datasets and build your requested models.

DataMiner is open-source, with the source code for this project available at github.com/darrenyatesau/DataLearner. This application is part of the lead developer’s PhD program at Charles Sturt University, Australia.

Use of this app in research applications, please cite the research paper:
Yates, D., Islam, M.Z, Gao, J. DataMiner: A Data-Mining and Knowledge Discovery Tool for Android Smartphones and Tablets. ArXiv, 2019

Algorithms include:
•	Bayes – BayesNet, NaiveBayes
•	Functions – Logistic, SimpleLogistic
•	Lazy – IBk (K Nearest Neighbours), KStar
•	Meta – AdaBoostM1, Bagging, LogitBoost, MultiBoostAB, Random Committee, RotationForest
•	Rules – Conjunctive Rule, Decision Table, DTNB, JRip, OneR, PART, Ridor, ZeroR
•	Trees – ADTree, BFTree, DecisionStump, ForestPA, J48 (C4.5), LADTree, Random Forest, RandomTree, REPTree, SimpleCART, SPAARC, SysFor.
•	Clusterers – DBSCAN, Expectation Maximisation (EM), Farthest-First, FilteredClusterer, SimpleKMeans
•	Associations – Apriori, FilteredAssociator, FPGrowth

Training datasets must conform to the Weka’ ARFF format (we are working on a CSV dataset reader – thanks for your patience).

Any problems, please drop me a line at darrenyates at gmail dot com.
