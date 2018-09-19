package com.zlikun.jee;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

/**
 * 缓存统计测试
 *
 * @author zlikun
 * @date 2018-09-19 17:18
 */
public class CacheStatsTest {

    @Test
    void test() throws ExecutionException {

        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                // 并发级别，同时限制同时写入缓存线程数
                .concurrencyLevel(4)
                // 初始容量
                .initialCapacity(4)
                // 最大容量
                .maximumSize(8)
                // 统计缓存的命中率
                .recordStats()
                // 自动加载
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) {
                        // 仅供测试，返回键和一个字符的ASCII值（二进制表示）
                        return Integer.toBinaryString(key.charAt(0));
                    }
                });

        for (int i = 0; i < 100; i++) {
            cache.get(String.valueOf(i & 0xf + 'A'));
        }

        // 输出统计结果：命中96次、未命中4次、#load(String)成功4次
        // CacheStats{hitCount=96, missCount=4, loadSuccessCount=4, loadExceptionCount=0, totalLoadTime=1678335, evictionCount=0}
        System.out.println(cache.stats());

    }

}
