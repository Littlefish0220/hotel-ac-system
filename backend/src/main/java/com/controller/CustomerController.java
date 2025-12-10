package com.controller;

import com.service.CustomerService;

/**
 * 前台营业员办理入住和退房的控制器。
 */
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    public void checkIn(String customerId,
            String customerName,
            String idNumber,
            String roomId,
            int days,
            double deposit) {
        customerService.processCheckIn(customerId, customerName, idNumber, roomId, days, deposit);
    }

    public void checkOut(String roomId) {
        customerService.processCheckOut(roomId);
    }
}
