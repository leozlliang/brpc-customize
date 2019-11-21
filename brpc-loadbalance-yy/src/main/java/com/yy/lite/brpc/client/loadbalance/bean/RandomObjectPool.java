package com.yy.lite.brpc.client.loadbalance.bean;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomObjectPool<T extends RandomObject> {

    private final List<T> randomObjectsPool;

    private double totalWeight = 0;

    private static final int DEFAULT_SIZE = 8;

    private final Random random = new Random(System.currentTimeMillis());

    public RandomObjectPool() {
        this(DEFAULT_SIZE);
    }

    public RandomObjectPool(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Illegal size : " + size);
        }
        this.randomObjectsPool = new ArrayList<>();
    }

    public RandomObjectPool(List<T> randomObjectsPool) {
        this.randomObjectsPool = randomObjectsPool;
        for (RandomObject randomObject : randomObjectsPool) {
            if (randomObject.getWeight() <= 0) {
                continue;
            }
            totalWeight += randomObject.getWeight();
        }
    }

    public int size() {
        return randomObjectsPool.size();
    }
    public boolean addRandomObject(T randomObject) {
        if (randomObject.getWeight() > 0) {
            totalWeight += randomObject.getWeight();
        }
        return randomObjectsPool.add(randomObject);
    }

    public boolean removeRandomObject(RandomObject randomObject) {
        return randomObjectsPool.remove(randomObject);
    }

    /**
     * 随机一个对象
     * @return
     */
    public RandomObject<T> random() {
        return doRandom(1, false).get(0);
    }

    /**
     * 随机多个对象
     * @param count 数量
     * @return
     */
    public List<RandomObject<T>> random(int count) {
        return doRandom(count, false);
    }

    /**
     * 随机一个对象并从对象池移除
     * @return
     */
    public RandomObject randomAndRemove() {
        return doRandom(1, true).get(0);
    }

    /**
     * 随机多个对象并从对象池移除
     * @param count 数量
     * @return
     */
    public List<RandomObject<T>> randomAndRemove(int count) {
        return doRandom(count, true);
    }

    private List<RandomObject<T>> doRandom(int count, boolean remove) {
        if (randomObjectsPool.size() < 0) {
            throw new RuntimeException("Not enough random object");
        }
        List<RandomObject<T>> result = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            result.add(doRandomOne(remove));
        }
        return result;
    }

    private RandomObject<T> doRandomOne(boolean remove) {

        double randomNumber = 0;
        while (randomNumber <= 0) {
            randomNumber = random.nextDouble() * totalWeight;
        }
        Iterator<T> it = randomObjectsPool.iterator();
        while (it.hasNext()) {
            RandomObject randomObject = it.next();
            if (randomObject.getWeight() <= 0) {
                continue;
            }
            if (randomNumber <= randomObject.getWeight()) {
                if (remove) {
                    it.remove();
                    totalWeight -= randomObject.getWeight();
                }
                return randomObject;
            }
            randomNumber -= randomObject.getWeight();
        }
        throw new RuntimeException("Cannot generator a random Object");
    }

}
