/**
 *
 */
package com.allendowney.thinkdast;

import java.util.*;

/**
 * Implementation of a Map using a binary search tree.
 *
 * @param <K>
 * @param <V>
 *
 */
public class MyTreeMap<K, V> implements Map<K, V> {

	private int size = 0;
	private Node root = null;

	/**
	 * Represents a node in the tree.
	 *
	 */
	protected class Node {
		public K key;
		public V value;
		public Node left = null;
		public Node right = null;

		/**
		 * @param key
		 * @param value
		 * @param left
		 * @param right
		 */
		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@Override
	public void clear() {
		size = 0;
		root = null;
	}

	@Override
	public boolean containsKey(Object target) {
		return findNode(target) != null;
	}

	/**
	 * Returns the entry that contains the target key, or null if there is none.
	 *
	 * @param target
	 */
	private Node findNode(Object target) {
		// some implementations can handle null as a key, but not this one
		if (target == null) {
			throw new IllegalArgumentException();
		}

		// something to make the compiler happy
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) target;

		Node node = root;
		while (node != null) {
			int comparison = k.compareTo(node.key);
			if (comparison == 0) {
				return node;
			}

			node = comparison < 0 ? node.left : node.right;
		}

		return null;
	}

	/**
	 * Compares two keys or two values, handling null correctly.
	 *
	 * @param target
	 * @param obj
	 * @return
	 */
	private boolean equals(Object target, Object obj) {
		if (target == null) {
			return obj == null;
		}
		return target.equals(obj);
	}

	@Override
	public boolean containsValue(Object target) {
		return containsValueHelper(root, target);
	}

	private boolean containsValueHelper(Node node, Object target) {
		if (node == null) {
			return false;
		}

		return equals(target, node.value) || containsValueHelper(node.left, target) || containsValueHelper(node.right, target);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key) {
		Node node = findNode(key);
		if (node == null) {
			return null;
		}
		return node.value;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new LinkedHashSet<K>();
		Deque<Node> stack = new LinkedList<Node>();
		Node current = root;

		while (!stack.isEmpty() || current != null) {
			if (current != null) {
				stack.push(current);
				current = current.left;
			} else {
				current = stack.pop();
				set.add(current.key);
				current = current.right;
			}
		}

		return set;
	}

	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (root == null) {
			root = new Node(key, value);
			size++;
			return null;
		}
		return putHelper(root, key, value);
	}

	private V putHelper(Node node, K key, V value) {
		@SuppressWarnings("unchecked")
		int comparison = ((Comparable<? super K>) key).compareTo(node.key);
		if (comparison == 0) {
			V oldValue = node.value;
			node.value = value;
			return oldValue;
		}

		if (comparison < 0) {
			if (node.left == null) {
				node.left = new Node(key, value);
				size++;
			} else {
				return putHelper(node.left, key, value);
			}
		} else {
			if (node.right == null) {
				node.right = new Node(key, value);
				size++;
			} else {
				return putHelper(node.right, key, value);
			}
		}

		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		// some implementations can handle null as a key, but not this one
		if (key == null) {
			throw new IllegalArgumentException();
		}

		// something to make the compiler happy
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) key;

		// Empty tree
		if (root == null) {
			return null;
		}

		// Find node, saving its parent
		Node parent = null;
		Node current = root;
		while (true) {
			int comparison = k.compareTo(current.key);
			if (comparison == 0) {
				// Found
				break;
			}

			parent = current;
			current = comparison < 0 ? current.left : current.right;
			if (current == null) {
				// Not found
				return null;
			}
		}

		// Leave - just delete
		if (current.left == null && current.right == null) {
			if (parent == null) {
				root = null;
			} else if (parent.left == current) {
				parent.left = null;
			} else {
				parent.right = null;
			}
		}

		// Node without left just collapse
		if (current.left == null && current.right != null) {
			if (parent == null) {
				root = current.right;
			} else if (parent.left == current) {
				parent.left = current.right;
			} else {
				parent.right = current.right;
			}
		}

		// Node without right just collapse
		if (current.left != null && current.right == null) {
			if (parent == null) {
				root = current.left;
			} else if (parent.left == current) {
				parent.left = current.left;
			} else {
				parent.right = current.left;
			}
		}

		// Node with both children requires rebalance
		if (current.left != null && current.right != null) {
			// Find minimum value in right subtree
			Node min = current.right;

			// If root of right subtree is min, collapse it
			if (min.left == null) {
				current.right = min.right;
			} else {
				// Otherwise find in min always using left pointers
				// Collapse when found
				Node parentOfMin;
				do {
					parentOfMin = min;
					min = min.left;
				} while (min.left != null);
				parentOfMin.left = min.right;
			}

			// Replace current with new min
			min.left = current.left;
			min.right = current.right;
			if (parent == null) {
				root = min;
			} else if (parent.left == current) {
				parent.left = min;
			} else {
				parent.right = min;
			}
		}
		size--;

		return current.value;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		Set<V> set = new HashSet<V>();
		Deque<Node> stack = new LinkedList<Node>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node == null) continue;
			set.add(node.value);
			stack.push(node.left);
			stack.push(node.right);
		}
		return set;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyTreeMap<String, Integer>();
		map.put("Word1", 1);
		map.put("Word2", 2);
		Integer value = map.get("Word1");
		System.out.println(value);

		for (String key: map.keySet()) {
			System.out.println(key + ", " + map.get(key));
		}
	}

	/**
	 * Makes a node.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public MyTreeMap<K, V>.Node makeNode(K key, V value) {
		return new Node(key, value);
	}

	/**
	 * Sets the instance variables.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param node
	 * @param size
	 */
	public void setTree(Node node, int size ) {
		this.root = node;
		this.size = size;
	}

	/**
	 * Returns the height of the tree.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @return
	 */
	public int height() {
		return heightHelper(root);
	}

	private int heightHelper(Node node) {
		if (node == null) {
			return 0;
		}
		int left = heightHelper(node.left);
		int right = heightHelper(node.right);
		return Math.max(left, right) + 1;
	}
}
