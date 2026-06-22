(function () {
    // 메모리에 Access Token이 없으면 로그인 자체를 한 적이 없는 상태이므로 즉시 로그인 페이지로 보낸다.
    // (새로고침하면 메모리가 날아가므로, 토큰 재발급(tryReissue, common.js)으로 복구를 시도한 뒤 판단한다)
    init();

    async function init() {
        if (!window.__accessToken) {
            const reissued = await tryReissue();
            if (!reissued) {
                window.location.href = '/';
                return;
            }
        }

        await loadMe();
    }

    async function loadMe() {
        try {
            const response = await fetchWithAuth('/api/members/me', { method: 'GET' });
            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || '회원 정보를 불러오지 못했습니다.');
            }

            const me = result.data;
            // 소셜 로그인 전용 계정은 username이 없으므로 이메일로 대체 표시
            // setAdminUserLabel은 admin-layout.js가 탑바에 노출하는 전역 함수
            window.setAdminUserLabel((me.username || me.email) + ' 님');
        } catch (err) {
            // fetchWithAuth가 재발급마저 실패하면 이미 로그인 페이지로 이동시키므로,
            // 여기 도달했다는 건 그 외의 예외적인 상황
            console.error(err);
        }
    }
})();