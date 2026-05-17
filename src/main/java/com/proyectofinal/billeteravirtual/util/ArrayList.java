package com.proyectofinal.billeteravirtual.util;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayList<T> implements Iterable<T> {

    private Node<T> head;
    private int size;

    /**
     * Obtiene la cantidad de elementos almacenados actualmente en la lista.
     * @return El número total de nodos en la lista.
     */
    public int size() {
        return size;
    }

    /**
     * Verifica si la lista se encuentra vacía.
     * @return true si el nodo cabeza (head) es nulo; false en caso contrario.
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Agrega un nuevo elemento al final de la lista enlazada (operación de inserción).
     * @param data El dato de tipo genérico T que se va a almacenar.
     */
    public void add(T data) {
        Node<T> newNode = new Node<>(data);

        if (head == null) {
            head = newNode;
        } else {

            Node<T> current = head;

            while (current.getNext() != null) {
                current = current.getNext();
            }

            current.setNext(newNode);
        }

        size++;
    }

    /**
     * Busca y elimina la primera aparición del elemento especificado en la lista,
     * reajustando los enlaces de los nodos adyacentes.
     * @param data El dato que se desea remover de la lista.
     * @return true si el elemento fue encontrado y eliminado con éxito; false en caso contrario.
     */
    public boolean remove(T data) {
        if (head == null) {
            return false;
        }

        if (head.getData().equals(data)) {
            head = head.getNext();
            size--;
            return true;
        }

        Node<T> current = head;

        while (current.getNext() != null) {

            if (current.getNext().getData().equals(data)) {

                current.setNext(current.getNext().getNext());

                size--;

                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    /**
     * Determina si un elemento específico existe dentro de la estructura de la lista.
     * @param data El dato que se quiere buscar.
     * @return true si el elemento coincide con el contenido de algún nodo; false en caso contrario.
     */
    public boolean contains(T data) {
        Node<T> current = head;

        while (current != null) {

            if (current.getData().equals(data)) {
                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    /**
     * Recupera el dato almacenado en el nodo que ocupa la posición indexada especificada.
     * @param index La posición de base cero del elemento que se desea obtener.
     * @return El valor genérico T contenido en ese nodo.
     * @throws IndexOutOfBoundsException Si el índice proporcionado es negativo o supera el tamaño de la lista.
     */
    public T get(int index) {
        checkIndex(index);

        Node<T> current = head;

        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }

        return current.getData();
    }

    /**
     * Valida de manera estricta si un índice solicitado se encuentra dentro del rango real de la lista.
     * @param index El índice numérico a evaluar.
     * @throws IndexOutOfBoundsException Si el índice no es válido (menor a 0 o mayor/igual al tamaño actual).
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size
            );
        }
    }

    /**
     * Proporciona un iterador personalizado para recorrer de forma secuencial y segura
     * los elementos de la lista enlazada utilizando un bucle for-each.
     * @return Una nueva instancia de Iterator configurada en el nodo cabeza (head).
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private Node<T> current = head;

            /**
             * Comprueba si todavía quedan elementos por recorrer en la iteración actual.
             * @return true si el nodo actual apunta a una referencia válida; false si llegó al final.
             */
            @Override
            public boolean hasNext() {
                return current != null;
            }

            /**
             * Devuelve el dato del nodo actual y desplaza el puntero de la iteración hacia el siguiente nodo.
             * @return El valor genérico T del nodo inspeccionado.
             * @throws NoSuchElementException Si se invoca el método cuando ya no quedan elementos en la lista.
             */
            @Override
            public T next() {

                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                T data = current.getData();

                current = current.getNext();

                return data;
            }
        };
    }
}