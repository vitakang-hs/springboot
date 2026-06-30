(function () {
    const canvas = document.getElementById('background');
    const ctx = canvas.getContext('2d');

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }
    resize();
    window.addEventListener('resize', resize);

    const STAR_COUNT = 220;
    const stars = Array.from({ length: STAR_COUNT }, () => ({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        r: Math.random() * 1.5 + 0.3,
        opacity: Math.random() * 0.5 + 0.3
    }));

    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        const g = ctx.createRadialGradient(
            canvas.width * 0.5, canvas.height * 0.38, 0,
            canvas.width * 0.5, canvas.height * 0.38, canvas.width * 0.75
        );
        g.addColorStop(0, '#1a1240');
        g.addColorStop(0.5, '#0c0a26');
        g.addColorStop(1, '#05050f');
        ctx.fillStyle = g;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        stars.forEach(s => {
            ctx.beginPath();
            ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(230, 225, 255, ${s.opacity})`;
            ctx.fill();
        });
    }

    draw();
    window.addEventListener('resize', () => {
        resize();
        stars.forEach(s => {
            s.x = Math.random() * canvas.width;
            s.y = Math.random() * canvas.height;
        });
        draw();
    });
})();