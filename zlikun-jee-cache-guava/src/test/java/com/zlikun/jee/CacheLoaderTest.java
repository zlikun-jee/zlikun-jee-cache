package com.zlikun.jee;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author zlikun
 * @date 2018-09-19 16:49
 */
@Slf4j
public class CacheLoaderTest {

    @Test
    void test() throws ExecutionException {

        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                // CacheLoader，用于当检索缓存不存在时，自动加载数据（需要自实现）
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) {
                        return "key'value is empty";
                    }
                });

        // 查询一个不存在的键，会触发 CacheLoader#load(String)
        assertEquals("key'value is empty", cache.get("A"));

        // 如果使用 cache#get(String, Callable) 方法，则优先于 CacheLoader#load(String)
        assertEquals("guava", cache.get("B", () -> "guava"));

    }

    @Test
    void async() throws ExecutionException {

        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, String>() {

                    final ListeningExecutorService exec =
                            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

                    @Override
                    public String load(String key) {
                        return "key'value is empty";
                    }

                    /**
                     * 使用异步刷新机制
                     * @param key
                     * @param oldValue
                     * @return
                     */
                    @Override
                    public ListenableFuture<String> reload(String key, String oldValue) {
                        return exec.submit(() -> String.format("%s => %s", key, oldValue));
                    }
                });

        // key'value is empty
        System.out.println(cache.get("A"));

        // refresh 触发 reload 操作
        cache.refresh("A");

        // A => key'value is empty
        System.out.println(cache.get("A"));

    }

}
