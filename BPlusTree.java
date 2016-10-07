import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.sun.xml.internal.ws.api.ComponentFeature.Target;

/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative TODO: Rename
 * to BPlusTree
 */
public class BPlusTree<K extends Comparable<K>, T> {

	public Node<K, T> root;
	public static final int D = 2;
	public boolean isRealTree = true;

	/**
	 * TODO Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public T search(K key) {
		if (root == null)
			return null;
		if (root.isLeafNode) {
			LeafNode<K, T> target = (LeafNode) root;
			for (int i = 0; i < target.keys.size(); i++) {
				if (target.keys.get(i) == key) {
					return target.values.get(i);
				}
			}
		} else {
			IndexNode<K, T> target = (IndexNode) root;
			ArrayList<Node<K, T>> children = target.children;
			for (int i = 0; i < target.keys.size(); i++) {
				if (target.keys.get(i).compareTo(key) > 0) {
					BPlusTree<K, T> tree = new BPlusTree<K, T>();
					tree.isRealTree = false;
					tree.root = children.get(i);
					return tree.search(key);
				}
			}
			if (target.keys.get(target.keys.size() - 1).compareTo(key) < 0) {
				// if key is larger than every key in the list
				BPlusTree<K, T> tree = new BPlusTree<K, T>();
				tree.isRealTree = false;
				tree.root = children.get(target.keys.size());
				return tree.search(key);
			}
		}
		return null;
	}

	/**
	 * TODO Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public Entry<K, Node<K, T>> insert(K key, T value) {

		if (root == null) {
			root = new LeafNode(key, value);

		} else if (root.isLeafNode) {
			LeafNode<K, T> target = (LeafNode) root;
			target.insertSorted(key, value);
			if (target.isOverflowed()) {// root leaf node overflowed
				Entry<K, Node<K, T>> splitedNode = splitLeafNode(target);// TODO:
																			// might
																			// need
																			// more
																			// input
				if (this.isRealTree) {
					K newkey = splitedNode.getKey();
					LeafNode<K, T> rightNode = (LeafNode<K, T>) splitedNode.getValue();// here
					root = new IndexNode<K, T>(newkey, target, rightNode);

				}
				return splitedNode;
			}
		} else { // root is link node
			IndexNode<K, T> target = (IndexNode) root; // target is the node we
														// are visiting
			ArrayList<Node<K, T>> children = target.children;
			for (int i = 0; i < target.keys.size(); i++) { // for every child of
															// target
				if (target.keys.get(i).compareTo(key) > 0) { // if the key of
																// the child
																// node is first
																// time bigger
																// than the
																// insert key
																// that child
																// node should
																// be the
																// (grand)parent
																// of the insert
																// node
					BPlusTree<K, T> tree = new BPlusTree<K, T>();
					tree.isRealTree = false;
					Node<K, T> nextTarget = children.get(i);
					tree.root = nextTarget;
					Entry<K, Node<K, T>> splitedPoint = tree.insert(key, value);
					if (splitedPoint != null) { // back to the target level
						// the "nextTarget" will have a shorter children list
						// the Key in the splitedPoint is a new index node after
						// the "nextTarget" node
						// add the splitedPoint to target children. if children
						// overflow, split this index node

						target.insertSorted(splitedPoint, i);
						if (target.isOverflowed()) {// if adding the right
													// splited node will make
													// target overflow
							Entry<K, Node<K, T>> splitedNode = splitIndexNode(target);// TODO:
																						// might
																						// need
																						// more
																						// input
							if (this.isRealTree) {
								K newkey = splitedNode.getKey();
								IndexNode<K, T> rightNode = (IndexNode<K, T>) splitedNode.getValue();
								root = new IndexNode<K, T>(newkey, target, rightNode);

							}
							return splitedNode;
						} else {
							return null;
						}

					} else {
						return null;
					}
				}
			}
			if (target.keys.get(target.keys.size() - 1).compareTo(key) < 0) {
				// last child is the (grand)parent
				BPlusTree<K, T> tree = new BPlusTree<K, T>();
				tree.isRealTree = false;
				Node<K, T> nextTarget = children.get(target.keys.size());
				tree.root = nextTarget;
				Entry<K, Node<K, T>> splitedPoint = tree.insert(key, value);
				if (splitedPoint != null) { // back to the target level
					// the "nextTarget" will have a shorter children list
					// the Key in the splitedPoint is a new index node after the
					// "nextTarget" node
					// add the splitedPoint to target children. if children
					// overflow, split this index node

					target.insertSorted(splitedPoint, target.keys.size());
					if (target.isOverflowed()) {// if adding the right splited
												// node will make target
												// overflow
						Entry<K, Node<K, T>> splitedNode = splitIndexNode(target);// TODO:
																					// might
																					// need
																					// more
																					// input
						if (this.isRealTree) {
							K newkey = splitedNode.getKey();
							IndexNode<K, T> rightNode = (IndexNode<K, T>) splitedNode.getValue();
							root = new IndexNode<K, T>(newkey, target, rightNode);

						}
						return splitedNode;
					} else {
						return null;
					}

				} else {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * TODO Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf,
	 *            any other relevant data
	 * @return the key/node pair as an Entry
	 */
	public Entry<K, Node<K, T>> splitLeafNode(LeafNode<K, T> leaf) { // param...

		ArrayList<K> newKeys = new ArrayList<K>();
		ArrayList<T> newValues = new ArrayList<T>();
		for (int i = D; i < leaf.keys.size(); i++) {
			newKeys.add(leaf.keys.get(i));
			newValues.add(leaf.values.get(i));
		}
		LeafNode<K, T> newLeafNode = new LeafNode<K, T>(newKeys, newValues);
		newLeafNode.nextLeaf = leaf.nextLeaf;
		if (newLeafNode.nextLeaf != null)
			newLeafNode.nextLeaf.previousLeaf = newLeafNode;
		leaf.nextLeaf = newLeafNode;
		newLeafNode.previousLeaf = leaf;

		for (int i = D; i < leaf.keys.size(); i++) {
			leaf.keys.remove(i);
			leaf.values.remove(i);
		}

		Entry<K, Node<K, T>> splitedNode = new NodeEntry<K, Node<K, T>>((K) newLeafNode.keys.get(0), newLeafNode);
		return splitedNode;

	}

