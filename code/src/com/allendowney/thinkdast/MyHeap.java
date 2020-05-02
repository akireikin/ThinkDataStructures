package com.allendowney.thinkdast;

import java.util.*;

public class MyHeap<E>{
    private List<E> values;

    public MyHeap() {
        values = new ArrayList<>();
    }

    /**
     * Push new value to the heap
     */
    public void push(E value) {
        // Add value to the end
        values.add(value);

        int index = values.size();
        while (index > 1) {
            int prev = index >> 1;
            if (((Comparable<E>) values.get(index - 1)).compareTo(values.get(prev - 1)) >= 0) {
                break;
            }

            Collections.swap(values, index - 1, prev - 1);
            index = prev;
        }
    }

    public E poll() {
        E result = values.get(0);

        // make the last element the root
        values.set(0, values.get(values.size() - 1));
        values.remove(values.size() - 1);

        // rebalance
        int index = 0;
        int nextIndex;
        while (true) {
            nextIndex = (index + 1) * 2 - 1;

            // If right node is not missing and less than right, than this is the candidate for future comparison
            if (nextIndex + 1 < size() && ((Comparable<E>) values.get(nextIndex)).compareTo(values.get(nextIndex + 1)) > 0) {
                nextIndex++;
            }

            if (nextIndex < size() && ((Comparable<E>) values.get(index)).compareTo(values.get(nextIndex)) > 0) {
                Collections.swap(values, index, nextIndex);
                index = nextIndex;
                continue;
            }

            break;
        }

        return result;
    }

    /**
     * Get and remove the smallest one
     */
    public E pollWithNulls() {
        int index = 0;
        int nextIndex;
        E result = values.get(index);

        while (true) {
            nextIndex = (index + 1)*2 - 1;

            // If left out of bound -> set null and exit
            if (nextIndex >= values.size()) {
                values.set(index, null);
                break;
            } else if (nextIndex + 1 >= values.size()) {
                // if left is ok, but right is out of bound -> do nothing
            } else {
                E valueLeft = values.get(nextIndex);
                E valueRight = values.get(nextIndex + 1);
                if (valueLeft == null & valueRight == null) {
                    values.set(index, null);
                    break;
                }
                if (valueLeft == null) {
                    nextIndex++;
                } else if (valueRight == null) {
                    // nothing
                } else {
                    if (((Comparable<E>) valueLeft).compareTo(valueRight) > 0) {
                        nextIndex++;
                    }
                }
            }

            values.set(index, values.get(nextIndex));
            index = nextIndex;
        }

        return result;
    }

    public E peek() {
        if (size() == 0) {
            return null;
        }

        return values.get(0);
    }

    public int size() {
        return values.size();
    }

    public static void main(String[] args) {
        MyHeap<Integer> heap = new MyHeap<Integer>();
        heap.push(6);
        heap.push(3);
        heap.push(5);
        heap.push(8);
        heap.push(1);
        heap.push(4);
        heap.push(2);
        heap.push(7);
        System.out.println(heap.values);

        for (int i = 0; i < 8; i++) {
            System.out.println(heap.poll());
        }
    }
}
