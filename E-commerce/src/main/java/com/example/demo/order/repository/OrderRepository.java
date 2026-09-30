package com.example.demo.order.repository;

import com.example.demo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import com.example.demo.order.entity.OrderStatus;
import java.util.List;
import com.example.demo.security.user.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @Override
  @EntityGraph(attributePaths = "orderItems")
  List<Order> findAll();

  List<Order> findByUser(User user);
List <Order> findByStatus(OrderStatus status);
}