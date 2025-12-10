package com.repository;

import com.model.entity.AccommodationBill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用内存保存住宿费账单。
 */
public class InMemoryAccommodationBillRepository implements AccommodationBillRepository {

    private final Map<String, AccommodationBill> data = new HashMap<>();

    @Override
    public void save(AccommodationBill bill) {
        data.put(bill.getRoomId(), bill);
    }

    @Override
    public Optional<AccommodationBill> findByRoomId(String roomId) {
        return Optional.ofNullable(data.get(roomId));
    }
}
