package com.elevatemc.anticheat.util.type;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

public final class EvictingList<T> extends LinkedList<T> {

    @Getter
    private final int maxSize;

    public EvictingList(final int maxSize) {
        this.maxSize = maxSize;
    }

    public EvictingList(final Collection<? extends T> c, int maxSize) {
        super(c);
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(T t) {
        if (size() >= getMaxSize()) removeFirst();
        return super.add(t);
    }

    public boolean isFull() {
        return size() >= getMaxSize();
    }
}
