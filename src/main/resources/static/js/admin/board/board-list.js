/**
 * 게시글 목록 (/board).
 *
 * 검색 조건(구분/검색어/페이지)은 URL 쿼리스트링(?category=&type=&keyword=&page=)에 유지한다.
 * 그래서 상세에서 뒤로가기하거나 새로고침해도 보던 목록이 그대로 다시 열린다.
 */
(function () {
    const PAGE_SIZE = 15;
    const PAGE_GROUP_SIZE = 5; // 페이지 번호 버튼을 한 번에 몇 개 보여줄지

    const { el, formatDate, badgeClass } = BoardPage;

    init();

    async function init() {
        const me = await BoardPage.init();
        if (!me) return;

        restoreSearchInputs();
        bindEvents();
        await loadPosts();
    }

    function currentState() {
        const params = new URLSearchParams(window.location.search);
        return {
            category: params.get('category') || '',
            type: params.get('type') || 'TITLE_CONTENT',
            keyword: params.get('keyword') || '',
            page: Math.max(0, parseInt(params.get('page') || '0', 10) || 0)
        };
    }

    function navigate(changes) {
        const next = { ...currentState(), ...changes };
        const params = new URLSearchParams();
        if (next.category) params.set('category', next.category);
        if (next.keyword) {
            params.set('type', next.type);
            params.set('keyword', next.keyword);
        }
        if (next.page > 0) params.set('page', String(next.page));

        const query = params.toString();
        history.pushState(null, '', query ? `/board?${query}` : '/board');
        restoreSearchInputs();
        loadPosts();
    }

    function restoreSearchInputs() {
        const state = currentState();
        document.getElementById('searchType').value = state.type;
        document.getElementById('searchKeyword').value = state.keyword;
        document.querySelectorAll('.board-tab').forEach(tab => {
            tab.classList.toggle('board-tab--active', tab.dataset.category === state.category);
        });
    }

    async function loadPosts() {
        const state = currentState();
        const params = new URLSearchParams({ page: state.page, size: PAGE_SIZE });
        if (state.category) params.set('categoryCode', state.category);
        if (state.keyword) {
            params.set('searchType', state.type);
            params.set('keyword', state.keyword);
        }

        try {
            const page = await BoardPage.api(`/api/posts?${params}`);
            renderRows(page.content);
            renderPagination(page);
        } catch (err) {
            Modal.alert(err.message, { type: 'error' });
        }
    }

    function renderRows(posts) {
        const rows = document.getElementById('postRows');
        rows.replaceChildren(...posts.map(renderRow));
        document.getElementById('emptyMessage').hidden = posts.length > 0;
    }

    function renderRow(post) {
        const row = el('div', post.isNotice ? 'board-row board-row--notice' : 'board-row');

        const category = el('span', 'col-category');
        category.append(el('span', badgeClass(post.category), post.categoryName));

        const title = el('span', 'col-title');
        const link = el('a', 'post-title', post.title);
        link.href = `/board/${post.postId}`;
        if (post.commentCount > 0) {
            link.append(el('span', 'comment-count', `[${post.commentCount}]`));
        }
        title.append(link);

        row.append(
            category,
            title,
            el('span', 'col-author', post.authorUsername),
            el('span', 'col-date', formatDate(post.createdAt)),
            el('span', 'col-views', post.viewCount)
        );
        return row;
    }

    function renderPagination(page) {
        const container = document.getElementById('pagination');
        const totalPages = page.totalPages ?? page.page?.totalPages ?? 0;
        const current = page.number ?? page.page?.number ?? 0;

        if (totalPages <= 1) {
            container.replaceChildren();
            return;
        }

        const groupStart = Math.floor(current / PAGE_GROUP_SIZE) * PAGE_GROUP_SIZE;
        const groupEnd = Math.min(groupStart + PAGE_GROUP_SIZE, totalPages);

        const buttons = [];
        buttons.push(pageButton('<', groupStart - 1, groupStart === 0, 'page-btn page-btn--prev'));
        for (let i = groupStart; i < groupEnd; i++) {
            buttons.push(pageButton(String(i + 1), i, false,
                i === current ? 'page-btn page-btn--active' : 'page-btn'));
        }
        buttons.push(pageButton('>', groupEnd, groupEnd >= totalPages, 'page-btn page-btn--next'));

        container.replaceChildren(...buttons);
    }

    function pageButton(label, targetPage, disabled, className) {
        const button = el('button', className, label);
        button.type = 'button';
        button.disabled = disabled;
        if (!disabled && !className.includes('active')) {
            button.addEventListener('click', () => navigate({ page: targetPage }));
        }
        return button;
    }

    function bindEvents() {
        document.getElementById('writeBtn').addEventListener('click', function () {
            window.location.href = '/board/write';
        });

        document.getElementById('categoryTabs').addEventListener('click', function (e) {
            const tab = e.target.closest('.board-tab');
            if (!tab) return;
            navigate({ category: tab.dataset.category, page: 0 });
        });

        document.getElementById('searchBtn').addEventListener('click', function () {
            navigate({
                type: document.getElementById('searchType').value,
                keyword: document.getElementById('searchKeyword').value.trim(),
                page: 0
            });
        });

        document.getElementById('searchKeyword').addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.isComposing) {
                document.getElementById('searchBtn').click();
            }
        });

        // 브라우저 뒤로/앞으로 가기 시 URL 상태로 다시 그린다
        window.addEventListener('popstate', function () {
            restoreSearchInputs();
            loadPosts();
        });
    }
})();
