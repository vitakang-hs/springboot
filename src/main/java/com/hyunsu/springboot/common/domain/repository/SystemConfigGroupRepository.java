package com.hyunsu.springboot.common.domain.repository;

import com.hyunsu.springboot.common.domain.model.entity.SystemConfigGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemConfigGroupRepository extends JpaRepository<SystemConfigGroup, String> {

    /**
     * 설정 페이지 좌측 메뉴 렌더링용. enabled=true인 그룹만 sortOrder 순으로 조회한다.
     */
    List<SystemConfigGroup> findByEnabledTrueOrderBySortOrderAsc();
}