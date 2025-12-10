package com.repository;

import com.model.entity.AcDetailRecord;

import java.util.List;

/**
 * 空调详单记录仓库接口
 */
public interface AcDetailRecordRepository {

    void save(AcDetailRecord record);

    List<AcDetailRecord> findByRoomId(String roomId);

    List<AcDetailRecord> findAll();
}
