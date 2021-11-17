package de.comparus.opensource.longmap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class LongMapImplTest {
    private LongMap<String> map;
    private static final String VALUE_TEST = "TEST";

    @BeforeEach
    public void init(){
        map = new LongMapImpl<>();
        long keyOne = 31;
        long keySecond = 63;
        long keyThree = -127;

        map.put(keyOne, VALUE_TEST);
        map.put(keySecond, VALUE_TEST);
        map.put(keyThree, VALUE_TEST);
    }


    @Test
    void put() {
        assertEquals(3, map.size());
    }

    @Test
    void get_whenBucketMoreThanOneNode_CollisionEnable_valueStringType() {
         int key = 63;

         assertEquals(VALUE_TEST, map.get(key));
    }

    @Test
    void remove() {
        int key = 31;

        assertEquals(VALUE_TEST, map.remove(31));
    }

    @Test
    void remove_whenBucketNotInitialize() {
        long key = 1000;

        assertThrows(IllegalArgumentException.class, () -> map.remove(key));
    }

    @Test
    void isEmpty() {
        assertFalse(map.isEmpty());
    }

    @Test
    void containsKey_whenBucketMoreThanOne_and_collisionEnable() {
        int key = 31;
        assertTrue(map.containsKey(31));
    }

    @Test
    void containsValue_whenBucketMoreThanOne_and_collisionEnable() {
        assertTrue(map.containsValue(VALUE_TEST));
    }

    @Test
    void keys_whenEnableCollision() {
        long[] keysExpected = new long[]{-127, 31, 63};

        assertEquals(Arrays.toString(keysExpected) , Arrays.toString(map.keys()));
    }

    @Test
    void values_whenEnableCollision_And_BucketMoreThanOne() {
        long keyFirst = 31;
        long keySecond = 63;
        long keyThree = 25;
        String[] values = new String[]{"some1", "some2", "some2"};

        map.put(keyFirst, values[0]);
        map.put(keySecond, values[1]);
        map.put(keyThree, values[2]);

        assertTrue(Arrays.toString(map.values()).contains(values[0]));
        assertTrue(Arrays.toString(map.values()).contains(values[1]));
        assertTrue(Arrays.toString(map.values()).contains(values[2]));
    }

    @Test
    void size_whenEnableCollision_and_bucketMoreThanOne() {
        int size = 3;
        assertEquals(size, map.size());

    }

    @Test
    void clear_whenBucketMoreThanOne_And_collisionEnable() {
        map.clear();

        assertEquals(0, map.size());
    }
}