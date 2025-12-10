package com.repository;

import com.model.entity.Customer;

import java.util.Optional;

/**
 * 顾客数据访问接口。
 */
public interface CustomerRepository {

    void save(Customer customer);

    Optional<Customer> findById(String id);
}
