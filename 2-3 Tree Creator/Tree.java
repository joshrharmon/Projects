import java.util.*;

public class Tree {

	public class Node {
		private Node parent;
		
		// Array to hold node's children. [0] = left, [1] = mid, [2] = right
		private Node[] children = new Node[3];
		
		// Array to hold keys in node
		private Integer[] nodeData = new Integer[2];
		
		public Node(Integer left, Integer right, Node parent) {
			this.nodeData[0] = left;
			this.nodeData[1] = right;
			this.parent = parent;
		}
		
		// Utility methods
		public Integer getLeft() {
			return nodeData[0];
		}
		
		public Integer getRight() {
			return nodeData[1];
		}
		
		public void setParent(Node parent) {
			this.parent = parent;
		}
		
		public void setChild(int index, Node child) {
			this.children[index] = child;
		}

		public Node getChild(int index) {
			return this.children[index];
		}
		
		public boolean hasSpace() {
			if(nodeData[0] == null || nodeData[1] == null) 
				return true;
			else {
				return false;
			}
		}
		
		public void leafSort(int n1, int n2, Node parentRef) {
				// With this method, it is assumed that n1 is the left arg and n2 is the right arg
				if(n1 < n2)	{				// Already sorted 
					nodeData[0] = n1;
					nodeData[1] = n2;
					setParent(parentRef);
				}
				else {						// Not sorted, will swap values
					int temp = n1;
					nodeData[0] = n2;
					nodeData[1] = temp;
					setParent(parentRef);
				}
		}
		
		public Node getParent() {
			return parent;
		}

		public int getSize() {
			int size = 0;
			size += this.getNumVals();
			for(Node n: children) {
				if(n != null) 
					size += n.getSize();
			}
			return size;
		}

		public boolean isLeaf() {
			for(Node n: children) {
				if(n != null)
					return false;
			}
			return true;
		}
		
		public ArrayList<Integer> nodeSplit(int n1, int n2, int n3) {
			ArrayList<Integer> sorted = new ArrayList<Integer>();
			sorted.add(n1);		// Add all node Data to arraylist
			sorted.add(n2);
			sorted.add(n3);
			Collections.sort(sorted);
			return sorted;	
		}
		
		public boolean hasVal(int input) {
			for(Integer i: nodeData) {
				if(i != null && i == input)
					return true;
			}
			return false;
		}
	
		public int getNumVals() {
			int counter = 0;
			for(Integer i: nodeData) {
				if(i != null) { 
					counter++; 
				}
			}
			return counter;
		}
		
		public Node getChildOf(int input) {
			
			// If the node being looked at has one key
			if(this.getNumVals() == 1) {
				if(input < nodeData[0]) {	// If input is less than key, return the lesser child
					return children[0];		
				}
				if(input > nodeData[0])		// If the input is more than the key, return the greater child
					return children[2];
			}
			
			// If the node being looked at has two keys (and possibly a mid child)
			else if(this.getNumVals() == 2) {
				if(input < nodeData[0]) {
					return children[0];
				}
				else if(input > nodeData[1]) {
					return children[2];
				} else {
					return children[1];
				}
			} else {
				return null;
			}
			return null;
		}
	}
	
	public void nodeSplitterLeft(Node n, Node nodeMidParRise, Node nodeRightPar, Node nodeLeftPar, Node nodeLesser, Node nodeGreater, Node nodeMid) {
		nodeMidParRise.setChild(0, nodeLeftPar);	// Set new parent's left child to new lesser parent
		nodeMidParRise.setChild(2, nodeRightPar);	// Set new parent's left child to new greater parent
		
		nodeLeftPar.setChild(0, nodeLesser);	// Set new left parent's lesser child
		nodeLeftPar.setChild(2, nodeGreater);	// Set new right parent's greater child
		nodeLesser.setParent(nodeLeftPar);		// Set left child's parent to left parent
		nodeGreater.setParent(nodeLeftPar);		// Set right child's parent to left parent
		
		nodeRightPar.setChild(0, n.getParent().getChild(1));	// Take care of other split node
		nodeRightPar.setChild(2, n.getParent().getChild(2));
		n.getParent().getChild(1).setParent(nodeRightPar);
		n.getParent().getChild(2).setParent(nodeRightPar);
	}
	
	public void nodeSplitterRight(Node n, Node nodeMidParRise, Node nodeRightPar, Node nodeLeftPar, Node nodeLesser, Node nodeGreater, Node nodeMid) {
		nodeMidParRise.setChild(0, nodeLeftPar);	
		nodeMidParRise.setChild(2, nodeRightPar);	
		
		nodeRightPar.setChild(0, nodeLesser);	
		nodeRightPar.setChild(2, nodeGreater);	
		nodeLesser.setParent(nodeLeftPar);		
		nodeGreater.setParent(nodeLeftPar);		
		
		nodeLeftPar.setChild(0, n.getParent().getChild(1));	
		nodeLeftPar.setChild(2, n.getParent().getChild(2));
		n.getParent().getChild(1).setParent(nodeLeftPar);
		n.getParent().getChild(2).setParent(nodeLeftPar);
	}
	
