/**
 * 관리자 페이지(admin/*) 공통 레이아웃 - 탑바 + 좌측 사이드바.
 *
 * 각 admin 페이지는 <div id="adminLayout"></div> 빈 컨테이너만 두고,
 * 이 스크립트가 페이지 로드 시 탑바/사이드바 마크업을 채워 넣는다.
 *
 * 페이지 이동은 진짜 URL 이동(<a href>)으로만 한다 - SPA처럼 내부 콘텐츠만
 * JS로 바꿔치기하지 않는다. 그래야 새로고침해도 항상 같은 페이지가 그대로 다시 열린다.
 *
 * 메뉴 추가/이름 변경 등 레이아웃 수정은 이 파일 한 곳만 고치면
 * 모든 admin 페이지에 자동 반영된다.
 */
(function () {
    const MENU_ITEMS = [
        { label: 'HOME',          path: '/main',  enabled: true  },
        { label: 'COMMUNICATION', path: '/board', enabled: true  }
        // 추가 메뉴는 여기에 { label, path, enabled: true } 형태로 추가한다.
    ];

    function renderLayout() {
        const container = document.getElementById('adminLayout');
        if (!container) return;

        const currentPath = window.location.pathname;

        container.innerHTML = `
            <header class="admin-top-bar">
                <span class="admin-top-bar-title">hyunsu-delveloper</span>
                <div class="admin-top-bar-right">
                    <span class="admin-top-bar-user" id="adminUserLabel"></span>
                    <button type="button" class="btn-logout" id="adminLogoutBtn">로그아웃</button>
                </div>
            </header>
            <nav class="admin-side-bar">
                ${MENU_ITEMS.map(item => renderMenuItem(item, currentPath)).join('')}
            </nav>
        `;

        bindLogout();
    }

    function renderMenuItem(item, currentPath) {
        const isActive = item.enabled && item.path === currentPath;
        const activeClass = isActive ? ' active' : '';
        const disabledClass = item.enabled ? '' : ' disabled';

        if (!item.enabled) {
            // 비활성 메뉴는 클릭이 안 되는 자리표시자로만 둔다 (구현 예정).
            return `
                <span class="admin-menu-item${disabledClass}" title="준비 중">
                    ${item.label}
                </span>
            `;
        }

        return `
            <a href="${item.path}" class="admin-menu-item${activeClass}">
                ${item.label}
            </a>
        `;
    }

    function bindLogout() {
        const logoutBtn = document.getElementById('adminLogoutBtn');
        if (!logoutBtn) return;

        logoutBtn.addEventListener('click', async function () {
            try {
                await fetchWithAuth('/api/auth/logout', { method: 'POST' });
            } catch (err) {
                console.error(err);
            } finally {
                window.__accessToken = null;
                window.location.href = '/';
            }
        });
    }

    /**
     * 탑바 우측의 사용자 라벨을 채운다.
     * 페이지마다 표시할 정보가 다를 수 있어, 페이지 측 스크립트가 직접 호출하는 방식으로 둔다.
     */
    window.setAdminUserLabel = function (text) {
        const label = document.getElementById('adminUserLabel');
        if (label) label.textContent = text;
    };

    renderLayout();
})();