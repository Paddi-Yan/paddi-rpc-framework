package com.paddi.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取单例对象的工厂类
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 12:25:10
 */
public class SingletonFactory {
    private static final Map<String, Object> INSTANCES = new ConcurrentHashMap<>();

    private SingletonFactory() { }

    public static <T> T getInstance(Class<T> clazz) {
        if(clazz == null) {
            throw new IllegalArgumentException();
        }
        String key = clazz.toString();
        if(INSTANCES.containsKey(key)) {
            return clazz.cast(INSTANCES.get(key));
        }else {
            return clazz.cast(INSTANCES.computeIfAbsent(key, k -> {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch(InvocationTargetException | InstantiationException | IllegalAccessException |
                        NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }
}
