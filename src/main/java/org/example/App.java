package org.example;

import java.util.stream.StreamSupport;

public class App {
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>(0, 0.8f);
        for (int i = 0; i < 100; ++i) {
            map.put(String.valueOf(i), i);
        }

        assert map.size() == 100;
        for (int i = 0; i < 100; ++i) {
            assert map.get(String.valueOf(i)) == i;
        }

        assert map.put("100", 100) == null;
        assert map.size() == 101;

        assert map.put("100", 111) == 100;
        assert map.size() == 101;

        assert map.remove("100") == 111;
        assert map.size() == 100;

        assert map.remove("999") == null;
        assert map.size() == 100;

        assert map.get("999") == null;

        assert map.put(null, 1) == null;
        assert map.size() == 101;

        assert map.put(null, 2) == 1;
        assert map.size() == 101;

        assert map.remove(null) == 2;
        assert map.size() == 100;

        assert map.put(null, null) == null;
        assert map.size() == 101;

        assert map.get(null) == null;
        assert map.put(null, 3) == null;
        assert map.size() == 101;
        assert map.remove(null) == 3;
        assert map.size() == 100;

        for (int i = 0; i < 50; ++i) {
            map.remove(String.valueOf(i * 2));
        }
        assert map.size() == 50;

        StreamSupport.stream(map.spliterator(), false)
                .sorted(java.util.Map.Entry.comparingByValue())
                .forEach(System.out::println);
    }
}