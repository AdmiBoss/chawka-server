package com.chawka.repository;

import com.chawka.model.StoredFileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoredFileRecordRepository extends JpaRepository<StoredFileRecord, String> {
    List<StoredFileRecord> findAllByOrderByCreatedAtDesc();
}
