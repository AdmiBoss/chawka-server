package com.chawka.repository;

import com.chawka.model.KhotbaShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KhotbaShareRepository extends JpaRepository<KhotbaShare, String> {
    List<KhotbaShare> findAllByOrderBySharedDateDescCreatedAtDesc();
}
