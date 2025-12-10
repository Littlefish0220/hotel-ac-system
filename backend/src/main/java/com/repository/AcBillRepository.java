package com.repository;

import com.model.entity.AcBill;

import java.util.Optional;

/**
 * 空调账单仓库接口
 */
public interface AcBillRepository {

    void save(AcBill bill);

    Optional<AcBill> findByRoomId(String roomId);
}
