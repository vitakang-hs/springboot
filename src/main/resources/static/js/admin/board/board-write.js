/**
 * 게시글 작성(/board/write) / 수정(/board/{postId}/edit) 공용.
 *
 * 구분 select는 /api/posts/categories로 채운다 - 관리자에게만 "공지"가 포함된다.
 * 공지 작성 권한은 서버(PostWriteCommandService)가 최종 검증한다.
 */
(function () {
    const editMatch = window.location.pathname.match(/^\/board\/(\d+)\/edit$/);
    const postId = editMatch ? editMatch[1] : null;
    const isEdit = postId !== null;

    const form = document.getElementById('postForm');
    const categorySelect = document.getElementById('category');
    const titleInput = document.getElementById('title');
    const contentInput = document.getElementById('content');
    const submitBtn = document.getElementById('submitBtn');

    let dirty = false; // 작성 중 이탈 경고용

    init().catch(err => console.error(err));

    async function init() {
        const me = await BoardPage.init();
        if (!me) return;

        if (isEdit) {
            document.getElementById('pageTitle').textContent = '글 수정';
            document.title = '글 수정 - hyunsu-delveloper';
            submitBtn.textContent = '수정';
        }

        try {
            await loadCategories();
            if (isEdit) await loadPost();
        } catch (err) {
            Modal.alert(err.message, {
                type: 'error',
                onClose: () => { window.location.href = isEdit ? `/board/${postId}` : '/board'; }
            });
            return;
        }

        bindEvents();
    }

    async function loadCategories() {
        const categories = await BoardPage.api('/api/posts/categories');
        categories.forEach(category => {
            const option = BoardPage.el('option', null, category.label);
            option.value = category.code;
            categorySelect.append(option);
        });
    }

    async function loadPost() {
        const post = await BoardPage.api(`/api/posts/${postId}?increaseView=false`);
        if (!post.mine) {
            throw new Error('본인이 작성한 게시글만 수정할 수 있습니다.');
        }

        // 관리자가 아니게 된 작성자의 공지처럼, 선택지에 없는 구분이면 현재 값을 보여주기 위해 추가
        if (![...categorySelect.options].some(option => option.value === post.category)) {
            const option = BoardPage.el('option', null, post.categoryName);
            option.value = post.category;
            categorySelect.append(option);
        }

        categorySelect.value = post.category;
        titleInput.value = post.title;
        contentInput.value = post.content;
        updateLength();
    }

    function updateLength() {
        document.getElementById('contentLength').textContent = contentInput.value.length.toLocaleString();
    }

    function validate() {
        if (!categorySelect.value) return '구분을 선택해 주세요.';
        if (!titleInput.value.trim()) return '제목을 입력해 주세요.';
        if (!contentInput.value.trim()) return '내용을 입력해 주세요.';
        return null;
    }

    function bindEvents() {
        [categorySelect, titleInput, contentInput].forEach(input => {
            input.addEventListener('input', () => { dirty = true; });
        });
        contentInput.addEventListener('input', updateLength);

        document.getElementById('cancelBtn').addEventListener('click', function () {
            const back = () => { dirty = false; window.location.href = isEdit ? `/board/${postId}` : '/board'; };
            if (!dirty) return back();
            Modal.confirm('작성 중인 내용이 사라집니다. 나가시겠습니까?', back);
        });

        window.addEventListener('beforeunload', function (e) {
            if (dirty) e.preventDefault();
        });

        form.addEventListener('submit', async function (e) {
            e.preventDefault();

            const error = validate();
            if (error) {
                Modal.alert(error);
                return;
            }

            submitBtn.disabled = true;
            const body = JSON.stringify({
                category: categorySelect.value,
                title: titleInput.value.trim(),
                content: contentInput.value
            });

            try {
                if (isEdit) {
                    await BoardPage.api(`/api/posts/${postId}`, { method: 'PUT', body });
                    dirty = false;
                    window.location.href = `/board/${postId}`;
                } else {
                    const created = await BoardPage.api('/api/posts', { method: 'POST', body });
                    dirty = false;
                    window.location.href = `/board/${created.id}`;
                }
            } catch (err) {
                Modal.alert(err.message, { type: 'error' });
                submitBtn.disabled = false;
            }
        });
    }
})();