	/**
	 * TODO split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index,
	 *            any other relevant data
	 * @return new key/node pair as an Entry
	 */
	public Entry<K, Node<K, T>> splitIndexNode(IndexNode<K, T> index) { // param
		ArrayList<K> newKeys = new ArrayList<K>();
		ArrayList<Node<K, T>> newChildren = new ArrayList<Node<K, T>>();

		K key = index.keys.get(D);

		for (int i = D + 1; i < index.keys.size(); i++) {
			newKeys.add(index.keys.get(i));
			newChildren.add(index.children.get(i));
		}
		newChildren.add(index.children.get(index.keys.size()));

		for (int i = D; i < index.keys.size(); i++) {
			index.keys.remove(i);
			index.children.remove(i + 1);
		}
		index.children.remove(index.children.get(index.keys.size()));

		IndexNode<K, T> newIndexNode = new IndexNode<K, T>(newKeys, newChildren);

		Entry<K, Node<K, T>> splitedNode = new NodeEntry<K, Node<K, T>>(key, newIndexNode);

		return splitedNode;
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(K key) {
		if (root == null) {
			return;
		}
		if (root.isLeafNode && isRealTree) {
			LeafNode<K, T> target = (LeafNode<K, T>) root;
			int index = target.keys.indexOf(key);
			if (index == -1) {
				System.out.println("key not found!!");
			} else {
				target.keys.remove(index);
				target.values.remove(index);
			}
		} else if (root.isLeafNode && !isRealTree) { //bottom level leaf
			LeafNode<K, T> target = (LeafNode<K, T>) root;
			int index = target.keys.indexOf(key);
			if (index == -1) {
				System.out.println("key not found!!");
			} else {
				target.keys.remove(index);
				target.values.remove(index);
				if (target.isUnderflowed()) {
					int underIndex = 0;
					if (target.previousLeaf == null) {
						underIndex = handleLeafNodeUnderflow(target, target.nextLeaf, target.parentNode);
					} else if (target.nextLeaf == null) {
						underIndex = handleLeafNodeUnderflow(target.previousLeaf, target, target.parentNode);
					} else if (target.previousLeaf.keys.size() <= target.nextLeaf.keys.size()) {
						underIndex = handleLeafNodeUnderflow(target, target.nextLeaf, target.parentNode);
					} else if (target.previousLeaf.keys.size() > target.nextLeaf.keys.size()) {
						underIndex = handleLeafNodeUnderflow(target.previousLeaf, target, target.parentNode);
					}
					if (underIndex == -1){
						
					}
				
				}
			}
		} else { //root is an index node
			
		}
	}

	/**
	 * TODO Handle LeafNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleLeafNodeUnderflow(LeafNode<K, T> left, LeafNode<K, T> right, IndexNode<K, T> parent) {
		// merge left and right when both of them are underflow
		if (left.keys.size() + right.keys.size() < 2 * D) {
			for (int i = 0; i < right.keys.size(); i++) {
				right.insertSorted(left.keys.get(i), left.values.get(i));
			}
			if (left.previousLeaf != null) { // changes previousLeaf of merged
												// node if left has one
				left.previousLeaf.nextLeaf = right;
				right.previousLeaf = left.previousLeaf;
			}
			int key = parent.children.indexOf(right) - 1;

			return key;
		} else {
			// left has fewer node, borrow the head of right
			if (left.keys.size() < right.keys.size()) {
				left.insertSorted(right.keys.get(0), right.values.get(0));
				right.keys.remove(0);
				right.values.remove(0);

			} else { // right has fewer node, borrow the tail of left
	            // only leave D elements in left node
	            int sizeofLeft = left.keys.size();
	            for (int i=0; i < sizeofLeft - D; i++){
	                right.insertSorted(left.keys.get(D), left.values.get(D));
	                left.keys.remove(D);
	                left.values.remove(D);
	            }
	            
			}
			int key = parent.children.indexOf(right) - 1;
			parent.keys.remove(key);
			parent.keys.add(right.keys.get(0));
			return -1;

		}
	}

	/**
	 * TODO Handle IndexNode Underflow (merge or redistribution)
	 * 
	 * @param left
	 *            : the smaller node
	 * @param right
	 *            : the bigger node
	 * @param parent
	 *            : their parent index node
	 * @return the splitkey position in parent if merged so that parent can
	 *         delete the splitkey later on. -1 otherwise
	 */
	public int handleIndexNodeUnderflow(IndexNode<K, T> leftIndex, IndexNode<K, T> rightIndex, IndexNode<K, T> parent) {
		
		//merge case
		if(leftIndex.keys.size() + rightIndex.keys.size() < 2*D){
			// add each child from leftIndex to rightIndex
			// set child parent to rightIndex
			for(int i = leftIndex.children.size() -1; i >=0; i--){
				rightIndex.children.add(0, leftIndex.children.get(i));
				leftIndex.children.get(i).parentNode = rightIndex;
			}
			
			int key = parent.children.indexOf(leftIndex);
	        K splitKey = parent.keys.get(key);
	        rightIndex.keys.add(0, splitKey);
	        
	        // add keys from left to the right
			for(int i =leftIndex.keys.size()-1; i >= 0; i--) {
				rightIndex.keys.add(0, leftIndex.keys.get(i));	
			}
			return key;
		
		}
		else{
			if(leftIndex.keys.size() > rightIndex.keys.size()){
				//move left children to the right until D keys in the left
				for(int i = D + 1; i <= leftIndex.children.size(); i++){
					rightIndex.children.add(0, leftIndex.children.get(leftIndex.children.size()-1));
					leftIndex.children.get(leftIndex.children.size()-1).parentNode = rightIndex;
					
					leftIndex.children.remove(leftIndex.children.size()-1);
				}
				
				int key = parent.children.indexOf(leftIndex);
		        K splitKey = parent.keys.get(key);
		        rightIndex.keys.add(0, splitKey);
		        parent.keys.remove(key);
	            parent.keys.add(key, splitKey);
	            
	            // move keys to right node
	            for (int i = leftIndex.keys.size() - 1; i >= D+1; i--){
	                K newkey = leftIndex.keys.get(i);
	                rightIndex.keys.add(0, newkey);
	            }
	            // remove keys in left node
	            for (int i = leftIndex.keys.size() - 1; i>=D; i--){
	                leftIndex.keys.remove(D);
	            }

				
				
			}
			
			return -1;
		}
		
		
		
	}

}
