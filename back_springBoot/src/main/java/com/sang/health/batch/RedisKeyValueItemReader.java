package com.sang.health.batch;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

public class RedisKeyValueItemReader extends ItemStreamSupport implements ItemStreamReader<Map.Entry<String, String>> {

    private final RedisTemplate<String, String> redisTemplate;
    private final ScanOptions scanOptions;
    private Cursor<byte[]> cursor;
    private final int scanCount;
    private final int batchSize;
    
    // 내부적으로 키-값 쌍을 캐싱
    private Queue<Map.Entry<String, String>> cachedEntries = new LinkedList<>();

    public RedisKeyValueItemReader(RedisTemplate<String, String> redisTemplate, String pattern, int scanCount, int batchSize) {
        this.redisTemplate = redisTemplate;
        this.scanCount = scanCount;
        this.batchSize = batchSize;

        this.scanOptions = ScanOptions.scanOptions()
            .match(pattern)
            .count(scanCount)
            .build();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);
        this.cursor = redisTemplate.getConnectionFactory()
            .getConnection()
            .scan(scanOptions);
    }

    @Override
    public Map.Entry<String, String> read() throws Exception {
        // 캐시가 빈 경우
        if (cachedEntries.isEmpty()) {
            loadBatch();
        }
        
        // 캐시에서 항목 하나를 꺼내서 반환
        return cachedEntries.poll();
    }
    
    private void loadBatch() {
        List<String> keys = new ArrayList<>(batchSize);
        
        // batchSize만큼 키 수집
        int count = 0;
        while (cursor != null && cursor.hasNext() && count < batchSize) {
            keys.add(new String(cursor.next()));
            count++;
        }
        
        if (!keys.isEmpty()) {
            // 모아둔 키들의 값을 한 번에 조회
            List<String> values = redisTemplate.opsForValue().multiGet(keys);
            
            // key-value 쌍을 캐시에 저장
            for (int i = 0; i < keys.size(); i++) {
                cachedEntries.add(new AbstractMap.SimpleEntry<>(keys.get(i), values.get(i)));
            }
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // 필요시 상태 저장 가능
    }

    @Override
    public void close() throws ItemStreamException {
        if (cursor != null) {
            cursor.close();
        }
    }
}