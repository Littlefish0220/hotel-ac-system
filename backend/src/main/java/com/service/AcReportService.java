package com.service;

import java.io.IOException;

/**
 * 空调账单和详单的导出服务。
 * 将某个房间的 AC 详单和汇总账单导出为 txt 文件。
 */
public interface AcReportService {

    /**
     * 导出指定房间的空调详单和账单。
     *
     * @param roomId    房间号
     * @param outputDir 输出目录路径，例如 "output" 或 "output/ac"
     */
    void exportAcReport(String roomId, String outputDir) throws IOException;
}
