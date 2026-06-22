(function () {
    const form = document.getElementById('signupForm');
    if (!form) return;

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('passwordConfirm');
    const emailInput = document.getElementById('email');
    const emailCodeInput = document.getElementById('emailCode');

    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');
    const passwordConfirmError = document.getElementById('passwordConfirmError');
    const emailCodeError = document.getElementById('emailCodeError');
    const codeTimerEl = document.getElementById('codeTimer');

    const checkUsernameBtn = document.getElementById('checkUsernameBtn');
    const sendCodeBtn = document.getElementById('sendCodeBtn');
    const verifyCodeBtn = document.getElementById('verifyCodeBtn');
    const submitBtn = document.getElementById('submitBtn');

    const USERNAME_MIN_LENGTH = 4;
    const USERNAME_REGEX = /^[A-Za-z0-9]+$/; // 영문/숫자만 허용 (한글 불가)
    const PASSWORD_MIN_LENGTH = 6;
    const CODE_VALID_SECONDS = 180; // 3분
    const RESEND_COOLDOWN_SECONDS = 60;
    const EMAIL_CODE_REGEX = /^[0-9]{6}$/;

    const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    let usernameVerified = false;     // 아이디 중복확인 통과 여부
    let emailCodeIssued = false;      // 인증코드 발송 완료 여부
    let emailCodeVerified = false;    // 인증코드 검증 통과 여부 (회원가입 버튼 활성화의 진짜 조건)
    let codeTimerInterval = null;
    let resendCooldownInterval = null;

    function showError(el, message) {
        el.textContent = message;
        el.classList.toggle('show', Boolean(message));
    }

    // 회원가입 버튼 활성화 조건을 매번 전체 재계산한다.
    // (개별 플래그를 여기저기서 따로 갱신하는 대신, 한 군데에서 모든 조건을 다시 확인하는 방식 -
    //  특정 입력값 변경 시 갱신을 빠뜨려 버튼이 잘못 활성화되는 사고를 방지)
    function updateSubmitButton() {
        const isUsernameOk = usernameFormatValid() && usernameVerified;
        const isPasswordOk = passwordFormatValid();
        const isPasswordConfirmOk = passwordConfirmMatches();
        const isEmailOk = emailCodeVerified;

        submitBtn.disabled = !(isUsernameOk && isPasswordOk && isPasswordConfirmOk && isEmailOk);
    }

    // ===== 아이디 =====

    function usernameFormatValid() {
        const value = usernameInput.value.trim();
        return value.length >= USERNAME_MIN_LENGTH && USERNAME_REGEX.test(value);
    }

    function validateUsername() {
        const value = usernameInput.value.trim();

        if (value.length === 0) {
            showError(usernameError, '');
            usernameInput.classList.remove('invalid');
            checkUsernameBtn.disabled = true;
            return false;
        }

        if (!USERNAME_REGEX.test(value)) {
            showError(usernameError, '아이디는 영문, 숫자만 사용할 수 있습니다.');
            usernameInput.classList.add('invalid');
            checkUsernameBtn.disabled = true;
            return false;
        }

        if (value.length < USERNAME_MIN_LENGTH) {
            showError(usernameError, `아이디는 ${USERNAME_MIN_LENGTH}자 이상이어야 합니다.`);
            usernameInput.classList.add('invalid');
            checkUsernameBtn.disabled = true;
            return false;
        }

        showError(usernameError, '');
        usernameInput.classList.remove('invalid');
        checkUsernameBtn.disabled = false;
        return true;
    }

    usernameInput.addEventListener('input', function () {
        if (usernameVerified) {
            usernameVerified = false;
            checkUsernameBtn.textContent = '중복확인';
            checkUsernameBtn.classList.remove('btn-inline-success');
            updateSubmitButton();
        }
        validateUsername();
    });

    checkUsernameBtn.addEventListener('click', async function () {
        if (!usernameFormatValid()) return;

        const username = usernameInput.value.trim();

        checkUsernameBtn.disabled = true;
        checkUsernameBtn.textContent = '확인 중...';

        try {
            const response = await fetch(`/api/members/check-username?username=${encodeURIComponent(username)}`, {
                method: 'GET'
            });
            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || '중복확인 중 오류가 발생했습니다.');
            }

            const isAvailable = result.data.available;

            if (isAvailable) {
                usernameVerified = true;
                checkUsernameBtn.textContent = '사용가능';
                checkUsernameBtn.disabled = true;
                checkUsernameBtn.classList.add('btn-inline-success');
                showError(usernameError, '');
            } else {
                usernameVerified = false;
                checkUsernameBtn.textContent = '중복확인';
                checkUsernameBtn.disabled = false;
                showError(usernameError, '이미 사용 중인 아이디입니다.');
                usernameInput.classList.add('invalid');
            }
        } catch (err) {
            usernameVerified = false;
            checkUsernameBtn.textContent = '중복확인';
            checkUsernameBtn.disabled = false;
            showError(usernameError, err.message || '중복확인 중 오류가 발생했습니다.');
        }

        updateSubmitButton();
    });

    // ===== 비밀번호 =====

    // 순수 판정 함수 (에러 메시지 표시 없이 boolean만 반환) - updateSubmitButton에서 재사용
    function passwordFormatValid() {
        return passwordInput.value.length >= PASSWORD_MIN_LENGTH;
    }

    function passwordConfirmMatches() {
        return passwordConfirmInput.value.length > 0
            && passwordConfirmInput.value === passwordInput.value;
    }

    function validatePassword() {
        const value = passwordInput.value;
        if (value.length === 0) {
            showError(passwordError, '');
            passwordInput.classList.remove('invalid');
            return false;
        }
        if (!passwordFormatValid()) {
            showError(passwordError, `비밀번호는 ${PASSWORD_MIN_LENGTH}자 이상이어야 합니다.`);
            passwordInput.classList.add('invalid');
            return false;
        }
        showError(passwordError, '');
        passwordInput.classList.remove('invalid');
        return true;
    }

    function validatePasswordConfirm() {
        const value = passwordConfirmInput.value;
        if (value.length === 0) {
            showError(passwordConfirmError, '');
            passwordConfirmInput.classList.remove('invalid');
            return false;
        }
        if (!passwordConfirmMatches()) {
            showError(passwordConfirmError, '비밀번호가 일치하지 않습니다.');
            passwordConfirmInput.classList.add('invalid');
            return false;
        }
        showError(passwordConfirmError, '');
        passwordConfirmInput.classList.remove('invalid');
        return true;
    }

    passwordInput.addEventListener('input', function () {
        validatePassword();
        if (passwordConfirmInput.value.length > 0) {
            validatePasswordConfirm();
        }
        updateSubmitButton();
    });

    passwordConfirmInput.addEventListener('input', function () {
        validatePasswordConfirm();
        updateSubmitButton();
    });

    // ===== 이메일 / 인증코드 발송 =====

    function validateEmail() {
        const value = emailInput.value.trim();
        const isValid = EMAIL_REGEX.test(value);
        sendCodeBtn.disabled = !isValid;
        return isValid;
    }

    // 이메일이 바뀌면 인증 관련 상태 전부 초기화 (다른 이메일로 바꿨는데 이전 인증이 유효한 척하면 안 되므로)
    emailInput.addEventListener('input', function () {
        validateEmail();
        if (emailCodeIssued || emailCodeVerified) {
            resetEmailVerificationState();
        }
    });

    function resetEmailVerificationState() {
        emailCodeIssued = false;
        emailCodeVerified = false;
        emailCodeInput.value = '';
        emailCodeInput.disabled = true;
        verifyCodeBtn.disabled = true;
        verifyCodeBtn.textContent = '확인';
        verifyCodeBtn.classList.remove('btn-inline-success');
        codeTimerEl.classList.remove('show', 'expired');
        if (codeTimerInterval) clearInterval(codeTimerInterval);
        showError(emailCodeError, '');

        // 발송 버튼도 함께 초기화 (재발송 쿨다운 텍스트가 남아있는 버그 방지)
        if (resendCooldownInterval) clearInterval(resendCooldownInterval);
        sendCodeBtn.textContent = '인증코드 발송';
        // 활성화 여부는 새 이메일 형식이 유효한지에 따라 호출부(emailInput input 핸들러)에서 다시 판단됨

        updateSubmitButton();
    }

    function startCodeTimer() {
        let remaining = CODE_VALID_SECONDS;

        codeTimerEl.classList.remove('expired');
        codeTimerEl.classList.add('show');

        if (codeTimerInterval) clearInterval(codeTimerInterval);

        function render() {
            const m = String(Math.floor(remaining / 60)).padStart(2, '0');
            const s = String(remaining % 60).padStart(2, '0');
            codeTimerEl.textContent = `인증코드 유효시간 ${m}:${s}`;
        }

        render();

        codeTimerInterval = setInterval(function () {
            remaining -= 1;
            if (remaining <= 0) {
                clearInterval(codeTimerInterval);
                codeTimerEl.textContent = '인증코드가 만료되었습니다. 다시 발송해 주세요.';
                codeTimerEl.classList.add('expired');
                emailCodeInput.disabled = true;
                emailCodeInput.value = '';
                verifyCodeBtn.disabled = true;
                emailCodeIssued = false;
                emailCodeVerified = false;
                updateSubmitButton();
                return;
            }
            render();
        }, 1000);
    }

    sendCodeBtn.addEventListener('click', async function () {
        if (!validateEmail()) return;

        const email = emailInput.value.trim();

        sendCodeBtn.disabled = true;
        sendCodeBtn.textContent = '발송 중...';

        try {
            const response = await fetch(`/api/members/email/send-code?email=${encodeURIComponent(email)}`, {
                method: 'POST'
            });
            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || '인증코드 발송 중 오류가 발생했습니다.');
            }

            // 테스트용: 서버가 발급한 코드를 alert으로 표시 (실제 메일 발송 연동 전까지의 임시 조치)
            alert(`[테스트용] 발급된 인증코드: ${result.data.code}`);

            emailCodeInput.disabled = false;
            emailCodeInput.value = '';
            verifyCodeBtn.disabled = false;
            emailCodeIssued = true;
            emailCodeVerified = false;
            updateSubmitButton();

            startCodeTimer();

            // 재발송 쿨다운 (60초)
            if (resendCooldownInterval) clearInterval(resendCooldownInterval);
            let cooldown = RESEND_COOLDOWN_SECONDS;
            sendCodeBtn.textContent = `재발송 (${cooldown}초)`;
            resendCooldownInterval = setInterval(function () {
                cooldown -= 1;
                sendCodeBtn.textContent = `재발송 (${cooldown}초)`;
                if (cooldown <= 0) {
                    clearInterval(resendCooldownInterval);
                    sendCodeBtn.textContent = '인증코드 발송';
                    sendCodeBtn.disabled = !validateEmail();
                }
            }, 1000);
        } catch (err) {
            sendCodeBtn.disabled = !validateEmail();
            sendCodeBtn.textContent = '인증코드 발송';
            alert(err.message || '인증코드 발송 중 오류가 발생했습니다.');
        }
    });

    // 인증코드 입력값이 바뀌면 검증 상태 초기화
    emailCodeInput.addEventListener('input', function () {
        if (emailCodeVerified) {
            emailCodeVerified = false;
            verifyCodeBtn.textContent = '확인';
            verifyCodeBtn.classList.remove('btn-inline-success');
            updateSubmitButton();
        }
        verifyCodeBtn.disabled = !EMAIL_CODE_REGEX.test(emailCodeInput.value.trim());
        showError(emailCodeError, '');
    });

    // ===== 인증코드 검증 =====

    verifyCodeBtn.addEventListener('click', async function () {
        const email = emailInput.value.trim();
        const code = emailCodeInput.value.trim();

        if (!EMAIL_CODE_REGEX.test(code)) return;

        verifyCodeBtn.disabled = true;
        verifyCodeBtn.textContent = '확인 중...';

        try {
            const response = await fetch('/api/members/email/verify-code', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, code })
            });
            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || '인증코드 확인 중 오류가 발생했습니다.');
            }

            const isVerified = result.data;

            if (isVerified) {
                emailCodeVerified = true;
                verifyCodeBtn.textContent = '인증완료';
                verifyCodeBtn.disabled = true;
                verifyCodeBtn.classList.add('btn-inline-success');
                emailCodeInput.disabled = true;
                showError(emailCodeError, '');
                if (codeTimerInterval) clearInterval(codeTimerInterval);
                codeTimerEl.classList.remove('show');
            } else {
                emailCodeVerified = false;
                verifyCodeBtn.textContent = '확인';
                verifyCodeBtn.disabled = false;
                showError(emailCodeError, '인증코드가 일치하지 않습니다.');
            }
        } catch (err) {
            emailCodeVerified = false;
            verifyCodeBtn.textContent = '확인';
            verifyCodeBtn.disabled = false;
            showError(emailCodeError, err.message || '인증코드 확인 중 오류가 발생했습니다.');
        }

        updateSubmitButton();
    });

    // ===== 제출 =====

    form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const usernameValid = validateUsername();
        const passwordValid = validatePassword();
        const passwordConfirmValid = validatePasswordConfirm();

        if (!usernameValid || !passwordValid || !passwordConfirmValid) {
            return;
        }

        if (!usernameVerified) {
            alert('아이디 중복확인을 먼저 진행해 주세요.');
            return;
        }

        if (!emailCodeVerified) {
            alert('이메일 인증을 먼저 완료해 주세요.');
            return;
        }

        const username = usernameInput.value.trim();
        const password = passwordInput.value;
        const email = emailInput.value.trim();

        submitBtn.disabled = true;
        submitBtn.textContent = '가입 처리 중...';

        try {
            const response = await fetch('/api/members/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password, email })
            });
            const result = await response.json();

            if (!response.ok || !result.success) {
                throw new Error(result.message || '회원가입 중 오류가 발생했습니다.');
            }

            alert('회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.');
            window.location.href = '/';
        } catch (err) {
            alert(err.message || '회원가입 중 오류가 발생했습니다.');
            submitBtn.disabled = false;
            submitBtn.textContent = '회원가입';
        }
    });

    // 초기 로드 시점 버튼 상태 확정 (HTML에 disabled가 이미 있지만 명시적으로 한 번 더 확인)
    updateSubmitButton();
})();