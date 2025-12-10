package com.service;

import com.model.entity.AccommodationBill;

/**
 * 负责“住宿费账单”的业务：
 * - 生成住宿费账单（写入 AccommodationBillRepository）
 * - 导出住宿费账单 txt 文件
 */
public interface BillService {

    /**
     * 生成住宿费账单，并保存到 AccommodationBillRepository。
     * 返回生成好的账单实体，方便后续导出使用。
     */
    AccommodationBill generateAccommodationBill(String roomId);

    /**
     * 将指定房间的最新住宿费账单导出为 txt 文件。
     *
     * @param roomId    房间号
     * @param outputDir 输出目录，比如 "output"
     */
    void exportAccommodationBillTxt(String roomId, String outputDir);
}
