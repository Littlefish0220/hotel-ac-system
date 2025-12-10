package com.repository;

import com.model.entity.AccommodationOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用内存保存住宿订单。
 * 简化：每个房间同一时间只有一个有效订单，用 roomId 做键。
 */
public class InMemoryAccommodationOrderRepository implements AccommodationOrderRepository {

    private final Map<String, AccommodationOrder> data = new HashMap<>();

    @Override
    public void save(AccommodationOrder order) {
        data.put(order.getRoomId(), order);
    }

    @Override
    public Optional<AccommodationOrder> findByRoomId(String roomId) {
        return Optional.ofNullable(data.get(roomId));
    }
}
