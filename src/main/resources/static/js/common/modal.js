/**
 * 전역 공통 모달 (alert / confirm 대체)
 *
 * 사용법:
 *   Modal.alert('저장되었습니다.');
 *   Modal.alert('오류가 발생했습니다.', { type: 'error' });
 *
 *   Modal.confirm('정말 삭제하시겠습니까?', () => {
 *       // 확인 클릭 시 실행할 콜백
 *   });
 *
 * 스크립트 한 줄만 추가하면 동작합니다:
 *   <script src="/js/common/modal.js"></script>
 */
(function () {
    'use strict';

    const STYLE_ID = 'modal-common-style';
    const ROOT_ID = 'modal-common-root';

    // ===== 스타일 주입 (최초 1회만) =====
    function injectStyle() {
        if (document.getElementById(STYLE_ID)) return;

        const style = document.createElement('style');
        style.id = STYLE_ID;
        style.textContent = `
            .modal-overlay {
                position: fixed;
                inset: 0;
                background: rgba(0, 0, 0, 0.45);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 9999;
                opacity: 0;
                transition: opacity 0.15s ease;
            }
            .modal-overlay.modal-show {
                opacity: 1;
            }
            .modal-box {
                background: #ffffff;
                border-radius: 16px;
                min-width: 340px;
                max-width: 90vw;
                padding: 28px 28px 22px;
                box-shadow: 0 20px 48px rgba(0, 0, 0, 0.28), 0 2px 8px rgba(0, 0, 0, 0.08);
                transform: translateY(-8px) scale(0.97);
                transition: transform 0.15s ease;
            }
            .modal-overlay.modal-show .modal-box {
                transform: translateY(0) scale(1);
            }
            .modal-message {
                font-size: 15px;
                line-height: 1.6;
                color: #1f2937;
                white-space: pre-line;
                margin-bottom: 22px;
            }
            .modal-actions {
                display: flex;
                justify-content: flex-end;
                gap: 8px;
            }
            .modal-btn {
                border: none;
                border-radius: 8px;
                padding: 9px 18px;
                font-size: 14px;
                font-weight: 600;
                cursor: pointer;
                transition: background 0.12s ease;
            }
            .modal-btn-cancel {
                background: #f3f4f6;
                color: #374151;
            }
            .modal-btn-cancel:hover {
                background: #e5e7eb;
            }
            .modal-btn-confirm {
                background: #4f46e5;
                color: #ffffff;
            }
            .modal-btn-confirm:hover {
                background: #4338ca;
            }
            .modal-btn-confirm.modal-btn-danger {
                background: #dc2626;
            }
            .modal-btn-confirm.modal-btn-danger:hover {
                background: #b91c1c;
            }
        `;
        document.head.appendChild(style);
    }

    // ===== 모달 렌더링 =====
    function render({ message, type, showCancel, confirmLabel, cancelLabel, danger, onConfirm, onCancel }) {
        injectStyle();

        // 기존 모달 있으면 제거 (중복 방지)
        const existing = document.getElementById(ROOT_ID);
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = ROOT_ID;
        overlay.className = 'modal-overlay';

        overlay.innerHTML = `
            <div class="modal-box" role="dialog" aria-modal="true">
                <div class="modal-message"></div>
                <div class="modal-actions">
                    ${showCancel ? `<button type="button" class="modal-btn modal-btn-cancel">${cancelLabel}</button>` : ''}
                    <button type="button" class="modal-btn modal-btn-confirm ${danger ? 'modal-btn-danger' : ''}">${confirmLabel}</button>
                </div>
            </div>
        `;

        // XSS 방지: 메시지는 textContent로 주입
        overlay.querySelector('.modal-message').textContent = message;

        document.body.appendChild(overlay);

        // 트랜지션 적용을 위해 다음 프레임에 show 클래스 부여
        requestAnimationFrame(() => overlay.classList.add('modal-show'));

        function close() {
            overlay.classList.remove('modal-show');
            setTimeout(() => overlay.remove(), 150);
            document.removeEventListener('keydown', onKeydown);
        }

        function onKeydown(e) {
            if (e.key === 'Escape') {
                close();
                if (onCancel) onCancel();
            }
        }
        document.addEventListener('keydown', onKeydown);

        overlay.querySelector('.modal-btn-confirm').addEventListener('click', () => {
            close();
            if (onConfirm) onConfirm();
        });

        const cancelBtn = overlay.querySelector('.modal-btn-cancel');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
                close();
                if (onCancel) onCancel();
            });
        }

        // 오버레이(바깥 영역) 클릭 시 닫기 — confirm은 취소로 처리
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                close();
                if (showCancel && onCancel) onCancel();
            }
        });
    }

    // ===== 공개 API =====
    const Modal = {
        /**
         * 알림 모달 (확인 버튼만 표시)
         * @param {string} message
         * @param {{type?: 'info'|'error'|'success', confirmLabel?: string, onClose?: Function}} [options]
         */
        alert(message, options = {}) {
            render({
                message,
                type: options.type || 'info',
                showCancel: false,
                confirmLabel: options.confirmLabel || '확인',
                onConfirm: options.onClose
            });
        },

        /**
         * 확인 모달 (확인/취소 버튼 표시)
         * @param {string} message
         * @param {Function} onConfirm 확인 클릭 시 실행할 콜백
         * @param {{type?: 'info'|'error'|'success', danger?: boolean, confirmLabel?: string, cancelLabel?: string, onCancel?: Function}} [options]
         */
        confirm(message, onConfirm, options = {}) {
            render({
                message,
                type: options.type || 'info',
                showCancel: true,
                confirmLabel: options.confirmLabel || '확인',
                cancelLabel: options.cancelLabel || '취소',
                danger: options.danger || false,
                onConfirm,
                onCancel: options.onCancel
            });
        }
    };

    window.Modal = Modal;
})();