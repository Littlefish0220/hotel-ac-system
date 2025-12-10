package com.model.entity;

/**
 * 顾客实体：保存顾客的基本信息。
 */
public class Customer {

    private String id; // 顾客ID（简单起见用字符串）
    private String name; // 顾客姓名
    private String idNumber; // 身份证号（可选）

    public Customer(String id, String name, String idNumber) {
        this.id = id;
        this.name = name;
        this.idNumber = idNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIdNumber() {
        return idNumber;
    }
}
