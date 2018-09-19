package com.zlikun.jee;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * 由于本库基于Guava改进而来，所以API非常接近
 *
 * @author zlikun
 * @date 2018-09-19 17:30
 */
public class CacheTest {

    @Test
    void usage() {

        // https://github.com/ben-manes/caffeine/wiki
        LoadingCache<String, Object> cache = Caffeine.newBuilder()
                .maximumSize(8)
                .expireAfterWrite(128, TimeUnit.MILLISECONDS)
                .recordStats()
                .build(key -> String.format("%s => empty", key));

        // 循环遍历：[A, B, C, D, ..., P]
        Stream.iterate(0, x -> x + 1)
                .limit(64)
                .map(x -> String.valueOf((char) ((x & 0xf) + 'A')))
                .forEach(key -> System.out.println(cache.get(key)));

        // 打印统计信息
        // CacheStats{hitCount=38, missCount=26, loadSuccessCount=26, loadFailureCount=0, totalLoadTime=1035265, evictionCount=18, evictionWeight=18}
        System.out.println(cache.stats());

    }

}
