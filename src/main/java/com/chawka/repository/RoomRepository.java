package com.chawka.repository;

import com.chawka.model.RoomRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<RoomRecord, String> {
    List<RoomRecord> findByHostPhoneAndRoomActiveTrue(String hostPhone);
}
