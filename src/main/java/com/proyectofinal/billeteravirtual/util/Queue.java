package com.proyectofinal.billeteravirtual.util;

public class Queue<T> {

    private Node<T> front;
    private Node<T> rear;
    private int size;

    public void offer(T data) {
        Node<T> newNode = new Node<>(data);

        if (rear == null) {
            front = rear = newNode;
        } else {
            rear.setNext(newNode);
            rear = newNode;
        }

        size++;
    }

    public T poll() {
        if (isEmpty()) return null;

        T data = front.getData();
        front = front.getNext();

        if (front == null) rear = null;

        size--;
        return data;
    }

    public T peek() {
        return isEmpty() ? null : front.getData();
    }

    public boolean isEmpty() {
        return front == null;
    }

    public int size() {
        return size;
    }
}