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

    const reissued = await reissueAccessToken();

    if (!reissued) {
        alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
        window.location.href = '/';
        // 호출부에서 더 이상 처리하지 않도록 reject
        return Promise.reject(new Error('인증이 만료되어 재로그인이 필요합니다.'));
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

async function reissueAccessToken() {
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
        return false;
    }
}