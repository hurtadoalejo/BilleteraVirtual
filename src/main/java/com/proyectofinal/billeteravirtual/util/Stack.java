package com.proyectofinal.billeteravirtual.util;

public class Stack<T> {

    private Node<T> top;
    private int size;

    public void push(T data) {
        Node<T> newNode = new Node<>(data);

        newNode.next = top;
        top = newNode;

        size++;
    }

    public T pop() {

        if (isEmpty()) {
            return null;
        }

        T data = top.data;

        top = top.next;

        size--;

        return data;
    }

    public T peek() {

        if (isEmpty()) {
            return null;
        }

        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }

    private static class Node<T> {

        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }
}
