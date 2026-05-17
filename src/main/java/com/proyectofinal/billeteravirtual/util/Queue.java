package com.proyectofinal.billeteravirtual.util;

public class Queue<T> {

    private Node<T> front;
    private Node<T> rear;
    private int size;

    /**
     * Inserta un nuevo elemento al final de la cola (operación enqueue / offer).
     * @param data El dato de tipo genérico T que se va a almacenar.
     */
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

    /**
     * Remueve y devuelve el elemento ubicado al frente de la cola (operación dequeue / poll).
     * @return El valor genérico T del elemento removido, o null si la cola está vacía.
     */
    public T poll() {
        if (isEmpty()) return null;

        T data = front.getData();
        front = front.getNext();

        if (front == null) rear = null;

        size--;
        return data;
    }

    /**
     * Consulta el elemento ubicado al frente de la cola sin removerlo (operación peek).
     * @return El valor genérico T del elemento al frente, o null si la cola está vacía.
     */
    public T peek() {
        return isEmpty() ? null : front.getData();
    }

    /**
     * Verifica si la cola se encuentra vacía.
     * @return true si el nodo frente (front) es nulo; false en caso contrario.
     */
    public boolean isEmpty() {
        return front == null;
    }

    /**
     * Obtiene la cantidad de elementos almacenados actualmente en la cola.
     * @return El número total de nodos en la cola.
     */
    public int size() {
        return size;
    }
}