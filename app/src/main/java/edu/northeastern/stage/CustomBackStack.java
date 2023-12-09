package edu.northeastern.stage;

import java.util.LinkedList;

public class CustomBackStack {
    private LinkedList<String> stack = new LinkedList<>();

    public void pushOrBringToFront(String fragmentTag) {
        stack.remove(fragmentTag); // Remove if it exists
        stack.addFirst(fragmentTag); // Add to the front
    }

    public void pushLast(String fragmentTag) {
        stack.addLast(fragmentTag);
    }

    public String pop() {
        return stack.pollFirst(); // Remove and return the top element
    }

    public String peekFirst() {
        return stack.peekFirst(); // Return the top element without removing
    }

    public String peekLast() {
        return stack.peekLast(); // Return the bottom element without removing
    }

    public void remove(String fragmentTag) {
        stack.remove(fragmentTag);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    public String getStackStatus() {
        return stack.toString();
    }
}

