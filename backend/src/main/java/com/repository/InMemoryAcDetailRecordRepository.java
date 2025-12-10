package com.repository;

import com.model.entity.AcDetailRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空调详单记录的内存实现版本
 */
public class InMemoryAcDetailRecordRepository implements AcDetailRecordRepository {

    private final List<AcDetailRecord> records = new ArrayList<>();

    @Override
    public void save(AcDetailRecord record) {
        records.add(record);
    }

    @Override
    public List<AcDetailRecord> findByRoomId(String roomId) {
        return records.stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    @Override
    public List<AcDetailRecord> findAll() {
        return new ArrayList<>(records);
    }
}
