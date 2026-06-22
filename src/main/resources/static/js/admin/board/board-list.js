(function () {
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
        bindEvents();
    }

    async function loadMe() {
        try {
            const response = await fetchWithAuth('/api/members/me', { method: 'GET' });
            const result = await response.json();
            if (result.success) {
                const me = result.data;
                window.setAdminUserLabel((me.username || me.email) + ' 님');
            }
        } catch (err) {
            console.error(err);
        }
    }

    function bindEvents() {
        // 글쓰기 버튼 (TODO: 게시글 작성 페이지로 이동)
        const writeBtn = document.getElementById('writeBtn');
        if (writeBtn) {
            writeBtn.addEventListener('click', function () {
                window.location.href = '/board/write';
            });
        }

        // 검색 버튼 (TODO: API 연결)
        const searchBtn = document.getElementById('searchBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', function () {
                const type = document.getElementById('searchType').value;
                const keyword = document.getElementById('searchKeyword').value.trim();
                if (!keyword) return;
                console.log('검색:', type, keyword); // TODO: API 연결 후 대체
            });
        }

        // 엔터 검색
        const searchInput = document.getElementById('searchKeyword');
        if (searchInput) {
            searchInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    document.getElementById('searchBtn').click();
                }
            });
        }
    }
})();