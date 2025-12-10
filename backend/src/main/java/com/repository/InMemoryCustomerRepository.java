package com.repository;

import com.model.entity.Customer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用内存保存顾客信息的简单实现。
 */
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<String, Customer> data = new HashMap<>();

    @Override
    public void save(Customer customer) {
        data.put(customer.getId(), customer);
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }
}
