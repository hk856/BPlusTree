import java.util.AbstractMap;
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
					tree.root = children.get(i);
					return tree.search(key);
				}
			}
			if (target.keys.get(target.keys.size()-1).compareTo(key)<0){
				//if key is larger than every key in the list
				BPlusTree<K, T> tree = new BPlusTree<K, T>();
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
			LeafNode target = (LeafNode) root;
			target.insertSorted(key, value);
			if (target.isOverflowed()) {//root leaf node overflowed
				Entry<K, Node<K, T>> splitedNode = splitLeafNode(target);//TODO: might need more input
				return splitedNode;
			}
		} else { // root is link node
			IndexNode<K, T> target = (IndexNode) root; //target is the node we are visiting
			ArrayList<Node<K,T>> children = target.children;
			for (int i = 0; i < target.keys.size(); i++){ //for every child of target
				if (target.keys.get(i).compareTo(key)>0){ //if the key of the child node is first time bigger than the insert key
														// that child node should be the (grand)parent of the insert node
					BPlusTree<K, T> tree = new BPlusTree<K, T>();
					Node<K,T> nextTarget = children.get(i);
					tree.root = nextTarget;
					Entry<K, Node<K, T>> splitedPoint = tree.insert(key, value);
					if(splitedPoint != null){ //back to the target level
						//the "nextTarget" will have a shorter children list
						//the Key in the splitedPoint is a new index node after the "nextTarget" node
						//add the splitedPoint to target children. if children overflow, split this index node		
						
						target.insertSorted(splitedPoint, i);
						if(target.isOverflowed()){//if adding the right splited node will make target overflow
							Entry<K, Node<K, T>> splitedNode = splitIndexNode(target);//TODO: might need more input
							return splitedNode;
						}else{
							return null;
						}
						
					}else{
						return null;
					}
				}
			}
			if (target.keys.get(target.keys.size()-1).compareTo(key)<0){
				//last child is the (grand)parent
				BPlusTree<K, T> tree = new BPlusTree<K, T>();
				Node<K,T> nextTarget = children.get(target.keys.size());
				tree.root = nextTarget;
				Entry<K, Node<K, T>> splitedPoint = tree.insert(key, value);
				if(splitedPoint != null){ //back to the target level
					//the "nextTarget" will have a shorter children list
					//the Key in the splitedPoint is a new index node after the "nextTarget" node
					//add the splitedPoint to target children. if children overflow, split this index node		
					
					target.insertSorted(splitedPoint, target.keys.size());
					if(target.isOverflowed()){//if adding the right splited node will make target overflow
						Entry<K, Node<K, T>> splitedNode = splitIndexNode(target);//TODO: might need more input
						return splitedNode;
					}else{
						return null;
					}
					
				}else{
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

		return null;
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

		return null;
	}

	/**
	 * TODO Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(K key) {

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
		return -1;

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
		return -1;
	}

}
