package com.service.impl;

import java.time.Instant;
import java.util.UUID;

import com.model.entity.AccommodationOrder;
import com.model.entity.Customer;
import com.model.entity.Room;
import com.model.entity.RoomState;
import com.repository.AccommodationOrderRepository;
import com.repository.CustomerRepository;
import com.repository.RoomRepository;
import com.service.CustomerService;

/**
 * 办理入住 / 退房的具体实现。
 */
public class CustomerServiceImpl implements CustomerService {

        private final CustomerRepository customerRepository;
        private final RoomRepository roomRepository;
        private final AccommodationOrderRepository orderRepository;

        public CustomerServiceImpl(CustomerRepository customerRepository,
                        RoomRepository roomRepository,
                        AccommodationOrderRepository orderRepository) {
                this.customerRepository = customerRepository;
                this.roomRepository = roomRepository;
                this.orderRepository = orderRepository;
        }

        @Override
        public void processCheckIn(String customerId,
                        String customerName,
                        String idNumber,
                        String roomId,
                        int days,
                        double deposit) {
                // 1. 创建或更新顾客信息
                Customer customer = new Customer(customerId, customerName, idNumber);
                customerRepository.save(customer);

                // 2. 获取房间并检查状态
                Room room = roomRepository.findById(roomId);
                if (room == null) {
                        throw new IllegalArgumentException("房间不存在: " + roomId);
                }

                if (room.getState() != RoomState.IDLE && room.getState() != RoomState.AC_OFF) {
                        throw new IllegalStateException("房间当前不可入住，状态=" + room.getState());
                }

                // 3. 创建住宿订单
                long now = Instant.now().toEpochMilli();
                String orderId = UUID.randomUUID().toString();
                AccommodationOrder order = new AccommodationOrder(
                                orderId,
                                customerId,
                                roomId,
                                now,
                                days,
                                deposit);
                orderRepository.save(order);

                // 4. 更新房间信息
                room.setCurrentCustomerId(customerId);
                room.setCurrentOrderId(orderId);
                room.setState(RoomState.OCCUPIED);
                roomRepository.save(room);

                System.out.println("办理入住成功：顾客 " + customerName
                                + " 入住房间 " + roomId + "，预计天数=" + days);
        }

        @Override
        public void processCheckOut(String roomId) {
                Room room = roomRepository.findById(roomId);
                if (room == null) {
                        throw new IllegalArgumentException("房间不存在: " + roomId);
                }

                AccommodationOrder order = orderRepository.findByRoomId(roomId)
                                .orElseThrow(() -> new IllegalStateException("房间 " + roomId + " 没有找到入住订单"));

                long now = Instant.now().toEpochMilli();
                order.setCheckOutTime(now);
                orderRepository.save(order);

                // 更新房间状态为空闲待清理（这里用 IDLE 简化）
                room.setCurrentCustomerId(null);
                room.setCurrentOrderId(null);
                room.setState(RoomState.IDLE);
                roomRepository.save(room);

                System.out.println("退房成功：房间 " + roomId + " 已退房");
        }
}
