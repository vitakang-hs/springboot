(function () {
    const form = document.getElementById('loginForm');
    if (!form) return;

    // 로그인 성공 후 이동할 메인 페이지 경로. 페이지 자체는 아직 만들어지지 않았다.
    const MAIN_PAGE_PATH = '/main';

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const submitBtn = form.querySelector('.btn-primary');

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            alert('아이디와 비밀번호를 입력해 주세요.');
            return;
        }

        submitBtn.disabled = true;
        submitBtn.textContent = '로그인 중...';

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include', // Refresh Token이 담긴 쿠키를 주고받기 위해 필요
                body: JSON.stringify({ username, password })
            });
            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || '로그인 중 오류가 발생했습니다.');
            }

            // Access Token은 메모리에만 보관한다 (Refresh Token처럼 쿠키로 저장하지 않음).
            window.__accessToken = result.data.accessToken;

            window.location.href = MAIN_PAGE_PATH;
        } catch (err) {
            alert(err.message || '로그인 중 오류가 발생했습니다.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = '로그인';
        }
    });

    document.querySelectorAll('.btn-social').forEach(btn => {
        btn.addEventListener('click', function () {
            const provider = btn.dataset.provider;

            if (provider === 'google') {
                // Spring Security OAuth2 Client 표준 진입점.
                // application-local.yml의 registration: google 설정과 매칭된다.
                window.location.href = '/oauth2/authorization/google';
                return;
            }

            // TODO: 다른 소셜 로그인 제공자(카카오 등) 추가 시 분기 지점
            console.log('social login click (not implemented yet):', provider);
        });
    });
})();