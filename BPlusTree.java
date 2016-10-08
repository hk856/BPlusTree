import java.util.ArrayList;
import java.util.Map.Entry;

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
			LeafNode<K, T> target = (LeafNode<K, T>) root;
			for (int i = 0; i < target.keys.size(); i++) {
				if (target.keys.get(i) == key) {
					return target.values.get(i);
				}
			}
		} else {
			IndexNode<K, T> target = (IndexNode<K, T>) root;
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
			root = new LeafNode<K, T>(key, value);
			root.myTree=this;
		} else if (root.isLeafNode) {
			LeafNode<K, T> target = (LeafNode<K, T>) root;
//			System.out.println(root.myTree);

			target.insertSorted(key, value);
			if (target.isOverflowed()) {// root leaf node overflowed
				Entry<K, Node<K, T>> splitedNode = splitLeafNode(target);
				if (this.isRealTree) {
					K newkey = splitedNode.getKey();
					LeafNode<K, T> rightNode = (LeafNode<K, T>) splitedNode.getValue();// here
					root = new IndexNode<K, T>(newkey, target, rightNode);
					root.myTree = target.myTree;
					target.parentNode = (IndexNode<K, T>) root;
					rightNode.parentNode = (IndexNode<K, T>) root;
				}
				return splitedNode;
			}
		} else { // root is link node
			IndexNode<K, T> target = (IndexNode<K, T>) root; // target is the
																// node we
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
							Entry<K, Node<K, T>> splitedNode = splitIndexNode(target);
							if (this.isRealTree) {
								K newkey = splitedNode.getKey();
								IndexNode<K, T> rightNode = (IndexNode<K, T>) splitedNode.getValue();
								rightNode.myTree = target.myTree;
								root = new IndexNode<K, T>(newkey, target, rightNode);
								root.myTree = target.myTree;
								target.parentNode = (IndexNode<K, T>) root;
								rightNode.parentNode = (IndexNode<K, T>) root;
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
						Entry<K, Node<K, T>> splitedNode = splitIndexNode(target);
						if (this.isRealTree) {
							K newkey = splitedNode.getKey();
							IndexNode<K, T> rightNode = (IndexNode<K, T>) splitedNode.getValue();
							rightNode.myTree = target.myTree;
							root = new IndexNode<K, T>(newkey, target, rightNode);
							root.myTree = target.myTree;
							target.parentNode = (IndexNode<K, T>) root;
							rightNode.parentNode = (IndexNode<K, T>) root;

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

		if (newLeafNode.nextLeaf != null)
			newLeafNode.nextLeaf.previousLeaf = newLeafNode;
		leaf.nextLeaf = newLeafNode;
		newLeafNode.previousLeaf = leaf;

		while (leaf.keys.size() > D) {
			leaf.keys.remove(D);
			leaf.values.remove(D);
		}
		newLeafNode.myTree = leaf.myTree;
		
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

		while (D < index.keys.size()) {
			index.keys.remove(D);
			index.children.remove(D + 1);// TODO critical
		}

		IndexNode<K, T> newIndexNode = new IndexNode<K, T>(newKeys, newChildren);
		newIndexNode.myTree = index.myTree;
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
		} else if (root.isLeafNode && !isRealTree) { // bottom level leaf
			LeafNode<K, T> target = (LeafNode<K, T>) root;
			int index = target.keys.indexOf(key);
			if (index == -1) {
				System.out.println("key not found!!");
			} else {
				target.keys.remove(index);
				target.values.remove(index);
				if (target.isUnderflowed()) {
					underFlowBubbleUp(target);
				}
			}
		} else { // root is an index node
			IndexNode<K, T> target = (IndexNode<K, T>) root;
			ArrayList<Node<K, T>> children = target.children;
			if (target.keys.get(target.keys.size() - 1).compareTo(key) < 0) {
				BPlusTree<K, T> tree = new BPlusTree<K, T>();
				tree.isRealTree = false;
				Node<K, T> nextTarget = children.get(target.keys.size() - 1);
				tree.root = nextTarget;
				tree.delete(key);
				return;
			} else {
				for (int i = 0; i < target.keys.size(); i++) {
					if (target.keys.get(i).compareTo(key) > 0) {
						BPlusTree<K, T> tree = new BPlusTree<K, T>();
						tree.isRealTree = false;
						Node<K, T> nextTarget = children.get(i);
						tree.root = nextTarget;
						tree.delete(key);
						return;
					}
				}
			}

		}
	}

	public void underFlowBubbleUp(Node<K, T> target) {

		if (target.isLeafNode) {
			LeafNode<K, T> leafTarget = (LeafNode<K, T>) target;
			int leafUnderFlow = 0;
			LeafNode<K, T> leftNode = null;
			LeafNode<K, T> rightNode = null;
			IndexNode<K, T> parent = target.parentNode;
			if (leafTarget.previousLeaf == null) {
				leftNode = leafTarget;
				rightNode = leafTarget.nextLeaf;
			} else if (leafTarget.nextLeaf == null) {
				leftNode = leafTarget.previousLeaf;
				rightNode = leafTarget;
			} else if (leafTarget.previousLeaf.keys.size() <= leafTarget.nextLeaf.keys.size()) {
				leftNode = leafTarget;
				rightNode = leafTarget.nextLeaf;
			} else if (leafTarget.previousLeaf.keys.size() > leafTarget.nextLeaf.keys.size()) {
				leftNode = leafTarget.previousLeaf;
				rightNode = leafTarget;
			}
			leafUnderFlow = handleLeafNodeUnderflow(leftNode, rightNode, parent);

			if (leafUnderFlow == -1) {
				int locationIndex = parent.children.indexOf(rightNode) - 1;
				System.out.println("location index is: " + locationIndex);
				parent.keys.remove(locationIndex);
				parent.keys.add(locationIndex, rightNode.keys.get(0));

			} else {
				parent.keys.remove(leafUnderFlow);
				if (parent.isUnderflowed()) {

					underFlowBubbleUp(parent);
				}
			}
		} else {// target is index node
			IndexNode<K, T> indexTarget = (IndexNode<K, T>) target;
			if (indexTarget.keys.size()==0){
//				System.out.println(indexTarget.myTree);
//						indexTarget.children.get(0);
				return;
			}
			IndexNode<K, T> parent = indexTarget.parentNode;
			if (parent == null) {
				return;
			} else {
				int thisIndex = parent.children.indexOf(target);
				IndexNode<K, T> left = null;
				IndexNode<K, T> right = null;
				if (thisIndex == 0) {
					left = (IndexNode<K, T>) parent.children.get(thisIndex);
					right = (IndexNode<K, T>) parent.children.get(thisIndex + 1);
				} else if (thisIndex == parent.children.size() - 1) {
					left = (IndexNode<K, T>) parent.children.get(thisIndex - 1);
					right = (IndexNode<K, T>) parent.children.get(thisIndex);
				} else {
					IndexNode<K, T> leftIndex = (IndexNode<K, T>) parent.children.get(thisIndex - 1);
					IndexNode<K, T> rightIndex = (IndexNode<K, T>) parent.children.get(thisIndex + 1);
					if (leftIndex.keys.size() <= rightIndex.keys.size()) {
						left = indexTarget;
						right = rightIndex;
					} else if (leftIndex.keys.size() > rightIndex.keys.size()) {
						left = leftIndex;
						right = indexTarget;
					}
				}

				int indexUnderFlow = handleIndexNodeUnderflow(left, right, parent);

				if (indexUnderFlow == -1) {
					int locationIndex = parent.children.indexOf(right) - 1;
					parent.keys.remove(locationIndex);
					parent.keys.add(locationIndex, right.keys.get(0));
				} else {
					parent.keys.remove(indexUnderFlow);
					
					if (parent.isUnderflowed()) {

						underFlowBubbleUp(parent);

					}

				}
			}
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
			for (int i = 0; i < left.keys.size(); i++) {
				right.insertSorted(left.keys.get(i), left.values.get(i));
			}
			right.previousLeaf = left.previousLeaf;
			if (left.previousLeaf != null) { // changes previousLeaf of merged
												// // node if left has one
				left.previousLeaf.nextLeaf = right;
			}

			int leftPosition = parent.children.indexOf(left);
			parent.children.remove(leftPosition);

			int key = parent.children.indexOf(right);

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
				for (int i = 0; i < sizeofLeft - D; i++) {
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

		// merge case
		// move everything from left to right: children + keys
		if (leftIndex.keys.size() + rightIndex.keys.size() < 2 * D) {
			// add each child from leftIndex to rightIndex
			// set child parent to rightIndex
			for (int i = leftIndex.children.size() - 1; i >= 0; i--) {
				Node<K, T> child = leftIndex.children.get(i);
				rightIndex.children.add(0, child);
				child.parentNode = rightIndex;
			}

			int key = parent.children.indexOf(leftIndex);
			K splitKey = parent.keys.get(key);
			rightIndex.keys.add(0, splitKey);

			// add keys from left to the right
			for (int i = leftIndex.keys.size() - 1; i >= 0; i--) {
				rightIndex.keys.add(0, leftIndex.keys.get(i));
			}
			parent.children.remove(parent.children.indexOf(leftIndex));
			return key;

		} else {
			// when left is bigger, borrow left to right
			// children = keys + 1
			if (leftIndex.keys.size() > rightIndex.keys.size()) {
				// move left children to the right until D keys in the left, D+1
				// children left
				for (int i = D + 1; i < leftIndex.children.size(); i++) {
					Node<K, T> child = leftIndex.children.get(leftIndex.children.size() - 1);

					rightIndex.children.add(0, child);
					child.parentNode = rightIndex;
					leftIndex.children.remove(leftIndex.children.size() - 1);
				}

				// restructure parent node
				// get old split key in parent and add it to the rightIndex
				// use the last key in leftIndex to be the new split key in
				// parent
				int key = parent.children.indexOf(leftIndex);
				K splitKey = parent.keys.get(key);
				rightIndex.keys.add(0, splitKey);
				K newSplitKey = leftIndex.keys.get(D - 1);
				parent.keys.remove(key);
				parent.keys.add(key, newSplitKey);

				// move rest keys from left to right node until D
				for (int i = leftIndex.keys.size() - 1; i >= D; i--) {
					K newkey = leftIndex.keys.get(i);
					rightIndex.keys.add(0, newkey);
					leftIndex.keys.remove(i);
				}
			} else {
				// when right is bigger, borrow right to left
				// until left has D keys
				while (leftIndex.keys.size() < D) {
					// move parent key to left, then move right[0] to be the new
					// parent
					int key = parent.children.indexOf(leftIndex);
					K oldSplitKey = parent.keys.get(key);
					K newSplitKey = rightIndex.keys.get(0);
					leftIndex.keys.add(oldSplitKey);

					// move child
					Node<K, T> child = rightIndex.children.get(0);
					leftIndex.children.add(child);
					child.parentNode = leftIndex;
					rightIndex.children.remove(0);

					// move key
					parent.keys.remove(key);
					parent.keys.add(key, newSplitKey);
					rightIndex.keys.remove(0);
				}

			}

			return -1;
		}

	}

}
