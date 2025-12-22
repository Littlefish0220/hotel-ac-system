package com.repository;

import com.model.entity.AccommodationOrder;

import java.util.Optional;

public interface AccommodationOrderRepository {

    void save(AccommodationOrder order);

    Optional<AccommodationOrder> findByRoomId(String roomId);
    
    // 删除指定房间的订单（退房时可用）
    void deleteByRoomId(String roomId);
}
