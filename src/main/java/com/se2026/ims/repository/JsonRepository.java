package com.se2026.ims.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.se2026.ims.model.Identifiable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonRepository<T extends Identifiable> implements Repository<T> {
    private final File file;
    private final Class<T> type;
    private final ObjectMapper mapper;
    private List<T> items = new ArrayList<>();

    public JsonRepository(String filePath, Class<T> type) {
        this.file = new File(filePath);
        this.type = type;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        load();
    }

    @Override
    public void save(T item) {
        if (findById(item.getId()).isPresent()) {
            update(item);
        } else {
            items.add(item);
            store();
        }
    }

    @Override
    public Optional<T> findById(String id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    @Override
    public void delete(String id) {
        items.removeIf(item -> item.getId().equals(id));
        store();
    }

    @Override
    public void update(T item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                break;
            }
        }
        store();
    }

    @Override
    public void load() {
        if (!file.exists()) {
            items = new ArrayList<>();
            return;
        }
        try {
            JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
            items = mapper.readValue(file, listType);
        } catch (IOException e) {
            e.printStackTrace();
            items = new ArrayList<>();
        }
    }

    @Override
    public void store() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
