package com.model.entity;

public enum RoomState {
    IDLE, // 空闲（没有入住）
    OCCUPIED, // 已入住
    AC_ON, // 空调正在使用
    AC_OFF // 空调关闭
}