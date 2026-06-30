package com.hyunsu.springboot.common.domain.model.vo;

/**
 * 설정 항목(SystemConfigItem)의 값 타입.
 *
 * 관리자 설정 화면에서 value를 어떤 입력 UI로 보여줄지 결정하는 기준이 된다.
 *
 * TEXT: 단일 텍스트/숫자 값 (예: "5") - 일반 텍스트 입력창으로 렌더링
 * LIST: 콤마(,)로 구분된 목록 값 (예: "10,20,50,100") - 목록 입력 UI로 렌더링
 */
public enum ConfigValueType {
    TEXT,
    LIST
}