package com.controller;

import com.model.entity.AccommodationBill;
import com.service.BillService;

/**
 * 账单控制器：现在先支持生成住宿费账单。
 */
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    public AccommodationBill generateAccommodationBill(String roomId) {
        return billService.generateAccommodationBill(roomId);
    }
}
