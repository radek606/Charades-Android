package com.ick.kalambury.util

class LimitedMutableList<E>(
        private val sizeLimit: Int,
        private val innerList: MutableList<E> = mutableListOf()
) : MutableList<E> by innerList {

    override fun add(element: E): Boolean {
        if (size == sizeLimit) {
            removeAt(0)
        }
        return innerList.add(element)
    }

}