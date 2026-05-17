package com.proyectofinal.billeteravirtual.util;

public class Stack<T> {

    private Node<T> top;
    private int size;

    /**
     * Inserta un nuevo elemento en el tope de la pila (operación push).
     * @param data El dato de tipo genérico T que se va a almacenar.
     */
    public void push(T data) {
        Node<T> newNode = new Node<>(data);

        newNode.next = top;
        top = newNode;

        size++;
    }

    /**
     * Remueve y devuelve el elemento ubicado en el tope de la pila (operación pop).
     * @return El valor genérico T del elemento removido, o null si la pila está vacía.
     */
    public T pop() {

        if (isEmpty()) {
            return null;
        }

        T data = top.data;

        top = top.next;

        size--;

        return data;
    }

    /**
     * Consulta el elemento ubicado en el tope de la pila sin removerlo (operación peek).
     * @return El valor genérico T del elemento en el tope, o null si la pila está vacía.
     */
    public T peek() {

        if (isEmpty()) {
            return null;
        }

        return top.data;
    }

    /**
     * Verifica si la pila se encuentra vacía.
     * @return true si el nodo tope (top) es nulo; false en caso contrario.
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Obtiene la cantidad de elementos almacenados actualmente en la pila.
     * @return El número total de nodos en la pila.
     */
    public int size() {
        return size;
    }

    /**
     * Clase interna estática que representa un nodo de la pila enlazada.
     * @param <T> El tipo de dato genérico que almacena el nodo.
     */
    private static class Node<T> {

        T data;
        Node<T> next;

        /**
         * Construye un nuevo nodo con el dato especificado.
         * @param data El valor que se almacenará en este nodo.
         */
        Node(T data) {
            this.data = data;
        }
    }
}
