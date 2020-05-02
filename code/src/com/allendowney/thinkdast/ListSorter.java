package com.allendowney.thinkdast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Provides sorting algorithms.
 *
 */
public class ListSorter<T> {

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void insertionSort(List<T> list, Comparator<T> comparator) {
	
		for (int i=1; i < list.size(); i++) {
			T elt_i = list.get(i);
			int j = i;
			while (j > 0) {
				T elt_j = list.get(j-1);
				if (comparator.compare(elt_i, elt_j) >= 0) {
					break;
				}
				list.set(j, elt_j);
				j--;
			}
			list.set(j, elt_i);
		}
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void mergeSortInPlace(List<T> list, Comparator<T> comparator) {
		List<T> sorted = mergeSort(list, comparator);
		list.clear();
		list.addAll(sorted);
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * Returns a list that might be new.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public List<T> mergeSort(List<T> list, Comparator<T> comparator) {
		// Out of the recursion
		if (list.size() == 1) {
			return list;
		}

		int newSize = list.size() / 2;
		List<T> left = new LinkedList<>(list.subList(0, newSize));
		List<T> right = new LinkedList<>(list.subList(newSize, list.size()));
		return merge(mergeSort(left, comparator), mergeSort(right, comparator), comparator);
	}

	/**
	 * Merges two sorted lists into a single sorted list.
	 * 
	 * @param first
	 * @param second
	 * @param comparator
	 * @return
	 */
	private List<T> merge(List<T> first, List<T> second, Comparator<T> comparator) {
		List<T> merged = new LinkedList<>();
		int overallSize = first.size() + second.size();
		while (merged.size() < overallSize) {
			if (first.size() == 0) {
				merged.add(second.remove(0));
			} else if (second.size() == 0) {
				merged.add(first.remove(0));
			} else {
				merged.add(comparator.compare(first.get(0), second.get(0)) < 0 ? first.remove(0) : second.remove(0));
			}
		}
		return merged;
	}

	/**
	 * Returns the list with the smaller first element, according to `comparator`.
	 * 
	 * If either list is empty, `pickWinner` returns the other.
	 * 
	 * @param first
	 * @param second
	 * @param comparator
	 * @return
	 */
	private List<T> pickWinner(List<T> first, List<T> second, Comparator<T> comparator) {
		return null;
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void heapSort(List<T> list, Comparator<T> comparator) {
		MyHeap<T> heap = new MyHeap<T>();
		for (T element: list) {
			heap.push(element);
		}
		for (int i = 0; i < list.size(); i++) {
			list.set(i, heap.poll());
		}
	}

	
	/**
	 * Returns the largest `k` elements in `list` in ascending order.
	 * 
	 * @param k
	 * @param list
	 * @param comparator
	 * @return 
	 * @return
	 */
	public List<T> topK(int k, List<T> list, Comparator<T> comparator) {
		MyHeap<T> heap = new MyHeap<T>();
		for (T element: list) {
			if (heap.size() < k) {
				heap.push(element);
			} else if (comparator.compare(heap.peek(), element) < 0) {
				heap.poll();
				heap.push(element);
			}
		}

		List<T> result = new LinkedList<T>();
		for (int i = 0; i < k; i++) {
			result.add(heap.poll());
		}
		return result;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer elt1, Integer elt2) {
				return elt1.compareTo(elt2);
			}
		};
		
		ListSorter<Integer> sorter = new ListSorter<Integer>();
		sorter.insertionSort(list, comparator);
		System.out.println(list);

		list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.mergeSortInPlace(list, comparator);
		System.out.println(list);

		list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.heapSort(list, comparator);
		System.out.println(list);
	
		list = new ArrayList<Integer>(Arrays.asList(6, 3, 5, 8, 1, 4, 2, 7));
		List<Integer> queue = sorter.topK(4, list, comparator);
		System.out.println(queue);
	}
}
