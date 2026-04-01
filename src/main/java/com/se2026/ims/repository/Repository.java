package com.se2026.ims.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    void save(T item);
    Optional<T> findById(String id);
    List<T> findAll();
    void delete(String id);
    void update(T item);
    void load();
    void store();
}
