/*  Implementation of SysFor - "a Systematically Developed Forest of Multiple 
    Decision Trees" by Md Zahidul Islam and Helen Giggins. 
    Copyright (C) <2015>  <Michael J. Siers>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. 
    
    Author contact details: 
    Name: Michael Furner
    Email: mfurner@csu.edu.au
    Location: 	School of Computing and Mathematics, Charles Sturt University,
    			Bathurst, NSW, Australia, 2795.
 */
package weka.classifiers.trees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//import weka.associations.gsp.Element;
//import weka.classifiers.AbstractClassifier;
import au.com.darrenyates.datalearner.MainActivity;
import weka.classifiers.Classifier;
import weka.classifiers.EvaluationTS;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.j48ts.C45SplitTS;
import weka.classifiers.trees.j48ts.DistributionTS;
import weka.classifiers.trees.j48ts.C45SplitTS;
import weka.classifiers.trees.j48ts.DistributionTS;
import weka.core.Attribute;
import weka.core.Capabilities;
//import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.converters.ConverterUtils;

/*
 * <!-- globalinfo-start -->
 * Implementation of the decision forest algorithm SysFor, which was published
 * in:<br>
 * <br>
 * Md Zahidul Islam and Helen Giggins: Knowledge Discovery through SysFor - a
 * Systematically Developed Forest of Multiple Decision Trees In: Ninth
 * Australasian Data Mining Conference, 195-204, 2011.<br>
 * <br>
 * This decision forest algorithm is designed to build a forest consisting of
 * high accuracy decision trees. It uses gain ratio as the selection criteria.
 * The general structure of "SysFor.java" is taken from "MetaCost.java". The
 * default parameter settings are the ones used in the SysFor paper. This
 * implementation uses the J48 implementation of C4.5. Since J48 does not
 * require a setting for minimum gain ratio, this SysFor implementation also
 * does not require it. The original SysFor paper contained some voting
 * techniques for classification that are not implemented here. Instead,
 * majority voting is used.
 *
 * <!-- globalinfo-end -->
 *
 *
 * @author Michael J. Siers and Michael Furner
 * @version $Revision: 1.0$
 */
public class SysFor extends Classifier {
    
    /*
     * For serialization.
     */
    private static final long serialVersionUID = -5891220800957072995L;
    
    /*
     * The trees that comprise the SysFor forest.
     */
    private ArrayList<Classifier> forest;
    
    /*
     * Dataset the forest is built on.
     */
    private Instances dataset;
    
    /*
     * The minimum number of records in a leaf for the C4.5 trees. (default 10)
     */
    private int minRecLeaf = 10;
    
    /*
     * The number of trees that the user has requested. In most cases, this
     * number of trees will be created. However, in rare cases, a smaller number
     * is created.
     */
    private int numberTrees = 60;
    
    /*
     * Used to control the minimum gain ratio for an attribute to be considered
     * for the set of "good attributes". (default 0.3)
     */
    private float goodness = 0.3f;
    
    /*
     * Used to control whether or not a split point may be added to the set of
     * "good attributes" if the split point's attribute is already used in the
     * set of "good attributes". The smaller this value is, the more split
     * points may be used on the same attribute. (default 0.3)
     */
    private float separation = 0.3f;
    
    /*
     * The confidence factor that will be used in the C4.5 trees. (default 0.25)
     */
    private float confidence = 0.25f;
    
    /*
     * The number of classes in dataset.
     */
    private int numClasses = -1;
    
    /*
     * The class names which are used in the string output.
     */
    private String[] classNames;
    
    /*
     * A variable that is used to store the attribute domains of the passed
     * dataset.
     */
    private double[] attDomains;
    
    /*
     * Parses a given list of options. <br>
     *
     * <!-- options-start -->
     * Valid options are: <br>
     *
     * <pre> -N &lt;numberTrees&gt;
     *  Requested number of trees in forest.
     *  (default 60)</pre>
     *
     * <pre> -L &lt;minRecLeaf&gt;
     *  The minimum number of records in a leaf. Works as in C4.5. (default 10)
     * </pre>
     *
     * <pre> -G &lt;goodness&gt;
     *  Used to control the minimum gain ratio for an attribute to be
     *  considered for the set of "good attributes". (default 0.3)</pre>
     *
     * <pre> -S &lt;separation&gt;
     *  Used to control whether or not a split point may be added
     *  to the set of "good attributes" if the split point's attribute is already
     *  used in the set of "good attributes". The smaller this value is, the more split points
     *  may be used on the same attribute. (default 0.3)</pre>
     *
     * <pre> -C &lt;confidenceFactor&gt;
     *  The confidence factor that will be used in the C4.5 trees. (default 0.25)</pre>
     *
     * <pre> -D
     *  If set, classifier is run in debug mode and
     *  may output additional info to the console</pre>
     *
     * <!-- options-end -->
     *
     * Options after -- are passed to the designated classifier.<p>
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {

        String sMinRecLeaf = Utils.getOption('L', options);
        if (sMinRecLeaf.length() != 0) {
            setMinRecLeaf(Integer.parseInt(sMinRecLeaf));
        } else {
            setMinRecLeaf(10);
        }

        String sNumberTrees = Utils.getOption('N', options);
        if (sNumberTrees.length() != 0) {
            setNumberTrees(Integer.parseInt(sNumberTrees));
        } else {
            setNumberTrees(60);
        }

        String sGoodness = Utils.getOption('G', options);
        if (sGoodness.length() != 0) {
            setGoodness(Float.parseFloat(sGoodness));
        } else {
            setGoodness(0.3f);
        }

        String sSeparation = Utils.getOption('S', options);
        if (sSeparation.length() != 0) {
            setSeparation(Float.parseFloat(sSeparation));
        } else {
            setSeparation(0.3f);
        }

        String sConfidence = Utils.getOption('C', options);
        if (sConfidence.length() != 0) {
            setConfidence(Float.parseFloat(sConfidence));
        } else {
            setConfidence(0.25f);
        }

        super.setOptions(options);
    }
    
    /*
     * Gets the current settings of the classifier.
     *
     * @return the current setting of the classifier
     */
    @Override
    public String[] getOptions() {

        Vector<String> result = new Vector<String>();

        result.add("-L");
        result.add("" + getMinRecLeaf());

        result.add("-N");
        result.add("" + getNumberTrees());

        result.add("-G");
        result.add("" + getGoodness());

        result.add("-S");
        result.add("" + getSeparation());

        result.add("-C");
        result.add("" + getConfidence());

        Collections.addAll(result, super.getOptions());

        return result.toArray(new String[result.size()]);
    }
    
    /*
     * Setter for numberTrees
     *
     * @param numberTrees value to set to
     */
    private void setNumberTrees(int numberTrees) {
        this.numberTrees = numberTrees;
    }
    
    /*
     * Getter for numberTrees
     *
     * @return number trees
     */
    private int getNumberTrees() {
        return this.numberTrees;
    }
    
    /*
     * Setter for goodness
     *
     * @param goodness value to set to
     */
    private void setGoodness(float goodness) {
        this.goodness = goodness;
    }
    
    /*
     * Getter for goodness
     *
     * @return goodness value
     */
    private float getGoodness() {
        return this.goodness;
    }
    
    /*
     * Setter for separation
     *
     * @param separation value to set to
     */
    private void setSeparation(float separation) {
        this.separation = separation;
    }
    
    /*
     * Getter for separation
     *
     * @return separation
     */
    private float getSeparation() {
        return this.separation;
    }
    
    /*
     * Setter for confidence
     *
     * @param confidence value to set to
     */
    private void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /*
     * Getter for confidence
     *
     * @return confidence
     */
    private float getConfidence() {
        return this.confidence;
    }
    
    /*
     * Setter for minRecLeaf
     *
     * @param minRecLeaf value to set to
     */
    private void setMinRecLeaf(int minRecLeaf) {
        this.minRecLeaf = minRecLeaf;
    }
    
    /*
     * Getter for minimum leaf records
     *
     * @return minRecLeaf
     */
    private int getMinRecLeaf() {
        return this.minRecLeaf;
    }
    
    /*
     * Returns tool tip for numberOfTrees
     *
     * @return Tool tip for numberOfTrees
     */
    public String numberTreesTipText() {
        return "Number of trees built in the forest";
    }
    
    /*
     * Returns tool tip for goodness
     *
     * @return Tool tip for goodness
     */
    public String goodnessTipText() {
        return "Used to control the minimum gain ratio for an attribute to be "
                + "considered for the set of \"good attributes\"";
    }
    
