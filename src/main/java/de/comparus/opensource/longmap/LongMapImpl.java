package de.comparus.opensource.longmap;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LongMapImpl<V> implements LongMap<V> {
    private static final String BUCKET_INITIALIZE_EXCEPTION = "Bucket not initialize";
    private static final String VALUE_NOT_FOUND_EXCEPTION = "Value not found by this id";
    private static final int DEFAULT_CAPACITY = 1 << 4;
    private static final float DEFAULT_LOAD_FACTOR = 0.75F;
    private int capacity;
    private int threshold;
    private final float loadFactor;
    private Bucket<V>[] buckets;
    private int size;

    public LongMapImpl() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        initMap( DEFAULT_CAPACITY );
    }

    public static void main(String[] args) {

    }

    @Override
    public V put(long key,V value) {
        int index = countIndexBucket( key );

        if ( buckets[index] != null ) {
            Bucket.Node<V> node = buckets[index].getHeadNode();

            do {
                if ( node.key == key ) {
                    buckets[index].deleteNode( key );
                    buckets[index].addBack( key,value );
                    break;
                } else if ( node.next == null ) {
                    buckets[index].addBack( key,value );
                    ++size;
                }
            } while ((node = node.next) != null);
        } else {
            buckets[index] = new Bucket<>();
            buckets[index].setHead( key,value );
            ++size;
        }

        if ( size > threshold ) {
            enlargeBuckets();
        }

        return null;
    }

    @Override
    public V get(long key) {
        int index = countIndexBucket( key );
        Bucket.Node<V> node = getHeadNodeFromBucket( index );

        do {
            if ( node.getKey() == key ) {
                break;
            }
        } while ((node = node.getNext()) != null);

        return Optional.ofNullable( node )
            .orElseThrow( () -> new IllegalArgumentException( VALUE_NOT_FOUND_EXCEPTION ) )
            .getValue();
    }

    private Bucket.Node<V> getHeadNodeFromBucket(int index) {
        return Optional.ofNullable( buckets[index] )
            .orElseThrow( () -> new IllegalArgumentException( BUCKET_INITIALIZE_EXCEPTION ) )
            .getHeadNode();
    }

    @Override
    public V remove(long key) {
        int index = countIndexBucket( key );
        V value = Optional.ofNullable( buckets[index] )
            .orElseThrow( () -> new IllegalArgumentException( BUCKET_INITIALIZE_EXCEPTION ) )
            .deleteNode( key );
        size = (value != null) ? --size : size;

        return value;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(long key) {
        int index = countIndexBucket( key );
        Bucket.Node<V> node = getHeadNodeFromBucket( index );
        boolean isFound = false;

        do {
            if ( node.getKey() == key ) {
                isFound = true;
                break;
            }
        } while ((node = node.getNext()) != null);

        return isFound;
    }

    @Override
    public boolean containsValue(V value) {
        boolean isFound = false;

        for (int index = 0; index < buckets.length; index++) {
            if ( buckets[index] != null ) {
                Bucket.Node<V> node = getHeadNodeFromBucket( index );

                do {
                    if ( node.getValue().equals( value ) ) {
                        isFound = true;
                        break;
                    }
                } while ((node = node.getNext()) != null);
            }
        }

        return isFound;
    }

    @Override
    public long[] keys() {
        List<Long> keys = new ArrayList<>();

        for (int index = 0; index < buckets.length; index++) {
            if ( buckets[index] != null ) {
                Bucket.Node node = buckets[index].getHeadNode();

                do {
                    keys.add( node.getKey() );
                } while ((node = node.getNext()) != null);
            }
        }

        return keys.stream().mapToLong( Long::longValue ).toArray();
    }

    @Override
    public V[] values() {
        List<Object> values = new ArrayList<>();

        for (int index = 0; index < buckets.length; index++) {
            if ( buckets[index] != null ) {
                Bucket.Node<V> node = buckets[index].getHeadNode();

                do {
                    values.add( node.getValue() );
                } while ((node = node.getNext()) != null);
            }
        }

        return (V[]) values.toArray();
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void clear() {
        initMap( DEFAULT_CAPACITY );
    }

    private void initMap(int capacity) {
        this.capacity = capacity;
        this.threshold = (int) (this.capacity * loadFactor);
        this.buckets = new Bucket[this.capacity];
        this.size = 0;
    }

    private void enlargeBuckets() {
        Bucket<V>[] oldBuckets = buckets;
        initMap( capacity <<= 1 );

        for (int index = 0; index < oldBuckets.length; index++) {
            if ( oldBuckets[index] != null ) {
                Bucket.Node<V> node = oldBuckets[index].getHeadNode();

                do {
                    put( node.getKey(),node.getValue() );
                } while ((node = node.next) != null);
            }
        }
    }

    private int countIndexBucket(long key) {
        return (int) ((buckets.length - 1) & key);
    }

    static class Bucket<V> {
        private Node<V> head;
        private Node<V> tail;

        void setHead(long key,V value) {
            Node<V> node = new Node( key,value );

            if ( head == null ) {
                head = node;
                tail = node;
            } else {
                node.setNext( head );
                head = node;
            }
        }

        Node<V> getHeadNode() {
            return this.head;
        }

        void addBack(long key,V value) {
            Node<V> node = new Node( key,value );

            if ( tail == null ) {
                head = node;
            } else {
                tail.setNext( node );
            }
            tail = node;
        }

        V deleteNode(long key) {
            Node<V> result = null;

            if ( head == null )
                return null;

            if ( head == tail ) {
                result = head;
                head = tail = null;

                return result.getValue();
            }

            if ( head.key == key ) {
                result = head;
                head = head.getNext();

                return result.getValue();
            }

            Node<V> node = head;
            while (node.getNext() != null) {
                if ( node.getNext().getKey() == key ) {

                    if ( tail == node.next ) {
                        tail = node;
                    }

                    result = node.getNext();
                    node.setNext( node.getNext().getNext() );
                    return result.getValue();
                }
                node = node.getNext();
            }

            return null;
        }

        static class Node<V> {
            Node<V> next;
            long key;
            V value;

            public Node(long key,V value) {
                this.key = key;
                this.value = value;
            }

            public long getKey() {
                return key;
            }

            public V getValue() {
                return value;
            }

            public Node<V> getNext() {
                return this.next;
            }

            public void setNext(Node<V> next) {
                this.next = next;
            }

            @Override
            public String toString() {
                return key + "=" + value;
            }
        }
    }
}
