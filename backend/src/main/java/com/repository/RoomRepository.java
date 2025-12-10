package com.repository;

import java.util.List;

import com.model.entity.Room;

public interface RoomRepository {
    void save(Room room);

    Room findById(String roomId); // 改为直接返回 Room

    List<Room> findAll();

    void update(Room room);

    void delete(String roomId);

    List<Room> findAvailableRooms();
}
