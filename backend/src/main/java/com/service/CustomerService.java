package com.service;

/**
 * 顾客入住 / 退房相关的业务接口。
 */
public interface CustomerService {

    /**
     * 办理入住：创建顾客（如果需要）、创建住宿订单、修改房间状态。
     */
    void processCheckIn(String customerId,
            String customerName,
            String idNumber,
            String roomId,
            int days,
            double deposit);

    /**
     * 办理退房：设置订单离开时间、修改房间状态。
     */
    void processCheckOut(String roomId);
}
