package com.hyunsu.springboot.common.domain.repository;

import com.hyunsu.springboot.common.domain.model.entity.Code;
import com.hyunsu.springboot.common.domain.model.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeRepository extends JpaRepository<Code, CodeId> {

    /**
     * 특정 그룹의 활성 코드를 정렬 순서대로 조회.
     * 화면에서 구분 선택박스 등을 채울 때 사용.
     */
    List<Code> findByIdGroupCodeAndEnabledTrueOrderBySortOrderAsc(String groupCode);

    /**
     * 특정 그룹의 코드값 하나 조회 (유효성 검증용).
     */
    boolean existsByIdGroupCodeAndIdCodeAndEnabledTrue(String groupCode, String code);
}