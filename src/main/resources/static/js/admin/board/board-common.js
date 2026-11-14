/**
 * 게시판 화면(목록/상세/작성) 공통 헬퍼.
 *
 * - BoardPage.init(): 로그인 확인(없으면 Refresh Token으로 재발급, 실패 시 로그인 페이지로) +
 *   내 정보(/api/members/me) 조회 + 탑바 사용자 라벨 표시. 내 정보를 반환한다.
 * - BoardPage.api(): fetchWithAuth + JSON 파싱 + 실패 시 서버 메시지로 Error throw.
 * - BoardPage.formatDate(): 서버의 LocalDateTime 문자열을 화면 표시용으로 변환.
 *
 * 사용자 입력(제목/내용/댓글/작성자명)은 innerHTML로 넣지 않고 textContent로만 넣는다 (XSS 방지).
 */
(function () {
    async function init() {
        if (!window.__accessToken) {
            const reissued = await tryReissue();
            if (!reissued) {
                window.location.href = '/';
                return null;
            }
        }

        try {
            const me = await api('/api/members/me');
            window.setAdminUserLabel((me.username || me.email) + ' 님');
            return me;
        } catch (err) {
            console.error(err);
            return null;
        }
    }

    async function api(url, options = {}) {
        const headers = { ...(options.headers || {}) };
        if (options.body && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await fetchWithAuth(url, { ...options, headers });
        const result = await response.json().catch(() => ({}));

        if (!response.ok || !result.success) {
            const error = new Error(result.message || '요청을 처리하는 중 오류가 발생했습니다.');
            error.status = response.status;
            throw error;
        }
        return result.data;
    }

    /**
     * @param {string} value  "2026-11-14T11:30:00.123"
     * @param {boolean} withTime true면 시:분까지
     * 오늘 작성된 글은 목록에서 시:분만 보여준다.
     */
    function formatDate(value, withTime = false) {
        if (!value) return '';
        const date = new Date(value);
        const pad = n => String(n).padStart(2, '0');
        const ymd = `${date.getFullYear()}.${pad(date.getMonth() + 1)}.${pad(date.getDate())}`;
        const hm = `${pad(date.getHours())}:${pad(date.getMinutes())}`;

        if (withTime) return `${ymd} ${hm}`;

        const today = new Date();
        const isToday = date.toDateString() === today.toDateString();
        return isToday ? hm : ymd;
    }

    /** 태그 생성 헬퍼: el('span', 'badge', '공지') */
    function el(tag, className, text) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined && text !== null) node.textContent = text;
        return node;
    }

    function badgeClass(category) {
        return category === 'NOTICE' ? 'badge badge--notice'
            : category === 'QNA' ? 'badge badge--qna'
            : 'badge badge--free';
    }

    window.BoardPage = { init, api, formatDate, el, badgeClass };
})();
