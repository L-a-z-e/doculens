package com.doculens.domain.collection.repository;

import com.doculens.domain.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    boolean existsByName(String name);
}
