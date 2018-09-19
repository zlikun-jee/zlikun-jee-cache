package com.zlikun.jee;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

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

}