    /*
     * Returns tool tip for separation
     *
     * @return Tool tip for separation
     */
    public String separationTipText() {
        return "Used to control whether or not a split point may be added "
                + "to the set of \"good attributes\" if the split point's attribute is already "
                + "used in the set of \"good attributes\". The smaller this value is, the more split points "
                + "may be used on the same attribute. (default 0.3)";
    }
    
    /*
     * Returns tool tip for confidence
     *
     * @return Tool tip for confidence
     */
    public String confidenceTipText() {
        return "The confidence factor that will be used in the J48";
    }
    
    /*
     * Returns tool tip for minimumLeafRecord
     *
     * @return Tool tip for minimumLeafRecord
     */
    public String minRecLeafTipText() {
        return "The minimum number of records in a leaf for the J48";
    }
    
    /*
     * This method corresponds to Algorithm 1 in the SysFor paper.
     *
     * @param data - data with which to build the classifier
     * @throws Exception
     */
    @Override
    public void buildClassifier(Instances data) throws Exception {

        getCapabilities().testWithFail(data);
        data = new Instances(data);
        // data.deleteWithMissingClass();

        dataset = data;

        numClasses = data.numClasses();
        classNames = new String[numClasses];
        for (int i = 0; i < numClasses; i++) {
            classNames[i] = data.classAttribute().value(i);
        }

        attDomains = new double[data.numAttributes()];
        for (int i = 0; i < attDomains.length; i++) {
            attDomains[i] = calculateAttributeDomain(data, i);
        }

        // Remove the records with missing values.
        for (int i = 0; i < data.numAttributes(); i++) {
            if (i != data.classIndex()) {
                data.deleteWithMissing(i);
            }
        }

        //if this is a dataset with only the class attribute
        if (data.numAttributes() == 1) {
            forest = new ArrayList<Classifier>();
            J48 onlyClass = new J48();
            onlyClass.setConfidenceFactor(confidence);
            onlyClass.setMinNumObj(minRecLeaf);
            onlyClass.buildClassifier(data);
            forest.add(onlyClass);
            return;
        }

        // Initialize the forest, good attributes, and split points to empty array lists.
        forest = new ArrayList<Classifier>();
        ArrayList<Attribute> goodAttributes = new ArrayList<Attribute>();
        ArrayList<Double> splitPoints = new ArrayList<Double>();

        ArrayList<GoodAttribute> goodAttributeObjects = getGoodAttributes(data, goodness);
        for (int i = 0; i < goodAttributeObjects.size(); i++) {
            goodAttributes.add(goodAttributeObjects.get(i).getAttribute());
            splitPoints.add(goodAttributeObjects.get(i).getSplitPoint());
        }

        int i = 0;
        while ((forest.size() < numberTrees) && (forest.size() < goodAttributes.size())) {
            Attribute currentSplitAttribute = goodAttributes.get(i);
            double currentSplitValue = splitPoints.get(i);
            SysForTree currentTree = new SysForTree(new GoodAttribute(currentSplitAttribute, currentSplitValue));
            currentTree.buildClassifier(data);
            forest.add(currentTree);
            i++;
        }
        i = 0;
        int K = forest.size() - 1;

        while ((forest.size() < numberTrees) && (i <= K)) {
            Instances[] dataSplits = splitData(data, goodAttributeObjects.get(i));
            ArrayList<ArrayList<Attribute>> levelTwoGoodAttributes = new ArrayList<ArrayList<Attribute>>();
            ArrayList<ArrayList<Double>> levelTwoSplitPoints = new ArrayList<ArrayList<Double>>();
            ArrayList<ArrayList<GoodAttribute>> levelTwoGoodAttributeObjects = new ArrayList<ArrayList<GoodAttribute>>();

            for (int j = 0; j < dataSplits.length; j++) {
                ArrayList<Attribute> currentSplitGoodAttributes = new ArrayList<Attribute>();
                ArrayList<Double> currentSplitPoints = new ArrayList<Double>();

                ArrayList<GoodAttribute> currentGoodAttributeObjects = getGoodAttributes(dataSplits[j], goodness);
                for (int l = 0; l < currentGoodAttributeObjects.size(); l++) {
                    currentSplitGoodAttributes.add(currentGoodAttributeObjects.get(l).getAttribute());
                    currentSplitPoints.add(currentGoodAttributeObjects.get(l).getSplitPoint());
                }
                levelTwoGoodAttributeObjects.add(currentGoodAttributeObjects);

                levelTwoGoodAttributes.add(currentSplitGoodAttributes);
                levelTwoSplitPoints.add(currentSplitPoints);
            }

            // Calculate the possible number of trees.
            // Here it is broken down into numerator and denominator for readability.
            int possibleNumberTrees = 0;
            int numerator = 0;
            int denominator = 0;

            // Calculate the numerator and denominator
            for (int j = 0; j < dataSplits.length; j++) {
                numerator += levelTwoGoodAttributes.get(j).size() * dataSplits[j].numInstances();
                denominator += dataSplits[j].numInstances();
            }

            possibleNumberTrees = numerator / denominator;

            int x = 0;
            ArrayList<SysForTree> levelTwoTrees = null;
            while ((forest.size() < numberTrees) && (x <= possibleNumberTrees - 1)) {
                levelTwoTrees = new ArrayList<SysForTree>();
                for (int j = 0; j < dataSplits.length; j++) {
                    if ((levelTwoGoodAttributes.get(j).size() - 1) > x) {
                        SysForTree newSubTree = new SysForTree(levelTwoGoodAttributeObjects.get(j).get(x + 1));
                        newSubTree.buildClassifier(dataSplits[j]);
                        levelTwoTrees.add(newSubTree);
                    } else {
                        SysForTree newSubTree;
                        // There won't be any good attribute objects if there's only 1 record in the split.
                        if (dataSplits[j].numInstances() == 1 || levelTwoGoodAttributeObjects.get(j).isEmpty()) {
                            newSubTree = new SysForTree();
                        } else {
                            newSubTree = new SysForTree(levelTwoGoodAttributeObjects.get(j).get(0));
                        }
                        newSubTree.buildClassifier(dataSplits[j]);
                        levelTwoTrees.add(newSubTree);
                    }
                }
                SysForTree[] levelTwoTreesArray = new SysForTree[levelTwoTrees.size()];
                for (int j = 0; j < levelTwoTreesArray.length; j++) {
                    levelTwoTreesArray[j] = levelTwoTrees.get(j);
                }
                SysForLevelTwoTree levelTwoTree = new SysForLevelTwoTree(levelTwoTreesArray,
                        goodAttributeObjects.get(i));
                levelTwoTree.buildClassifier(data);
                forest.add(levelTwoTree);
                x++;
            }
            i++;
        }
    }
    
