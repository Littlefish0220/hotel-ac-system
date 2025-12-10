package com.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.model.entity.Room;

public class InMemoryRoomRepository implements RoomRepository {
    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.getRoomId(), room);
    }

    @Override
    public Room findById(String roomId) {
        return rooms.get(roomId); // 直接返回，可能为 null
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }

    @Override
    public void update(Room room) {
        rooms.put(room.getRoomId(), room);
    }

    @Override
    public void delete(String roomId) {
        rooms.remove(roomId);
    }

    @Override
    public List<Room> findAvailableRooms() {
        return rooms.values().stream()
                .filter(room -> !room.isOccupied())
                .collect(Collectors.toList());
    }
}
