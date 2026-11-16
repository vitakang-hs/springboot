/**
 * 게시글 상세 (/board/{postId}) - 본문, 수정/삭제(작성자만), 댓글 목록/작성/삭제.
 *
 * 수정/삭제 버튼은 서버 응답의 mine이 true일 때만 보여준다.
 * 버튼을 숨기는 건 화면 편의일 뿐이고, 실제 권한 검증은 서버가 한다.
 */
(function () {
    const { el, formatDate, badgeClass } = BoardPage;
    const postId = window.location.pathname.split('/')[2];

    init().catch(err => console.error(err));

    async function init() {
        const me = await BoardPage.init();
        if (!me) return;

        // 목록(/board?검색조건)에서 들어왔다면 "목록으로"가 보던 페이지로 돌아가게 한다.
        // 글쓰기(/board/write)·수정(/board/{id}/edit)에서 넘어온 경우는 기본값(/board) 그대로 둔다.
        const backUrl = listUrlFromReferrer();
        if (backUrl) {
            document.getElementById('backLink').href = backUrl;
        }

        bindEvents();
        await loadPost(true);
    }

    function listUrlFromReferrer() {
        if (!document.referrer) return null;
        try {
            const referrer = new URL(document.referrer);
            const isList = referrer.origin === window.location.origin && referrer.pathname === '/board';
            return isList ? referrer.pathname + referrer.search : null;
        } catch (e) {
            return null;
        }
    }

    async function loadPost(increaseView) {
        try {
            const post = await BoardPage.api(`/api/posts/${postId}?increaseView=${increaseView}`);
            renderPost(post);
            renderComments(post.comments);
        } catch (err) {
            Modal.alert(err.message, {
                type: 'error',
                onClose: () => { window.location.href = '/board'; }
            });
        }
    }

    function renderPost(post) {
        document.title = `${post.title} - hyunsu-delveloper`;

        const category = document.getElementById('postCategory');
        category.className = badgeClass(post.category);
        category.textContent = post.categoryName;

        document.getElementById('postTitle').textContent = post.title;
        document.getElementById('postAuthor').textContent = post.authorUsername;

        const edited = post.updatedAt && post.updatedAt.slice(0, 19) !== post.createdAt.slice(0, 19);
        document.getElementById('postDate').textContent =
            formatDate(post.createdAt, true) + (edited ? ' (수정됨)' : '');
        document.getElementById('postViews').textContent = post.viewCount;
        document.getElementById('postContent').textContent = post.content;

        document.getElementById('ownerActions').hidden = !post.mine;
        document.getElementById('postDetail').hidden = false;
        document.getElementById('commentSection').hidden = false;
    }

    function renderComments(comments) {
        document.getElementById('commentTotal').textContent = comments.length;

        const list = document.getElementById('commentList');
        if (comments.length === 0) {
            list.replaceChildren(el('li', 'comment-empty', '첫 댓글을 남겨보세요.'));
            return;
        }
        list.replaceChildren(...comments.map(renderComment));
    }

    function renderComment(comment) {
        const item = el('li', 'comment-item');

        const head = el('div', 'comment-head');
        head.append(
            el('span', 'comment-author', comment.authorUsername),
            el('span', 'comment-date', formatDate(comment.createdAt, true))
        );

        if (comment.mine) {
            const deleteBtn = el('button', 'comment-delete', '삭제');
            deleteBtn.type = 'button';
            deleteBtn.addEventListener('click', () => deleteComment(comment.commentId));
            head.append(deleteBtn);
        }

        item.append(head, el('p', 'comment-content', comment.content));
        return item;
    }

    function bindEvents() {
        document.getElementById('editBtn').addEventListener('click', function () {
            window.location.href = `/board/${postId}/edit`;
        });

        document.getElementById('deleteBtn').addEventListener('click', function () {
            Modal.confirm('게시글을 삭제하시겠습니까? 댓글도 함께 삭제됩니다.', deletePost, { danger: true });
        });

        const input = document.getElementById('commentInput');
        const submitBtn = document.getElementById('commentSubmitBtn');

        input.addEventListener('input', function () {
            document.getElementById('commentLength').textContent = input.value.length;
            submitBtn.disabled = input.value.trim().length === 0;
        });

        document.getElementById('commentForm').addEventListener('submit', async function (e) {
            e.preventDefault();
            const content = input.value.trim();
            if (!content) return;

            submitBtn.disabled = true;
            try {
                await BoardPage.api(`/api/posts/${postId}/comments`, {
                    method: 'POST',
                    body: JSON.stringify({ content })
                });
                input.value = '';
                document.getElementById('commentLength').textContent = '0';
                await loadPost(false);
            } catch (err) {
                Modal.alert(err.message, { type: 'error' });
                submitBtn.disabled = false;
            }
        });
    }

    async function deletePost() {
        try {
            await BoardPage.api(`/api/posts/${postId}`, { method: 'DELETE' });
            Modal.alert('게시글이 삭제되었습니다.', {
                type: 'success',
                onClose: () => { window.location.href = '/board'; }
            });
        } catch (err) {
            Modal.alert(err.message, { type: 'error' });
        }
    }

    function deleteComment(commentId) {
        Modal.confirm('댓글을 삭제하시겠습니까?', async function () {
            try {
                await BoardPage.api(`/api/posts/${postId}/comments/${commentId}`, { method: 'DELETE' });
                await loadPost(false);
            } catch (err) {
                Modal.alert(err.message, { type: 'error' });
            }
        }, { danger: true });
    }
})();
