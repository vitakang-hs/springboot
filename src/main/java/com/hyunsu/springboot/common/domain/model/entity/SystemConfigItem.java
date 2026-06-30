package com.hyunsu.springboot.common.domain.model.entity;

import com.hyunsu.springboot.common.domain.model.vo.ConfigValueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 설정 항목 (key-value).
 *
 * SystemConfigGroup에 속하는 개별 설정 값을 표현한다.
 * (예: LOGIN_MAX_FAIL_COUNT = "5", POST_PAGE_SIZE_OPTIONS = "10,20,50,100")
 *
 * SystemConfigGroup과 달리, value 필드는 관리자 설정 화면에서 자유롭게 수정 가능하다.
 * config_key, group_code, value_type 등 항목의 "정의" 자체는 개발 시점에 마이그레이션으로
 * 고정되며, 화면에서 바뀌는 것은 value 하나뿐이다.
 *
 * 별도 Repository를 두지 않고 group과 함께 SystemConfigGroup Aggregate에 속하는 것으로
 * 볼 수도 있으나, group_code 단위로 묶이는 것 외에는 group과 독립적으로 조회/수정되는
 * 빈도가 훨씬 높아(설정값 단건 조회/수정이 주 유스케이스) 별도 Repository
 * (SystemConfigItemRepository)를 두고 config_key를 PK로 사용한다.
 */
@Entity
@Table(name = "system_config_item")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemConfigItem {

    private static final String LIST_DELIMITER = ",";

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 10)
    private ConfigValueType valueType;

    @Column(name = "sort_order", nullable = false)
    private short sortOrder;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 설정값 변경.
     * 관리자 설정 화면에서 value를 수정할 때 사용하는 유일한 변경 메서드.
     */
    public void changeValue(String newValue) {
        if (newValue == null || newValue.isBlank()) {
            throw new IllegalArgumentException("설정값은 비어 있을 수 없습니다.");
        }
        this.value = newValue;
    }

    /**
     * value를 정수로 해석한다. valueType이 TEXT이고 숫자 형식인 항목(예: LOGIN_MAX_FAIL_COUNT)에서 사용한다.
     */
    public int valueAsInt() {
        return Integer.parseInt(value);
    }

    /**
     * value를 콤마(,)로 구분된 목록으로 해석한다. valueType이 LIST인 항목(예: POST_PAGE_SIZE_OPTIONS)에서 사용한다.
     */
    public List<String> valueAsList() {
        return List.of(value.split(LIST_DELIMITER));
    }
}