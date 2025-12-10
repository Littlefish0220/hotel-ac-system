package com.service;

/**
 * 退房结账服务：串起住宿费账单 + AC 账单导出。
 */
public interface CheckoutService {

    /**
     * 完整的退房结账流程：
     * 1. 生成住宿费账单，并导出 txt；
     * 2. 生成空调 AC 账单；
     * 3. 导出 AC 详单 + AC 账单 txt。
     *
     * @param roomId              房间号
     * @param checkInTimeSeconds  入住时间（秒）
     * @param checkOutTimeSeconds 退房时间（秒）
     * @param outputDir           输出目录，比如 "output"
     */
    void processCheckOut(String roomId,
            int checkInTimeSeconds,
            int checkOutTimeSeconds,
            String outputDir);
}