    /*
     * This method corresponds to Algorithm 2 in the SysFor paper.
     *
     * @param dataset - subsection of the dataset on which to find good
     * attributes
     * @param goodness - goodness threshold, determines the maximum difference
     * between gain ratios of selected split points
     * @return the
     * java.util.ArrayList<weka.classifiers.trees.SysFor.GoodAttribute>
     */
    private ArrayList<GoodAttribute> getGoodAttributes(Instances dataset, float goodness) {
        // Initialize a set of attributes, a set of split points, and a set of gain ratios to empty.
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        ArrayList<Double> splitPoints = new ArrayList<Double>();
        ArrayList<Double> gainRatios = new ArrayList<Double>();

        // For each attribute in the dataset (that isn't the class attribute):
        for (int i = 0; i < dataset.numAttributes(); i++) {
            // Calculate the domain of the attribute (highest value minus lowest value)
            double currentAttributeDomain = attDomains[i];

            Attribute currentAttribute = dataset.attribute(i);
            if (i == dataset.classIndex()) {
                continue;
            }
            // If the current attribute is categorical:
            if (currentAttribute.isNominal()) {
                // Calculate the gain ratio of the current attribute.
                C45SplitTS split = new C45SplitTS(i, minRecLeaf, 0.0);
                try {
                    split.buildClassifier(dataset);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Store the corresponding information into attributes, splitPoints, and gainRatios
                DistributionTS dist = split.distribution();
                int[][] perSplitClassCount = new int[dist.numBags()][dataset.numClasses()];
                int splitsThatHaveMinimumRecords = 0;
                for (int x = 0; x < perSplitClassCount.length; x++) {
                    for (int y = 0; y < perSplitClassCount[x].length; y++) {
                        perSplitClassCount[x][y] = (int) dist.perClassPerBag(x, y);
                    }
                    if (dist.perBag(x) >= minRecLeaf) {
                        splitsThatHaveMinimumRecords++;
                    }
                }

                if (splitsThatHaveMinimumRecords < 2) {
                    continue;
                }

                DistributionTS noSplitDist = null;
                try {
                    noSplitDist = new DistributionTS(dataset);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Calculate the information before splitting.
                double infoBefore = 0.0;
                for (int j = 0; j < dataset.numClasses(); j++) {
                    infoBefore += (noSplitDist.perClass(j) / dataset.numInstances())
                            * logFunc(noSplitDist.perClass(j) / dataset.numInstances());
                }
                infoBefore = -1 * infoBefore;
                double gain = calculateGainNominal(perSplitClassCount, dataset.numClasses(), infoBefore);
                double splitInfo = 0.0;
                splitInfo = calculateNominalSplitInfo(perSplitClassCount, numClasses);
                double gainRatio = gain / splitInfo;
                gainRatios.add(gainRatio);
                splitPoints.add(-100.0);
                attributes.add(currentAttribute);
            } else if (currentAttribute.isNumeric()) {
                // Initialize a set of candidate attributes 
                // and a set of candidate gain ratios to empty.
                ArrayList<Double> candidateSplitPoints = new ArrayList<Double>();
                ArrayList<Double> candidateGainRatios = new ArrayList<Double>();

                // Create a set of available split points on currentAttribute
                ArrayList<Double> availableSplitPoints = findAvailableSplitPoints(dataset, i);
                // Create a set of corresponding gain ratios
                ArrayList<Double> availableGainRatios = new ArrayList<Double>();
                for (int j = 0; j < availableSplitPoints.size(); j++) {
                    availableGainRatios.add(0.0);
                }

                // Move instances from the right side of a potential split one split point at a time.
                // During each iteration, calculate the gain ratio of the split and store it in availableGainRatios
                availableGainRatios = calculateNumericSplitsGR(dataset, availableSplitPoints, i);

                // Find all the best split points within this attribute that satisfy the separation threshold.
                while (!availableSplitPoints.isEmpty()) {
                    // Find the highest gain ratio from the available gain ratios.
                    double maxGainRatio = Double.NEGATIVE_INFINITY;
                    int maxGainRatioIndex = -1;
                    for (int j = 0; j < availableGainRatios.size(); j++) {
                        if (availableGainRatios.get(j) > maxGainRatio) {
                            maxGainRatio = availableGainRatios.get(j);
                            maxGainRatioIndex = j;
                        }
                    }

                    // Store the highest gain ratio and its corresponding split point in candidateGainRatios 
                    // and candidateSplitPoints, then remove them from the available lists.
                    candidateGainRatios.add(availableGainRatios.get(maxGainRatioIndex));
                    candidateSplitPoints.add(availableSplitPoints.get(maxGainRatioIndex));
                    availableGainRatios.remove(maxGainRatioIndex);
                    availableSplitPoints.remove(maxGainRatioIndex);

                    ArrayList<Integer> newAvailableSplitPointsIndices = recalculateAvailableSplitPoints(
                            availableSplitPoints,
                            candidateSplitPoints,
                            currentAttributeDomain);

                    // Update availableGainRatios and availableSplitPoints using the above recalculated indices
                    ArrayList<Double> newAvailableSplitPoints = new ArrayList<Double>();
                    ArrayList<Double> newAvailableGainRatios = new ArrayList<Double>();
                    for (int j = 0; j < newAvailableSplitPointsIndices.size(); j++) {
                        newAvailableSplitPoints.add(availableSplitPoints.get(newAvailableSplitPointsIndices.get(j)));
                        newAvailableGainRatios.add(availableGainRatios.get(newAvailableSplitPointsIndices.get(j)));
                    }
                    availableSplitPoints = newAvailableSplitPoints;
                    availableGainRatios = newAvailableGainRatios;
                }

                while (!candidateSplitPoints.isEmpty()) {
                    attributes.add(currentAttribute);
                    // Find the highest gain ratio from the candidate gain ratios.
                    double maxGainRatio = Double.NEGATIVE_INFINITY;
                    int maxGainRatioIndex = -1;
                    for (int j = 0; j < candidateGainRatios.size(); j++) {
                        if (candidateGainRatios.get(j) > maxGainRatio) {
                            maxGainRatio = candidateGainRatios.get(j);
                            maxGainRatioIndex = j;
                        }
                    }
                    gainRatios.add(maxGainRatio);
                    splitPoints.add(candidateSplitPoints.get(maxGainRatioIndex));

                    // Remove the best gain ratio and corresponding split point from candidateGainRatios and 
                    // candidateSplitPoints respectively.
                    candidateGainRatios.remove(maxGainRatioIndex);
                    candidateSplitPoints.remove(maxGainRatioIndex);
                }
            }
        }
        // Sort the gain ratios in descending order. Update the attributes and splitpoints accordingly.
        GoodAttributesWithGainRatios goodAttributesWithGainRatios = new GoodAttributesWithGainRatios(attributes,
                splitPoints,
                gainRatios);
        goodAttributesWithGainRatios.sort();

        // Remove the elements in gainRatios, and corresponding elements in attributes and splitPoints
        // where the difference between a gain ratio and the best gain ratio in gainRatios is greater than 
        // goodness
        if (goodAttributesWithGainRatios.size() > 0) {
            double bestGainRatio = goodAttributesWithGainRatios.getGainRatio(0);
            for (int i = 1; i < goodAttributesWithGainRatios.size(); i++) {
                double currentGainRatio = goodAttributesWithGainRatios.getGainRatio(i);
                if (currentGainRatio == Double.NEGATIVE_INFINITY) {
                    currentGainRatio = 0;
                }
                if (Math.abs(currentGainRatio - bestGainRatio) > goodness) {
                    goodAttributesWithGainRatios.remove(i);
                    i--;
                }
            }
        }

        return goodAttributesWithGainRatios.getGoodAttributes();
    }
    
    /*
     * Returns the distribution of tree votes for the available classes,
     * classifying using majority voting.
     *
     * @param instance - the instance to be classified
     * @return probablity distribution for this instance's classification
     * @throws Exception
     */
    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {

        //if the forest hasn't been built or is empty due to poorly selected attribute values
        if (forest == null || forest.isEmpty()) {
            ZeroR zr = new ZeroR();
            zr.buildClassifier(dataset);
            return zr.distributionForInstance(instance);
        }

        double[] returnValue = new double[numClasses];
        double[] fullSupports = new double[numClasses];

        for (int i = 0; i < numClasses; i++) {
            returnValue[i] = 0;
            fullSupports[i] = 0;
        }
        for (int j = 0; j < forest.size(); j++) {

            double[] currentTreeDistribution = forest.get(j).distributionForInstance(instance);
            double cl = forest.get(j).classifyInstance(instance);

            int i = Utils.maxIndex(currentTreeDistribution);
            fullSupports[i] += currentTreeDistribution[i];
            returnValue[(int) cl]++;
        }

        Utils.normalize(returnValue);

        if (Double.compare(0.5, returnValue[0]) == 0) { //in the case of a tie
            Utils.normalize(fullSupports); // get it in distribution form
            returnValue = fullSupports;
        }

        return returnValue;

    }
    
    /*
     * Calculate the domain of the attribute (highest value minus lowest value
     * for numerical. Number of distinct values for nominal attributes).
     *
     * @param dataset - dataset on which to calculate the domain
     * @param attributeIndex - index in the dataset of the attribute to
     * calculate
     * @return the range of the dataset for numerical attributes, number of
     * distinct values for nominal attributes
     */
    private double calculateAttributeDomain(Instances dataset, int attributeIndex) {
        if (dataset.attribute(attributeIndex).isNumeric()) {
            // Sort the attributes values and store them as a list.
            double[] values = dataset.attributeToDoubleArray(attributeIndex);
            Double[] objectValues = new Double[values.length];
            for (int i = 0; i < objectValues.length; i++) {
                objectValues[i] = values[i];
            }
            Arrays.sort(objectValues);
            ArrayList<Double> lValues = new ArrayList<Double>();
            lValues.addAll(Arrays.asList(objectValues));

            // Create an iterator object for the sorted attribute values
            Iterator<Double> iValues = lValues.iterator();

            double minValue = Double.POSITIVE_INFINITY;
            double maxValue = Double.NEGATIVE_INFINITY;

            while (iValues.hasNext()) {
                double currentValue = iValues.next();
                if (currentValue < minValue) {
                    minValue = currentValue;
                }
                if (currentValue > maxValue) {
                    maxValue = currentValue;
                }
            }
            double domain = maxValue - minValue;
            return domain;
        } else {
            return dataset.numDistinctValues(attributeIndex);
        }

    }
    
    /*
     * Main method for testing this class.
     *
     * @param argv should contain the following arguments: -t training file [-T
     * test file] [-c class index]
     */
    public static void main(String[] argv) throws Exception {
        runClassifier(new SysFor(), argv);
    }
    
    /*
     * Returns capabilities of algorithm
     *
     * @return Weka capabilities of SysFor
     */
    @Override
    public Capabilities getCapabilities() {

        Capabilities result = super.getCapabilities();   // returns the object from weka.classifiers.Classifier

        // attributes
        result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capabilities.Capability.MISSING_VALUES);
        result.disable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
        result.disable(Capabilities.Capability.STRING_ATTRIBUTES);

        // class
        result.enable(Capabilities.Capability.NOMINAL_CLASS);
        result.disable(Capabilities.Capability.NUMERIC_CLASS);
        result.disable(Capabilities.Capability.DATE_CLASS);
        result.disable(Capabilities.Capability.RELATIONAL_CLASS);
        result.disable(Capabilities.Capability.UNARY_CLASS);
        result.disable(Capabilities.Capability.NO_CLASS);
        result.disable(Capabilities.Capability.STRING_CLASS);
        return result;

    }
    
    /*
     * Return a description suitable for displaying in the
     * explorer/experimenter.
     *
     * @return a description suitable for displaying in the
     * explorer/experimenter
     */
    public String globalInfo() {
        return "Implementation of the decision forest algorithm SysFor, which was published in: \n"
                + "Md Zahidul Islam and Helen Giggins: Knowledge Discovery through SysFor -  "
                + "a Systematically Developed Forest of Multiple Decision Trees In: Ninth"
                + "Australasian Data Mining Conference, 195-204, 2011.\n"
                + "For more information, see:\n\n" + getTechnicalInformation().toString();
    }
    
    /*
     * Inner class for GoodAttributes. A GoodAttribute object is comprised of an
     * attribute, and a split point on that attribute.
     */
    private class GoodAttribute implements Serializable {

        private static final long serialVersionUID = 5731302547322418250L;
    
        Attribute attribute;
        double splitPoint;
    
        GoodAttribute(Attribute a, double split) {
            this.setAttribute(a, split);
        }
    
        void setAttribute(Attribute a, double split) {
            attribute = a;
            splitPoint = split;
        }
    
        Attribute getAttribute() {
            return this.attribute;
        }
    
        Double getSplitPoint() {
            return splitPoint;
        }
    }
    
    /*
     * This class is used specifically to store attribute, splitPoint, and
     * corresponding gain ratio information in one object. It is used in the
     * GoodAttributesWithGainRatios class which can sort a collection of
     * GoodAttributeWithGainRatio objects.
     *
     * @author Michael J. Siers
     */
    private class GoodAttributeWithGainRatio extends GoodAttribute implements Comparable {

        private static final long serialVersionUID = 3700968715919025916L;

        private double gainRatio;

        public double getGainRatio() {
            return gainRatio;
        }
    
        GoodAttributeWithGainRatio(Attribute a, double split, double gainRatio) {
            super(a, split);
            this.gainRatio = gainRatio;
        }

        @Override
        public int compareTo(Object a) {
            return Double.compare(this.gainRatio, ((GoodAttributeWithGainRatio) a).gainRatio);
        }
    }
    
    /*
     * This class is used specifically to sort attribute, splitPoint, and
     * gainRatio arrays simultaneously based on gain ratio.
     *
     * @author Michael J. Siers
     */
    private class GoodAttributesWithGainRatios implements Serializable {

        private static final long serialVersionUID = 2339265760515386479L;

        private ArrayList<GoodAttributeWithGainRatio> elements;
    
        GoodAttributesWithGainRatios(ArrayList<Attribute> attributes, ArrayList<Double> splitPoints,
                                     ArrayList<Double> gainRatios) {

            elements = new ArrayList<GoodAttributeWithGainRatio>();
            for (int i = 0; i < attributes.size(); i++) {
                elements.add(new GoodAttributeWithGainRatio(attributes.get(i), splitPoints.get(i), gainRatios.get(i)));
            }
        }
        
        /*
         * Sorts the elements into descending order of gain ratio
         */
        void sort() {
            Collections.sort(elements, Collections.reverseOrder());
        }
    
        ArrayList<GoodAttribute> getGoodAttributes() {
            ArrayList<GoodAttribute> returnValue = new ArrayList<GoodAttribute>();

            for (int i = 0; i < elements.size(); i++) {
                returnValue.add(elements.get(i));
            }

            return returnValue;
        }
    
        int size() {
            return elements.size();
        }
    
        double getGainRatio(int index) {
            return elements.get(index).gainRatio;
        }
    
        void remove(int index) {
            elements.remove(index);
        }
    }
    
    /*
     *
     */
    private ArrayList<Double> findAvailableSplitPoints(Instances dataset, int attributeIndex) {
        // Initialize the return value
        ArrayList<Double> splitPoints = new ArrayList<Double>();
        if (dataset.numInstances() == 0 || dataset.numInstances() == 1) {
            return splitPoints;
        }

        // Sort the attributes values and store them as a list.
        double[] values = dataset.attributeToDoubleArray(attributeIndex);
        Double[] objectValues = new Double[values.length];
        for (int i = 0; i < objectValues.length; i++) {
            objectValues[i] = values[i];
        }
        Arrays.sort(objectValues);
        ArrayList<Double> lValues = new ArrayList<Double>();
        lValues.addAll(Arrays.asList(objectValues));

        // Create an iterator object for the sorted attribute values
        Iterator<Double> iValues = lValues.iterator();

        // Get the first element
        double previousElement = iValues.next();

        NumericSplitDistribution dist = new NumericSplitDistribution(dataset, attributeIndex);
        // Now add the midpoints between each adjacent value to the return value.
        do {
            double valueOne = previousElement;
            double valueTwo = iValues.next();

            if (valueOne != valueTwo) {
                double midPointValue = (valueOne + valueTwo) / 2;

                dist.shiftRecords(midPointValue);
                if (dist.getNumberLeftSideInstances() >= minRecLeaf && dist.getNumberRightSideInstances() >= minRecLeaf) {
                    splitPoints.add(midPointValue);
                }
            }

            previousElement = valueTwo;
        } while (iValues.hasNext());

        return splitPoints;
    }
    
    /*
     * This method removes split points from the passed array which do not
     * satisfy the separation threshold equation.
     *
     * @param availableSplitPoints the available split points
     * @param candidateSplitPoints the already candidate split points
     * @param attributeDomain the highest value of this attribute minus the
     * lowest attribute.
     * @return the indexes of the recalculated available split points
     */
    private ArrayList<Integer> recalculateAvailableSplitPoints(ArrayList<Double> availableSplitPoints,
            ArrayList<Double> candidateSplitPoints,
            double attributeDomain) {

        ArrayList<Integer> returnValue = new ArrayList<Integer>();

        for (int i = 0; i < availableSplitPoints.size(); i++) {
            double currentSplitPoint = availableSplitPoints.get(i);
            boolean keepCurrentPoint = true;
            for (int j = 0; j < candidateSplitPoints.size(); j++) {
                if ((Math.abs(currentSplitPoint - candidateSplitPoints.get(j))) / attributeDomain <= separation) {
                    keepCurrentPoint = false;
                    break;
                }
            }
            if (keepCurrentPoint) {
                returnValue.add(i);
            }
        }

        return returnValue;
    }
    
    /*
     * A method for finding the gain ratios of a numeric attribute, given an
     * array of all possible split points. The method returns the available gain
     * ratios list
     *
     * @param instances
     * @param availableSplitPoints
     * @param attrIndex
     * @return
     */
    private ArrayList<Double> calculateNumericSplitsGR(Instances instances, ArrayList<Double> availableSplitPoints, int attrIndex) {
        ArrayList<Double> availableGainRatios = new ArrayList<Double>();
        for (int i = 0; i < availableSplitPoints.size(); i++) {
            availableGainRatios.add(0.0);
        }
        // Calculate the information before splitting.
        double noSplitInfo = 0.0;
        NumericSplitDistribution dist = new NumericSplitDistribution(instances, attrIndex);
        int totalLeft = dist.leftSideInstances.numInstances();
        int totalRight = dist.rightSideInstances.numInstances();
        for (int i = 0; i < instances.numClasses(); i++) {
            noSplitInfo += ((double) dist.rightClassCount[i] / totalRight) * logFunc((double) dist.rightClassCount[i] / totalRight);
        }
        noSplitInfo = -1 * noSplitInfo;

        double gain = 0.0;
        for (int i = 0; i < availableSplitPoints.size(); i++) {
            dist.shiftRecords(availableSplitPoints.get(i));
            gain = calculateGainNumeric(dist.leftClassCount, dist.rightClassCount, instances.numClasses(), noSplitInfo);
            double splitInfo = calculateNumericSplitInfo(dist.leftClassCount, dist.rightClassCount, instances.numClasses());
            double gainRatio = gain / splitInfo;
            availableGainRatios.set(i, gainRatio);
        }
        return availableGainRatios;
    }
    
    /*
     * Calculates the split info given number of class instances each side of
     * split for a numeric split
     *
     * @param leftClassCount - class instances in left of split
     * @param rightClassCount - class instances in right of split
     * @param numClasses - amount of classes in dataset
     * @return splitInfo for given numeric split
     */
    private double calculateNumericSplitInfo(int[] leftClassCount, int[] rightClassCount, int numClasses) {
        double splitInfo = 0.0;

        int totalRight = 0;
        int totalLeft = 0;
        for (int i = 0; i < numClasses; i++) {
            totalRight += rightClassCount[i];
            totalLeft += leftClassCount[i];
        }
        int totalRecords = totalRight + totalLeft;

        double x1 = (double) totalLeft / totalRecords;
        double y1 = logFunc((double) totalLeft / totalRecords);
        double z1 = x1 * y1;
        double x2 = (double) totalRight / totalRecords;
        double y2 = logFunc((double) totalRight / totalRecords);
        double z2 = x2 * y2;

        splitInfo += ((double) totalLeft / totalRecords) * logFunc((double) totalLeft / totalRecords);
        splitInfo += ((double) totalRight / totalRecords) * logFunc((double) totalRight / totalRecords);

        return -splitInfo;
    }
    
    /*
     * Calculates the split info given number of class instances each part of a
     * nominal split
     *
     * @param dataSplitsClassCounts - Class counts in each of the subsets
     * created in the nominal split
     * @param numClasses - number of classes in the dataset
     * @return
     */
    private double calculateNominalSplitInfo(int[][] dataSplitsClassCounts, int numClasses) {
        double splitInfo = 0.0;

        int[] totalSplitCounts = new int[dataSplitsClassCounts.length];
        for (int j = 0; j < dataSplitsClassCounts.length; j++) {
            for (int i = 0; i < numClasses; i++) {
                totalSplitCounts[j] += dataSplitsClassCounts[j][i];
            }
        }

        int totalRecords = 0;
        for (int i = 0; i < totalSplitCounts.length; i++) {
            totalRecords += totalSplitCounts[i];
        }

        for (int i = 0; i < dataSplitsClassCounts.length; i++) {
            splitInfo += ((double) totalSplitCounts[i] / totalRecords) * logFunc((double) totalSplitCounts[i] / totalRecords);
        }

        return -splitInfo;
    }
    
    /*
     * Calculates the information gain for a split on a numerical attribute
     *
     * @param leftClassCount - class instances on left of split
     * @param rightClassCount - class instances on right of split
     * @param numClasses - number of distinct classes in dataset
     * @param infoBeforeSplit - split info prior to split
     * @return
     */
    private double calculateGainNumeric(int[] leftClassCount, int[] rightClassCount, int numClasses, double infoBeforeSplit) {
        double infoAfterSplit = 0.0;

        int totalRight = 0;
        int totalLeft = 0;
        for (int i = 0; i < numClasses; i++) {
            totalRight += rightClassCount[i];
            totalLeft += leftClassCount[i];
        }
        int totalRecords = totalRight + totalLeft;

        double infoLessThan = 0.0;
        double infoGreaterThan = 0.0;
        for (int i = 0; i < numClasses; i++) {
            infoLessThan += ((double) leftClassCount[i] / totalLeft) * logFunc((double) leftClassCount[i] / totalLeft);
        }
        infoLessThan = -1 * infoLessThan;
        infoLessThan *= (double) totalLeft / totalRecords;
        for (int i = 0; i < numClasses; i++) {
            infoGreaterThan += ((double) rightClassCount[i] / totalRight) * logFunc((double) rightClassCount[i] / totalRight);
        }
        infoGreaterThan = -1 * infoGreaterThan;
        infoGreaterThan *= (double) totalRight / totalRecords;
        infoAfterSplit = infoGreaterThan + infoLessThan;

        return infoBeforeSplit - infoAfterSplit;
    }
    
    /*
     * Calculates the information gain for a split on a nominal attribute
     *
     * @param dataSplitsClassCounts - class counts for the subsets from the
     * split
     * @param numClasses - number of distinct classes in dataset
     * @param infoBeforeSplit - split info prior to split
     * @return
     */
    private double calculateGainNominal(int[][] dataSplitsClassCounts, int numClasses, double infoBeforeSplit) {
        double infoAfterSplit = 0.0;

        int[] totalSplitCounts = new int[dataSplitsClassCounts.length];
        for (int j = 0; j < dataSplitsClassCounts.length; j++) {
            for (int i = 0; i < numClasses; i++) {
                totalSplitCounts[j] += dataSplitsClassCounts[j][i];
            }
        }

        int totalRecords = 0;
        boolean fruitlessSplit = false; // if this is a split on an already homogenous dataset
        for (int i = 0; i < totalSplitCounts.length; i++) {
            if (totalSplitCounts[i] == 0) {
                fruitlessSplit = true;
            }
            totalRecords += totalSplitCounts[i];
        }

        if (!fruitlessSplit) {
            double[] splitsInfos = new double[dataSplitsClassCounts.length];
            for (int j = 0; j < splitsInfos.length; j++) {
                for (int i = 0; i < numClasses; i++) {
                    splitsInfos[j] += ((double) dataSplitsClassCounts[j][i] / totalSplitCounts[j]) * logFunc((double) dataSplitsClassCounts[j][i] / totalSplitCounts[j]);
                }
                splitsInfos[j] = -1 * splitsInfos[j];
                splitsInfos[j] *= (double) totalSplitCounts[j] / totalRecords;
            }

            infoAfterSplit = 0;
            for (int i = 0; i < splitsInfos.length; i++) {
                infoAfterSplit += splitsInfos[i];
            }
        } else { //fruitless split
            infoAfterSplit = 0;
        }

        return infoBeforeSplit - infoAfterSplit;
    }
    
    /*
     * The log of 2, taken from EntropyBasedSplitCrit.java.
     */
    private static double log2 = Math.log(2);
    
    /*
     * Help method for computing entropy, taken from EntropyBasedSplitCrit.java.
     *
     * @param num - number to find log2 of
     * @return log2 of num
     */
    private double logFunc(double num) {

        // Constant hard coded for efficiency reasons
        if (num < 1e-6) {
            return 0;
        } else {
            return Math.log(num) / log2;
        }
    }
    
    /*
     * This class can keep track of how many records of each class are in each
     * side of a numeric SysFor split.
     *
     * @author Michael J. Siers
     *
     */
    private class NumericSplitDistribution implements Serializable {

        private static final long serialVersionUID = 5260380811391105058L;

        private Instances leftSideInstances;
        private Instances rightSideInstances;
        private int[] leftClassCount;
        private int[] rightClassCount;
        private int attrIndex;
        
        /*
         * Constructor
         *
         * @param instances all of the instances
         * @param attrIndex the index of the attribute that will be tested to
         * create the split
         */
        NumericSplitDistribution(Instances instances, int attrIndex) {
            this.attrIndex = attrIndex;
            this.leftSideInstances = new Instances(instances, 0);
            this.rightSideInstances = new Instances(instances);
            this.rightSideInstances.sort(attrIndex);
            leftClassCount = new int[instances.numClasses()];
            rightClassCount = new int[instances.numClasses()];

            Enumeration eInstances = instances.enumerateInstances();
            while (eInstances.hasMoreElements()) {
                Instance currentInstance = (Instance) eInstances.nextElement();
                rightClassCount[(int) currentInstance.classValue()]++;
            }
        }
        
        /*
         * Provides total number of instances at this split point
         *
         * @return number of instances at split point
         */
        public int getNumberInstances() {
            return leftSideInstances.numInstances() + rightSideInstances.numInstances();
        }
        
        /*
         * Provides number of instances on the right of this split
         *
         * @return number of instances at right of split
         */
        int getNumberRightSideInstances() {
            return rightSideInstances.numInstances();
        }
        
        /*
         * Provides number of instances on the left of this split
         *
         * @return number of instances at left of split
         */
        int getNumberLeftSideInstances() {
            return leftSideInstances.numInstances();
        }
        
        /*
         * Shifts the records based on the passed split point. Also returns the
         * number of records that have been shifted.
         *
         * @param splitPoint the split value. Records with less than or equal to
         * will be shifted from the right side to the left side.
         * @return the number of records that were shifted.
         */
        int shiftRecords(double splitPoint) {
            if (rightSideInstances.numInstances() == 0) {
                return 0;
            }
            int shiftCount = 0;

            while (rightSideInstances.numInstances() > 0 && rightSideInstances.firstInstance().value(attrIndex) <= splitPoint) {
                Instance currentInstance = rightSideInstances.firstInstance();
                leftClassCount[(int) currentInstance.classValue()]++;
                rightClassCount[(int) currentInstance.classValue()]--;
//                leftSideInstances.add(new DenseInstance(currentInstance));
                leftSideInstances.add(currentInstance);
                rightSideInstances.delete(0);
                shiftCount++;
            }

            return shiftCount;
        }
    }
    
    /*
     * An inner class for representing a single tree within a SysFor forest.
     * This is a C4.5 tree with a specified root attribute. The split point is
     * also specified if the attribute is numeric. There is another class:
     * "SysForLevelTwoTree" which is to be used for representing a SysForTree
     * which has both the root attribute, and some/all of the next level split
     * points specified also.
     *
     * @author Michael J. Siers
     */
    private class SysForTree extends Classifier {

        private static final long serialVersionUID = -6792080072901419517L;

        private ArrayList<Integer> distribution;
        private int[][] splitsClassDist;
        private GoodAttribute rootSplit;
        private int majorityIndex;
        private int[] splitsMajorityIndexes;
        private String[] splitsMajorityValueNames;
        private String majorityValueName;
        private ArrayList<J48TS> c45Trees = new ArrayList<J48TS>();
        private int numClasses = -1;
        private boolean leaf = false;

        // The number of leaves in this sysfor tree
        private int numberLeaves = 0;
    
        SysForTree(GoodAttribute rootSplit) {
            this.rootSplit = rootSplit;
        }
        
        /*
         * Creates a SysForTree object which will not split
         */
        SysForTree() {
            leaf = true;
        }

        @Override
        public double[] distributionForInstance(Instance instance) throws Exception {
            if (leaf) {
                double[] rootDist = new double[numClasses];
                for (int i = 0; i < numClasses; i++) {
                    rootDist[i] = (double) splitsClassDist[0][i];
                }
                return rootDist;
            }
            double[] returnValue = new double[numClasses];

            Attribute rootAttribute = rootSplit.getAttribute();
            double splitValue = rootSplit.getSplitPoint();

            if (rootAttribute.isNumeric()) {
                if (instance.value(rootAttribute) > splitValue) {
                    return c45Trees.get(0).distributionForInstance(instance);
                } else {
                    return c45Trees.get(1).distributionForInstance(instance);
                }
            } else if (rootAttribute.isNominal()) {
                int treeIndex = (int) instance.value(rootAttribute);
                return c45Trees.get(treeIndex).distributionForInstance(instance);
            } else {
                for (int i = 0; i < numClasses; i++) {
                    returnValue[i] = 0;
                }
                return returnValue;
            }
        }
    
        int measureNumLeaves() {
            int numLeaves = 0;
            for (int i = 0; i < c45Trees.size(); i++) {
                int currentTreeNumLeaves = (int) c45Trees.get(i).measureNumLeaves();
                if (currentTreeNumLeaves == 0) {
                    numLeaves++;
                } else {
                    numLeaves += currentTreeNumLeaves;
                }
            }
            return numLeaves;
        }

        @Override
        public void buildClassifier(Instances data) throws Exception {
            this.numClasses = data.numClasses();
            if (leaf) {
                Instances[] dataSplits = new Instances[1];
                dataSplits[0] = new Instances(data);

                distribution = new ArrayList<Integer>();
                distribution.add(1);
                splitsClassDist = new int[1][numClasses];

//                if (data.isEmpty()) {
                if (data.numInstances() == 0) {
                    splitsClassDist[0][0]++;
                    splitsMajorityIndexes = new int[1];
                    splitsMajorityIndexes[0] = 0;
                } else {
                    splitsClassDist[0][(int) data.firstInstance().classValue()]++;
                    splitsMajorityIndexes = new int[1];
                    splitsMajorityIndexes[0] = (int) data.firstInstance().classValue();
                }

                splitsMajorityValueNames = new String[dataSplits.length];
                splitsMajorityValueNames[0] = data.classAttribute().value(splitsMajorityIndexes[0]);

                for (int i = 0; i < dataSplits.length; i++) {
//---------------------------------------------------------------------------------------------------
                    if (MainActivity.killThread == true) break;
//---------------------------------------------------------------------------------------------------
                    J48TS currentSubTree = new J48TS();
                    currentSubTree.setConfidenceFactor(confidence);
                    currentSubTree.setMinNumObj(minRecLeaf);
                    currentSubTree.buildClassifier(dataSplits[i]);
                    c45Trees.add(currentSubTree);

                    numberLeaves += currentSubTree.measureNumLeaves();
                }

                return;
            }

            Instances[] dataSplits = splitData(data, rootSplit);
            distribution = new ArrayList<Integer>();
            splitsClassDist = new int[dataSplits.length][numClasses];
            splitsMajorityIndexes = new int[dataSplits.length];
            splitsMajorityValueNames = new String[dataSplits.length];

            for (int i = 0; i < dataSplits.length; i++) {
                distribution.add(0);
            }

            for (int i = 0; i < dataSplits.length; i++) {
                distribution.set(i, dataSplits[i].numInstances());
            }

            for (int i = 0; i < dataSplits.length; i++) {
//---------------------------------------------------------------------------------------------------
                if (MainActivity.killThread == true) break;
//---------------------------------------------------------------------------------------------------
                for (int j = 0; j < dataSplits[i].numInstances(); j++) {
                    Instance currentInstance = dataSplits[i].instance(j);
                    splitsClassDist[i][(int) currentInstance.classValue()]++;
                }
            }

            for (int i = 0; i < dataSplits.length; i++) {
//---------------------------------------------------------------------------------------------------
                if (MainActivity.killThread == true) break;
//---------------------------------------------------------------------------------------------------
                int currentLargestValueSupport = -1;

                for (int j = 0; j < numClasses; j++) {
                    if (splitsClassDist[i][j] > currentLargestValueSupport) {
                        currentLargestValueSupport = splitsClassDist[i][j];
                        splitsMajorityIndexes[i] = j;
                    }
                }
                splitsMajorityValueNames[i] = data.classAttribute().value(splitsMajorityIndexes[i]);
            }

            for (int i = 0; i < dataSplits.length; i++) {
//---------------------------------------------------------------------------------------------------
                if (MainActivity.killThread == true) break;
//---------------------------------------------------------------------------------------------------
                J48TS currentSubTree = new J48TS();
                currentSubTree.setConfidenceFactor(confidence);
                currentSubTree.setMinNumObj(minRecLeaf);
                currentSubTree.buildClassifier(dataSplits[i]);
                c45Trees.add(currentSubTree);

                numberLeaves += currentSubTree.measureNumLeaves();
            }
        }
        
        /*
         * Returns string representation of forest.
         *
         * @return string representation of forest,
         */
        @Override
        public String toString() {
            String treeString = "";

            Attribute rootAttribute = rootSplit.getAttribute();
            double splitValue = rootSplit.getSplitPoint();

            if (rootAttribute.isNumeric()) {
                //treeString += "|   ";
                treeString += rootAttribute.name();
                treeString += " ";
                treeString += "<=";
                treeString += " ";
                treeString += splitValue;

                if (c45Trees.get(1).measureNumLeaves() == 1) {
                    treeString += ": ";
                    treeString += splitsMajorityValueNames[1];
                    treeString += " {";
                    // count the remaining support
                    int remainingSupport = 0;
                    for (int j = 0; j < splitsClassDist[1].length; j++) {
                        treeString += classNames[j] + "," + splitsClassDist[1][j] + ";";
                        if (j == splitsMajorityIndexes[1]) {
                            continue;
                        }
                        remainingSupport += splitsClassDist[1][j];
                    }
                    treeString += "} (";
                    treeString += (splitsClassDist[1][splitsMajorityIndexes[1]] + remainingSupport);
                    treeString += "/";
                    treeString += remainingSupport;
                    treeString += ")";
                } else {
                    treeString += "\n";
                    String c45String = c45Trees.get(1).toString();
                    // remove the first 3 lines and the last 4 lines
                    c45String = c45String.substring(c45String.indexOf('\n') + 1);
                    c45String = c45String.substring(c45String.indexOf('\n') + 1);
                    c45String = c45String.substring(c45String.indexOf('\n') + 1);
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.replace("\n", "\n|   ");
                    c45String = "|   " + c45String;
                    // the last line will have "|   ", so remove the last line.
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    treeString += c45String;
                }

                treeString += "\n";
                treeString += rootAttribute.name();
                treeString += " ";
                treeString += ">";
                treeString += " ";
                treeString += splitValue;

                if (c45Trees.get(0).measureNumLeaves() == 1) {
                    treeString += ": ";
                    treeString += splitsMajorityValueNames[0];
                    treeString += " {";
                    // count the remaining support
                    int remainingSupport = 0;
                    for (int j = 0; j < splitsClassDist[0].length; j++) {
                        treeString += classNames[j] + "," + splitsClassDist[0][j] + ";";
                        if (j == splitsMajorityIndexes[0]) {
                            continue;
                        }
                        remainingSupport += splitsClassDist[0][j];
                    }
                    treeString += "} (";
                    treeString += (splitsClassDist[0][splitsMajorityIndexes[0]] + remainingSupport);
                    treeString += "/";
                    treeString += remainingSupport;
                    treeString += ")";
                } else {
                    treeString += "\n";
                    String c45String = c45Trees.get(0).toString();
                    // remove the first 3 lines and the last 4 lines
                    c45String = c45String.substring(c45String.indexOf('\n') + 1);
                    c45String = c45String.substring(c45String.indexOf('\n') + 1);
                    c45String = c45String.substring(c45String.indexOf('\n') + 1);
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    c45String = c45String.replace("\n", "\n|   ");
                    c45String = "|   " + c45String;
                    c45String.replaceAll("\n", "\n|   ");
                    // the last line will have "|   ", so remove the last line.
                    c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                    treeString += c45String;
                }
            } else if (rootAttribute.isNominal()) {
                for (int i = 0; i < c45Trees.size(); i++) {
                    if (i > 0) {
                        treeString += "\n";
                    }
                    treeString += "";
                    treeString += rootAttribute.name();
                    treeString += " ";
                    treeString += "=";
                    treeString += " ";
                    treeString += rootAttribute.value(i);

                    if (c45Trees.get(i).measureNumLeaves() == 1) {
                        treeString += ": ";
                        treeString += splitsMajorityValueNames[i];
                        treeString += " {";
                        // count the remaining support
                        int remainingSupport = 0;
                        for (int j = 0; j < splitsClassDist[i].length; j++) {
                            treeString += classNames[j] + "," + splitsClassDist[i][j] + ";";
                            if (j == splitsMajorityIndexes[i]) {
                                continue;
                            }
                            remainingSupport += splitsClassDist[i][j];
                        }
                        treeString += "} (";
                        treeString += (splitsClassDist[i][splitsMajorityIndexes[i]] + remainingSupport);
                        treeString += "/";
                        treeString += remainingSupport;
                        treeString += ")";
                    } else {
                        treeString += "\n";
                        String c45String = c45Trees.get(i).toString();
                        // remove the first 3 lines and the last 4 lines
                        c45String = c45String.substring(c45String.indexOf('\n') + 1);
                        c45String = c45String.substring(c45String.indexOf('\n') + 1);
                        c45String = c45String.substring(c45String.indexOf('\n') + 1);
                        c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                        c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                        c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                        c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                        c45String = c45String.replace("\n", "\n|   ");
                        c45String = "|   " + c45String;
                        // the last line will have "|   ", so remove the last line.
                        c45String = c45String.substring(0, c45String.lastIndexOf('\n'));
                        treeString += c45String;
                    }
                }
            }

            return treeString;
        }

    }

    private class SysForLevelTwoTree extends Classifier {

        private static final long serialVersionUID = 8018080734563784558L;

        private ArrayList<Integer> distribution;
        private int majorityIndex;
        private int[] splitsMajorityIndexes;
        private int[][] splitsClassDist;
        private String[] splitsMajorityValueNames;
        private String majorityValueName;
        private SysForTree[] subTrees = null;
        private GoodAttribute rootSplit;
        private int numClasses;
        private int numberLeaves = 0;
    
        SysForLevelTwoTree(SysForTree[] subTrees, GoodAttribute rootSplit) {
            this.subTrees = subTrees;
            this.rootSplit = rootSplit;
            for (int i = 0; i < subTrees.length; i++) {
                numberLeaves += subTrees[i].numberLeaves;
            }
        }

        @Override
        public void buildClassifier(Instances data) throws Exception {
            numClasses = data.numClasses();
            distribution = new ArrayList<Integer>();
            Instances[] dataSplits = splitData(data, rootSplit);
            for (int i = 0; i < dataSplits.length; i++) {
                distribution.add(0);
            }

            for (int i = 0; i < dataSplits.length; i++) {
                distribution.set(i, dataSplits[i].numInstances());
            }

            splitsClassDist = new int[dataSplits.length][numClasses];
            splitsMajorityIndexes = new int[dataSplits.length];
            splitsMajorityValueNames = new String[dataSplits.length];

            for (int i = 0; i < dataSplits.length; i++) {
                for (int j = 0; j < dataSplits[i].numInstances(); j++) {
                    Instance currentInstance = dataSplits[i].instance(j);
                    splitsClassDist[i][(int) currentInstance.classValue()]++;
                }
            }

            int largestValueSupport = -1;
            for (int i = 0; i < distribution.size(); i++) {
                if (distribution.get(i) > largestValueSupport) {
                    largestValueSupport = distribution.get(i);
                    majorityIndex = i;
                }
            }

            //furner added
            int biggest = -1;
            int myIndex = -1;
            for (int i = 0; i < numClasses; i++) {
                if (splitsClassDist[majorityIndex][i] > biggest) {
                    myIndex = i;
                    biggest = splitsClassDist[majorityIndex][i];
                }
            }
            //furner stop

            majorityValueName = data.classAttribute().value(myIndex);
            //majorityValueName = data.classAttribute().value(majorityIndex);

            for (int i = 0; i < dataSplits.length; i++) {
                int currentLargestValueSupport = -1;

                for (int j = 0; j < numClasses; j++) {
                    if (splitsClassDist[i][j] > currentLargestValueSupport) {
                        currentLargestValueSupport = splitsClassDist[i][j];
                        splitsMajorityIndexes[i] = j;
                    }
                }
                splitsMajorityValueNames[i] = data.classAttribute().value(splitsMajorityIndexes[i]);
            }
        }

        @Override
        public double[] distributionForInstance(Instance instance) throws Exception {
            Attribute rootAttribute = rootSplit.getAttribute();
            double splitValue = rootSplit.getSplitPoint();
            if (rootAttribute.isNumeric()) {
                if (instance.value(rootAttribute) > splitValue) {
                    return subTrees[0].distributionForInstance(instance);
                } else {
                    return subTrees[1].distributionForInstance(instance);
                }
            } else if (rootAttribute.isNominal()) {
                return subTrees[(int) instance.value(rootAttribute)].distributionForInstance(instance);
            } else {
                double[] returnValue = new double[numClasses];
                for (int i = 0; i < numClasses; i++) {
                    returnValue[i] = 0;
                }
                return returnValue;
            }
        }

        @Override
        public String toString() {
            String treeString = "";

            Attribute rootAttribute = rootSplit.getAttribute();
            double splitValue = rootSplit.getSplitPoint();

            if (rootAttribute.isNumeric()) {
                //treeString += "|   ";
                treeString += rootAttribute.name();
                treeString += " ";
                treeString += "<=";
                treeString += " ";
                treeString += splitValue;

                if (subTrees[1].measureNumLeaves() == 1) {
                    treeString += ": ";
                    treeString += splitsMajorityValueNames[1];
                    treeString += " {";
                    // count the remaining support
                    int remainingSupport = 0;
                    for (int j = 0; j < splitsClassDist[1].length; j++) {
                        treeString += classNames[j] + "," + splitsClassDist[1][j] + ";";
                        if (j == splitsMajorityIndexes[1]) {
                            continue;
                        }
                        remainingSupport += splitsClassDist[1][j];
                    }
                    treeString += "} (";
                    treeString += (splitsClassDist[1][splitsMajorityIndexes[1]] + remainingSupport);
                    treeString += "/";
                    treeString += remainingSupport;
                    treeString += ")";
                } else {
                    treeString += "\n";
                    String subTreeString = subTrees[1].toString();
                    subTreeString = subTreeString.replace("\n", "\n|   ");
                    subTreeString = "|   " + subTreeString;
                    // the last line will have "|   ", so remove the last line.
                    //subTreeString = subTreeString.substring(0, subTreeString.lastIndexOf('\n'));
                    treeString += subTreeString;
                }

                treeString += "\n";
                treeString += rootAttribute.name();
                treeString += " ";
                treeString += ">";
                treeString += " ";
                treeString += splitValue;

                if (subTrees[0].measureNumLeaves() == 1) {
                    treeString += ": ";
                    treeString += splitsMajorityValueNames[0];
                    treeString += " {";
                    // count the remaining support
                    int remainingSupport = 0;
                    for (int j = 0; j < splitsClassDist[0].length; j++) {
                        treeString += classNames[j] + "," + splitsClassDist[0][j] + ";";
                        if (j == splitsMajorityIndexes[0]) {
                            continue;
                        }
                        remainingSupport += splitsClassDist[0][j];
                    }
                    treeString += "} (";
                    treeString += (splitsClassDist[0][splitsMajorityIndexes[0]] + remainingSupport);
                    treeString += "/";
                    treeString += remainingSupport;
                    treeString += ")";
                } else {
                    treeString += "\n";
                    String subTreeString = subTrees[0].toString();
                    subTreeString = subTreeString.replace("\n", "\n|   ");
                    subTreeString = "|   " + subTreeString;
                    subTreeString.replaceAll("\n", "\n|   ");
                    // the last line will have "|   ", so remove the last line.
                    //subTreeString = subTreeString.substring(0, subTreeString.lastIndexOf('\n'));
                    treeString += subTreeString;
                }
            } else if (rootAttribute.isNominal()) {
                for (int i = 0; i < subTrees.length; i++) {
                    if (i > 0) {
                        treeString += "\n";
                    }
                    treeString += "";
                    treeString += rootAttribute.name();
                    treeString += " ";
                    treeString += "=";
                    treeString += " ";
                    treeString += rootAttribute.value(i);

                    if (subTrees[i].measureNumLeaves() == 1) {
                        treeString += ": ";
                        treeString += splitsMajorityValueNames[i];
                        treeString += " {";
                        // count the remaining support
                        int remainingSupport = 0;
                        for (int j = 0; j < splitsClassDist[i].length; j++) {
                            treeString += classNames[j] + "," + splitsClassDist[i][j] + ";";
                            if (j == splitsMajorityIndexes[i]) {
                                continue;
                            }
                            remainingSupport += splitsClassDist[i][j];
                        }
                        treeString += "} (";
                        treeString += (splitsClassDist[i][splitsMajorityIndexes[i]] + remainingSupport);
                        treeString += "/";
                        treeString += remainingSupport;
                        treeString += ")";
                    } else {
                        treeString += "\n";
                        String subTreeString = subTrees[i].toString();
                        subTreeString = subTreeString.replace("\n", "\n|   ");
                        subTreeString = "|   " + subTreeString;
                        // the last line will have "|   ", so remove the last line.
                        //subTreeString = subTreeString.substring(0, subTreeString.lastIndexOf('\n'));
                        treeString += subTreeString;
                    }
                }
            }

            return treeString;
        }

    }
    
    /*
     * Splits a given set of instances into multiple instance based on a split
     * attribute. A split value is also used if the split attribute is
     * numerical.
     *
     * @param data the instances to split
     * @param splitPoint the object containing the split attribute and split
     * value. The split value is not used if the attribute is nominal.
     * @return an array of the data splits.
     */
    private Instances[] splitData(Instances data, GoodAttribute splitPoint) {
        Instances[] dataSplits = null;
        int numBags = -1;

        Attribute splitAttribute = splitPoint.getAttribute();
        double splitValue = splitPoint.getSplitPoint();
        if (splitAttribute.isNumeric()) {
            numBags = 2;
            dataSplits = new Instances[numBags];
            dataSplits[0] = new Instances(data, 0);
            dataSplits[1] = new Instances(data, 0);
            Enumeration eData = data.enumerateInstances();
            while (eData.hasMoreElements()) {
                Instance currentInstance = (Instance) eData.nextElement();
                double currentValue = currentInstance.value(splitAttribute);
                if (currentValue > splitValue) {
                    dataSplits[0].add(currentInstance);
                } else {
                    dataSplits[1].add(currentInstance);
                }
            }
        } else if (splitAttribute.isNominal()) {
            numBags = splitAttribute.numValues();

            dataSplits = new Instances[numBags];
            for (int i = 0; i < numBags; i++) {
                dataSplits[i] = new Instances(data, 0);
            }

            Enumeration eData = data.enumerateInstances();
            while (eData.hasMoreElements()) {
                Instance currentInstance = (Instance) eData.nextElement();
                double currentValue = currentInstance.value(splitAttribute);

                dataSplits[(int) currentValue].add(currentInstance);
            }
        }

        return dataSplits;
    }
    
    /*
     * Given an enumeration e, return a sorted enumeration from lowest to
     * highest
     *
     * @param e the enumeration to be sorted
     * @return a sorted enumeration
     */
    private Enumeration sortEnumeration(Enumeration e) {
        // Convert the Enumeration to a list, then sort the list.
        List<Double> values = Collections.list(e);
        Collections.sort(values);

        // Now convert the list back into an Enumeration object.
        Enumeration<Double> returnValue = new Vector(values).elements();
        return returnValue;
    }
    
    /*
     * Returns an instance of a TechnicalInformation object, containing detailed
     * information about the technical background of this class, e.g., paper
     * reference or book this class is based on.
     *
     * @return the technical information about this class
     */
    private TechnicalInformation getTechnicalInformation() {
        TechnicalInformation result;

        result = new TechnicalInformation(Type.CONFERENCE);
        result.setValue(Field.AUTHOR, "Islam & Giggins");
        result.setValue(Field.YEAR, "2011");
        result.setValue(Field.TITLE, "Knowledge Discovery through SysFor - a Systematically Developed Forest of Multiple Decision Trees");
        result.setValue(Field.BOOKTITLE, "Australasian Data Mining Conference (AusDM 11)");
        result.setValue(Field.EDITOR, "Editor");
        result.setValue(Field.PUBLISHER, "ACS");
        result.setValue(Field.SERIES, "CRPIT");
        result.setValue(Field.ADDRESS, "Ballarat, Australia");
        result.setValue(Field.VOLUME, "121");
        result.setValue(Field.PAGES, "195-204");
        result.setValue(Field.URL, "http://crpit.com/confpapers/CRPITV121Islam.pdf");

        return result;

    }

    @Override
    public String toString() {

        if (forest == null) {
            return "No Forest Built!";
        }

        String forestString = "";

        for (int i = 0; i < forest.size(); i++) {
            if (i != 0) {
                forestString += "\n";
            }
            forestString += "Tree " + (i + 1) + ": \n";
            forestString += forest.get(i);
            forestString += "\n";
        }

        return forestString;
    }
    
    /*
     * Returns an enumeration describing the available options.
     *
     * Valid options are:
     * <p>
     *
     * -L &lt;minimum records in leaf&gt; <br>
     * Set minimum number of records for a leaf. (default 10)
     * <p>
     *
     * -N &lt;no. trees&gt; <br>
     * Set number of trees to build. (default 60)
     * <p>
     *
     * -G &lt;goodness threshold&gt; <br>
     * Set goodness threshold for attribute selection. (default 0.3)
     * <p>
     *
     * -S &lt;separation threshold&gt; <br>
     * Set separation threshold for split point selection. (default 0.3)
     * <p>
     *
     * -C &lt;confidence factor&gt; <br>
     * Set confidence for pruning. (default 0.25)
     * <p>
     *
     * @return an enumeration of all the available options.
     */
    @Override
    public Enumeration<Option> listOptions() {

        Vector<Option> newVector = new Vector<Option>(13);

        newVector.addElement(new Option("\tSet minimum number of records for a leaf.\n"
                + "\t(default 10)", "L", 1, "-L"));
        newVector.addElement(new Option("\tSet number of trees to build.\n"
                + "\t(default 60)", "N", 1, "-N"));
        newVector.addElement(new Option("\tSet goodness threshold for attribute selection.\n"
                + "\t(default 0.3)", "G", 1, "-G"));
        newVector.addElement(new Option("\tSet separation threshold for split point selection.\n"
                + "\t(default 0.3)", "S", 1, "-S"));
        newVector.addElement(new Option("\tSet confidence for pruning.\n"
                + "\t(default 0.25)", "C", 1, "-C"));

        newVector.addAll(Collections.list(super.listOptions()));

        return newVector.elements();
    }

}
