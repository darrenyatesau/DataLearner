/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    PredictionNode.java
 *    Copyright (C) 2001 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.trees.adtreets;

import weka.classifiers.trees.ADTreeTS;
import weka.classifiers.trees.adtreets.SplitterTS;
import weka.core.FastVector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Class representing a prediction node in an alternating tree.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 1.7 $
 */
public final class PredictionNodeTS
  implements Serializable, Cloneable, RevisionHandler {

  /** for serialization */
  private static final long serialVersionUID = 6018958856358698814L;

  /** The prediction value stored in this node */
  private double value;

  /** The children of this node - any number of splitter nodes */
  private FastVector children;
  
  /**
   * Creates a new prediction node.
   *
   * @param newValue the value that the node should store
   */
  public PredictionNodeTS(double newValue) {

    value = newValue;
    children = new FastVector();  
  }

  /**
   * Sets the prediction value of the node.
   *
   * @param newValue the value that the node should store
   */
  public final void setValue(double newValue) {

    value = newValue;
  }

  /**
   * Gets the prediction value of the node.
   *
   * @return the value stored in the node
   */
  public final double getValue() {

    return value;
  }

  /**
   * Gets the children of this node.
   *
   * @return a FastVector containing child Splitter object references
   */ 
  public final FastVector getChildren() {

    return children;
  }

  /**
   * Enumerates the children of this node.
   *
   * @return an enumeration of child Splitter object references
   */ 
  public final Enumeration children() {

    return children.elements();
  }
  
  /**
   * Adds a child to this node. If possible will merge, and will perform a deep copy
   * of the child tree.
   *
   * @param newChild the new child to add (will be cloned)
   * @param addingTo the tree that this node belongs to
   */
  public final void addChild(SplitterTS newChild, ADTreeTS addingTo) {

    // search for an equivalent child
    SplitterTS oldEqual = null;
    for (Enumeration e = children(); e.hasMoreElements(); ) {
      SplitterTS split = (SplitterTS) e.nextElement();
      if (newChild.equalTo(split)) { oldEqual = split; break; }
    }
    if (oldEqual == null) { // didn't find one so just add
      SplitterTS addChild = (SplitterTS) newChild.clone();
      setOrderAddedSubtree(addChild, addingTo);
      children.addElement(addChild);
    }
    else { // found one, so do a merge
      for (int i=0; i<newChild.getNumOfBranches(); i++) {
	PredictionNodeTS oldPred = oldEqual.getChildForBranch(i);
	PredictionNodeTS newPred = newChild.getChildForBranch(i);
	if (oldPred != null && newPred != null)
	  oldPred.merge(newPred, addingTo);
      }
    }
  }

  /**
   * Clones this node. Performs a deep copy, recursing through the tree.
   *
   * @return a clone
   */ 
  public final Object clone() {

    PredictionNodeTS clone = new PredictionNodeTS(value);
    for (Enumeration e = children.elements(); e.hasMoreElements(); )
      clone.children.addElement(((SplitterTS) e.nextElement()).clone());
    return clone;
  }

  /**
   * Merges this node with another.
   *
   * @param merger the node that is merging with this node - will not be affected,
   * will instead be cloned
   * @param mergingTo the tree that this node belongs to 
   */ 
  public final void merge(PredictionNodeTS merger, ADTreeTS mergingTo) {

    value += merger.value;
    for (Enumeration e = merger.children(); e.hasMoreElements(); ) {
      addChild((SplitterTS)e.nextElement(), mergingTo);
    }
  }

  /**
   * Sets the order added values of the subtree rooted at this splitter node.
   *
   * @param addChild the root of the subtree
   * @param addingTo the tree that this node will belong to
   */
  private void setOrderAddedSubtree(SplitterTS addChild, ADTreeTS addingTo) {

    addChild.orderAdded = addingTo.nextSplitAddedOrder();
    for (int i=0; i<addChild.getNumOfBranches(); i++) {
      PredictionNodeTS node = addChild.getChildForBranch(i);
      if (node != null)
	for (Enumeration e = node.children(); e.hasMoreElements(); )
	  setOrderAddedSubtree((SplitterTS) e.nextElement(), addingTo);
    }
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}
