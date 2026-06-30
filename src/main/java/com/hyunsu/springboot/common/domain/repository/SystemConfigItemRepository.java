package com.hyunsu.springboot.common.domain.repository;

import com.hyunsu.springboot.common.domain.model.entity.SystemConfigItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemConfigItemRepository extends JpaRepository<SystemConfigItem, String> {

    /**
     * 설정 페이지에서 특정 그룹을 선택했을 때, 그 그룹에 속한 항목들을 sortOrder 순으로 조회한다.
     */
    List<SystemConfigItem> findByGroupCodeOrderBySortOrderAsc(String groupCode);

    /**
     * 코드 곳곳(LoginCommandService 등)에서 설정값 하나를 읽어올 때 사용한다.
     * (configKey가 PK이므로 사실 findById와 동일하지만, 호출부에서 의미가 더 잘 드러나도록 별도 메서드명을 둔다)
     */
    Optional<SystemConfigItem> findByConfigKey(String configKey);
}