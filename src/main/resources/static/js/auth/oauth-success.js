(function () {
    // 로그인 성공 후 이동할 메인 페이지 경로
    const MAIN_PAGE_PATH = '/main';

    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get('accessToken');

    if (!accessToken) {
        // 토큰 없이 이 페이지로 직접 들어온 경우 (정상적인 흐름이 아님)
        Modal.alert('로그인 처리 중 오류가 발생했습니다. 다시 시도해 주세요.');
        window.location.href = '/';
        return;
    }

    // Access Token은 메모리에만 보관한다 (로컬 로그인과 동일한 정책).
    window.__accessToken = accessToken;

    // 보안을 위해 URL에서 토큰 흔적을 제거한다.
    window.history.replaceState({}, document.title, window.location.pathname);

    window.location.href = MAIN_PAGE_PATH;
})();