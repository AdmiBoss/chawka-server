package com.chawka.repository;

import com.chawka.model.RoqiaShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoqiaShareRepository extends JpaRepository<RoqiaShare, String> {
    List<RoqiaShare> findAllByOrderBySharedDateDescCreatedAtDesc();
}
