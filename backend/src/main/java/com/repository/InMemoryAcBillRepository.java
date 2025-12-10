package com.repository;

import com.model.entity.AcBill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 空调账单的内存实现版本
 */
public class InMemoryAcBillRepository implements AcBillRepository {

    private final Map<String, AcBill> data = new HashMap<>();

    @Override
    public void save(AcBill bill) {
        data.put(bill.getRoomId(), bill);
    }

    @Override
    public Optional<AcBill> findByRoomId(String roomId) {
        return Optional.ofNullable(data.get(roomId));
    }
}
