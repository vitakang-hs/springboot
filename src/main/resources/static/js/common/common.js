/**
 * 인증이 필요한 API 호출용 공통 fetch 래퍼.
 *
 * 동작:
 *  1. window.__accessToken을 Authorization 헤더에 자동으로 실어 보낸다.
 *  2. 응답이 401(Access Token 만료/무효)이면, /api/auth/reissue를 자동 호출해 새 토큰을 받는다.
 *  3. 재발급에 성공하면 원래 요청을 새 토큰으로 한 번 더 시도한다.
 *  4. 재발급마저 실패하면(Refresh Token도 만료/무효) 로그인 페이지로 이동시킨다.
 *
 * 인증이 필요한 모든 API 호출(게시판 작성, 댓글 등)은 fetch 대신 이 함수를 사용한다.
 */
async function fetchWithAuth(url, options = {}) {
    const response = await fetch(url, withAuthHeader(options));

    if (response.status !== 401) {
        return response;
    }

    const reissued = await tryReissue();

    if (!reissued) {
        Modal.alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
        window.location.href = '/';
        // 호출부에서 더 이상 처리하지 않도록 예외를 던진다 (async 함수라 reject된 Promise가 된다)
        throw new Error('인증이 만료되어 재로그인이 필요합니다.');
    }

    // 새 토큰으로 원래 요청 재시도
    return fetch(url, withAuthHeader(options));
}

function withAuthHeader(options) {
    const headers = new Headers(options.headers || {});
    if (window.__accessToken) {
        headers.set('Authorization', `Bearer ${window.__accessToken}`);
    }
    return { ...options, headers, credentials: 'include' };
}

/**
 * Refresh Token 쿠키를 이용해 새 Access Token을 발급받아 메모리에 저장한다.
 * 성공하면 true, 실패하면 false를 반환한다.
 *
 * fetchWithAuth 내부에서 401 발생 시 자동으로 쓰이고, 페이지 진입 시
 * "이미 로그인된 상태인지" 확인하는 용도로도 재사용된다 (index.html, main.html 등).
 */
async function tryReissue() {
    try {
        const response = await fetch('/api/auth/reissue', {
            method: 'POST',
            credentials: 'include' // Refresh Token 쿠키를 보내기 위해 필요
        });

        if (!response.ok) {
            return false;
        }

        const result = await response.json();
        if (!result.success) {
            return false;
        }

        window.__accessToken = result.data.accessToken;
        return true;
    } catch (err) {
        // 네트워크 오류 등으로 재발급 요청 자체가 실패한 경우.
        // 호출부는 "로그인되지 않은 상태"와 똑같이 처리하면 되므로(로그인 페이지 유지/이동),
        // 예외를 다시 던지지 않고 false를 반환한다. 원인 파악을 위해 콘솔에만 남긴다.
        console.warn('토큰 재발급 요청 실패:', err);
        return false;
    }
}