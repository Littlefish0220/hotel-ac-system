package com.model.entity;

/*
 空调风速等级：
 - HIGH：1℃/分钟
 - MEDIUM：1℃/2分钟（缺省风速）
 - LOW：1℃/3分钟
*/

public enum FanSpeed {
    HIGH, // 高速风
    MEDIUM, // 中速风
    LOW // 低速风
}
