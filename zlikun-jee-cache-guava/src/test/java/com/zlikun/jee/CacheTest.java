package com.zlikun.jee;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author zlikun
 * @date 2018-09-19 13:26
 */
@Slf4j
public class CacheTest {

    @Test
    void usage() {

        Cache<String, String> cache = CacheBuilder.newBuilder()
                // 缓存初始容量
                .initialCapacity(2)
                // 缓存容量上限
                .maximumSize(4)
//                .expireAfterAccess(3, TimeUnit.SECONDS)
//                .expireAfterWrite(3, TimeUnit.SECONDS)
//                .refreshAfterWrite(1, TimeUnit.SECONDS)
//                .ticker(new Ticker() {
//                    @Override
//                    public long read() {
//                        return 0;
//                    }
//                })
//                .weakKeys()
//                .weakValues()
//                .softValues()
//                .concurrencyLevel(0)
//                .recordStats()
//                .removalListener((RemovalListener) notification -> { })
//                .maximumWeight(2)
//                .weigher((key, value) -> 0)
                .build();

        cache.put("A", "1");
        cache.put("B", "2");
        cache.put("C", "3");
        cache.put("D", "4");
        cache.put("E", "5");

        // 查询数据
        assertEquals("3", cache.getIfPresent("C"));
        // 超过容量后，新进数据会淘汰旧数据
        assertNull(cache.getIfPresent("A"));
        // 缓存容量
        assertEquals(4, cache.size());

        // 淘汰数据
        cache.invalidate("D");
        assertNull(cache.getIfPresent("D"));
        assertEquals(3, cache.size());

        // 批量查询数据，返回一个不可变Map集合
        ImmutableMap<String, String> immutableMap = cache.getAllPresent(Arrays.asList("A", "B", "C", "D"));
        assertEquals(2, immutableMap.size());
        assertEquals("2", immutableMap.get("B"));

        // 批量过期（淘汰），也可以指定键（迭代器）
        cache.invalidateAll();
        assertEquals(0, cache.size());

        try {
            // V get(K key, java.util.concurrent.Callable<? extends V> loader)方法
            // 用于当查询键不存在时执行loader里的逻辑，该逻辑返回一个值，该值被设置为指定键的值
            assertEquals("0", cache.get("A", () -> "0"));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // CacheStats{hitCount=0, missCount=0, loadSuccessCount=0, loadExceptionCount=0, totalLoadTime=0, evictionCount=0}
        System.out.println(cache.stats());

    }

    /**
     * 基于时间策略回收缓存
     *
     * @throws InterruptedException
     */
    @Test
    void expireAfter() throws InterruptedException {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                // 指定读续约时间
                .expireAfterAccess(300, TimeUnit.MILLISECONDS)
                // 指定写续约时间
                .expireAfterWrite(100, TimeUnit.MILLISECONDS)
                // 刷新后续约时间（这个用法不清楚）
                .refreshAfterWrite(500, TimeUnit.MILLISECONDS)
                .build();

        // 写入一个键，写入后键将于500毫秒后过期
        cache.put("A", "1");

        // 查询该键，查询后该键将于300毫秒后过期
        assertEquals("1", cache.getIfPresent("A"));

        // 休眠300毫秒
        TimeUnit.MILLISECONDS.sleep(300L);

        // 缓存应该过期
        assertNull(cache.getIfPresent("A"));

        // 再次写入
        cache.put("A", "2");
        // 将于100毫秒后过期
        TimeUnit.MILLISECONDS.sleep(100L);
        assertNull(cache.getIfPresent("A"));

        // 测试刷新缓存
        // ((LoadingCache<String, String>) cache).refresh("A");

    }

    @Test
    void removalListener() {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                // 移除键值监听器
                .removalListener(notification -> {
                    // [main] INFO com.zlikun.jee.CacheTest - key = A, value = 1, cause = EXPLICIT
                    log.info("key = {}, value = {}, cause = {}",
                            notification.getKey(), notification.getValue(), notification.getCause());
                })
                .build();

        cache.put("A", "1");
        cache.invalidate("A");
    }

    /**
     * 定时回收
     */
    @Test
    void ticker() {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                // 改变时间源（默认使用系统时钟）
                .ticker(new Ticker() {

                    @Override
                    public long read() {
                        return 0;
                    }
                })
                // 设定写入后5秒过期
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build();

        // TODO ...
        System.out.println(cache.stats());

    }

    /**
     * 基于引用回收策略
     */
    @Test
    void weak() {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .weakKeys()
                .weakValues()
                .softValues()
                .build();

        // TODO ...
        System.out.println(cache.stats());

    }

}