	public void nodeSplitterMid(Node n, Node nodeMidParRise, Node nodeRightPar, Node nodeLeftPar, Node nodeLesser, Node nodeGreater, Node nodeMid) {
		
	}

	private Node root;
	public Tree() {
		root = null;
	}
	
	public boolean insert(int x) {
		// First test to see if the root is null
		Node n = root;
		if(n == null) {
			root = new Node(x, null, null);
			return true;
		}

		
		// See if it's adding a duplicate, if so, return false.
		n = findNode(x);
		if(n != null)
			return false;
		
		// Normal adding procedure
		n = root;
		while(!n.isLeaf()) {
			n = n.getChildOf(x);
		}
		
		// If we made it this far, we've found the appropriate leaf or all leaves are full
		if(n.hasSpace()) {
			n.leafSort(n.nodeData[0], x, n.getParent());	// Create new node with sorted keys and properly assigned parent
			return true;
		}
		
		// If the last node searched does not have space, start rearranging procedures
		else if(!n.hasSpace()) {
			
			// Create three new nodes to split
			Node nodeLesser 	= new Node(n.nodeSplit(n.nodeData[0], n.nodeData[1], x).get(0), null, null);	// Create new, lesser child
			Node nodeMid 		= new Node(n.nodeSplit(n.nodeData[0], n.nodeData[1], x).get(1), null, null); 	// Create new parent
			Node nodeGreater 	= new Node(n.nodeSplit(n.nodeData[0], n.nodeData[1], x).get(2), null, null);	// Create new, greater child
			
			// If there is no parent for the correct leaf, make first assignment procedures
			if(n.getParent() == null) {
				root = nodeMid;							// Set nodeMid as the new root;
				nodeMid.setChild(0, nodeLesser);		// Set new parent's lesser child
				nodeMid.setChild(2, nodeGreater);		// Set new parent's greater child
				nodeLesser.setParent(nodeMid);			// Set lesser child's parent to new mid
				nodeGreater.setParent(nodeMid);			// Set greater child's parent to new mid
				n = null;								// Kill off old node
				return true;
			}
			
			// If correct leaf has a parent with space, start rearranging procedures
			else if(n.getParent().hasSpace()) {
				
				n.getParent().nodeData[1] 				= nodeMid.getLeft();	// Rise middle value to the parent
				nodeMid 								= null;					// Kill no longer needed node
				nodeLesser.setParent(n.getParent());							// Assign correct parent to middle node
				nodeGreater.setParent(n.getParent());							// Assign correct parent to greater node
				n.getParent().children[1]				= nodeLesser;			// Set middle child to lesser key
				n.getParent().children[2]				= nodeGreater;			// Set greater child to greater key
				return true;				
			}
			
			// Do proper splits if parent does not have space
			else if(!n.getParent().hasSpace()) {
				Node nodeMidParRise = new Node (n.getParent().nodeSplit(n.getParent().nodeData[0], n.getParent().nodeData[1], x).get(1), null, null);			// Create new node that is the new parent of the split
				Node nodeLeftPar	= new Node (n.getParent().nodeSplit(n.getParent().nodeData[0], n.getParent().nodeData[1], x).get(0), null, nodeMidParRise);	// Create new node that is the split left parent
				Node nodeRightPar	= new Node (n.getParent().nodeSplit(n.getParent().nodeData[0], n.getParent().nodeData[1], x).get(2), null, nodeMidParRise);	// Create new node that is the split right parent
				
				// If the split occurs on the left sub-tree
				if(n == n.getParent().getChild(0)) {
					nodeSplitterLeft(n, nodeMidParRise, nodeRightPar, nodeLeftPar, nodeLesser, nodeGreater, nodeMid);
				}
				
				// If the split occurs on the middle sub-tree
				else if(n == n.getParent().getChild(1)) {
					nodeSplitterMid(n, nodeMidParRise, nodeRightPar, nodeLeftPar, nodeLesser, nodeGreater, nodeMid);
				} 
				
				// If the split occurs on the right sub-tree
				else {
					nodeSplitterRight(n, nodeMidParRise, nodeRightPar, nodeLeftPar, nodeLesser, nodeGreater, nodeMid);
				}
				
				// If the parent of the full leaf is the root, set the highest parent as the new root
				if(n.getParent() == root) {				
					root = nodeMidParRise;
				}
				
				// Kill old node
				n = null;
				return true;
			}
		}
		return false;
	}
	public Node findNode(int input) {
		Node n = root;
		
		// If the root contains the desired value, return it.
		if(n == null) {
			return n;
		}
		else if(n.hasVal(input))
			return root;
		
		// Otherwise, search through the entire tree.
		while((n = n.getChildOf(input)) != null) {
			if(n.hasVal(input)) {
				return n;
			}
		}
		return null;
	}
	
	public int size(int x) {
		
		// Try to find desired node
		Node n = findNode(x);
		
		// If n exists within the tree, return the subtree size
		if(n != null)
			return n.getSize();
		else { 
			return 0; 
		}
	}
}
