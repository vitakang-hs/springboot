package com.hyunsu.springboot.member.domain.repository;

import com.hyunsu.springboot.member.domain.model.entity.MemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {
}