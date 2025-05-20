package org.example;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MyHashMap<K, V> extends AbstractMap<K, V> implements Iterable<AbstractMap.SimpleEntry<K, V>> {
    private static final int INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return StreamSupport.stream(spliterator(), false)
                .collect(Collectors.toUnmodifiableSet());
    }

    private record Bucket<K, V>(LinkedList<SimpleEntry<K, V>> chain) {
        public Bucket() {
            this(new LinkedList<>());
        }

        private SimpleEntry<K, V> findEntry(Object key, boolean removeOnFound) {
            for (ListIterator<SimpleEntry<K, V>> it = chain.listIterator(); it.hasNext();) {
                SimpleEntry<K, V> entry = it.next();
                if (key.hashCode() == entry.getKey().hashCode() && key.equals(entry.getKey())) {
                    if (removeOnFound) {
                        it.remove();
                    }
                    return entry;
                }
            }
            return null;
        }

        public SimpleEntry<K, V> getEntry(Object key) {
            return findEntry(key, false);
        }

        public SimpleEntry<K, V> setEntry(K key, V value) {
            SimpleEntry<K, V> entry = findEntry(key, false);
            if (entry != null) {
               entry.setValue(value);
            }
            return entry;
        }

        public SimpleEntry<K, V> removeEntry(Object key) {
            return findEntry(key, true);
        }
    }

    private Bucket<K, V>[] buckets;
    private int size;
    private int capacity;
    private final float loadFactor;

    @SuppressWarnings("unchecked")
    private static <K, V> Bucket<K, V>[] allocateBuckets(int capacity) {
        return  (Bucket<K, V>[]) Stream.generate(Bucket<K, V>::new)
                .limit(capacity)
                .toArray(Bucket[]::new);
    }

    public MyHashMap(int capacity, float loadFactor) throws IllegalArgumentException {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        if (loadFactor <= 0.f) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        buckets = allocateBuckets(capacity);
    }

    public MyHashMap() {
        this(INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public float currentLoad() {
        if (buckets.length == 0) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) size / buckets.length;
    }

    public V get(Object key) throws NullPointerException{
        if (key == null) {
            throw new NullPointerException();
        }

        Bucket<K, V> bucket = getBucketWith(key);
        SimpleEntry<K, V> entry = bucket.getEntry(key);
        return entry == null ? null : entry.getValue();
    }

    @SuppressWarnings("UnusedReturnValue")
    public V put(K key, V value) throws NullPointerException {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        if (currentLoad() > loadFactor) {
            reallocate();
        }

        Bucket<K, V> bucket = getBucketWith(key);
        SimpleEntry<K, V> entry = bucket.setEntry(key, value);
        if (entry != null) {
            return entry.getValue();
        }

        bucket.chain.addLast(new SimpleEntry<>(key, value));
        ++size;

        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    public V remove(Object key) throws NullPointerException {
        if (key == null) {
            throw new NullPointerException();
        }

        Bucket<K, V> bucket = getBucketWith(key);
        SimpleEntry<K, V> entry = bucket.removeEntry(key);
        if (entry != null) {
            --size;
            return entry.getValue();
        }
        return null;
    }

    @Override
    public Iterator<SimpleEntry<K, V>> iterator() {
        return new Iterator<>() {
            private int bucketNum;
            private int nextEntryNum;

            @Override
            public boolean hasNext() {
                if (buckets.length == 0) {
                    return false;
                }

                Bucket<K, V> currentBucket = buckets[bucketNum];
                if (nextEntryNum < currentBucket.chain.size()) {
                    return true;
                }
                for (int i = bucketNum + 1; i < capacity; ++i) {
                    if (! buckets[i].chain.isEmpty()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public SimpleEntry<K, V> next() {
                if (buckets.length == 0) {
                    throw new NoSuchElementException();
                }

                Bucket<K, V> currentBucket = buckets[bucketNum];
                if (nextEntryNum < currentBucket.chain.size()) {
                    return currentBucket.chain.get(nextEntryNum++);
                }
                for (int i = bucketNum + 1; i < capacity; ++i) {
                    if (! buckets[i].chain.isEmpty()) {
                        bucketNum = i;
                        nextEntryNum = 1;
                        return buckets[i].chain.getFirst();
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }

    public int size() {
        return size;
    }

    private Bucket<K, V> getBucketWith(Object key) {
        int hash = key.hashCode();
        int bucketNum = hash % capacity;
        return buckets[bucketNum];
    }

    private void reallocate() {
        Bucket<K, V>[] oldBuckets = buckets;

        capacity = capacity == 0? 1: capacity * 2;
        size = 0;
        buckets = allocateBuckets(capacity);

        for (Bucket<K, V> bucket : oldBuckets) {
            for (SimpleEntry<K, V> entry : bucket.chain) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }
}
