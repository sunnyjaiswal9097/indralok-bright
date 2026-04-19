/* =============================================
   Indralok Bright - Main JS
   ============================================= */

document.addEventListener('DOMContentLoaded', function () {

    // ---- SIDEBAR TOGGLE ----
    const sidebar    = document.getElementById('sidebar');
    const overlay    = document.getElementById('overlay');
    const menuToggle = document.getElementById('menuToggle');
    const sidebarClose = document.getElementById('sidebarClose');

    function openSidebar() {
        if (sidebar)  sidebar.classList.add('open');
        if (overlay)  overlay.classList.add('show');
        document.body.style.overflow = 'hidden';
    }
    function closeSidebar() {
        if (sidebar)  sidebar.classList.remove('open');
        if (overlay)  overlay.classList.remove('show');
        document.body.style.overflow = '';
    }
    if (menuToggle)   menuToggle.addEventListener('click', openSidebar);
    if (sidebarClose) sidebarClose.addEventListener('click', closeSidebar);
    if (overlay)      overlay.addEventListener('click', closeSidebar);

    // ---- AUTO-DISMISS FLASH MESSAGES ----
    setTimeout(() => {
        document.querySelectorAll('.flash').forEach(el => {
            el.style.transition = 'opacity 0.5s';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 500);
        });
    }, 5000);

    // ---- TABLE SEARCH FILTER ----
    window.filterTable = function () {
        const input = document.getElementById('tableSearch');
        const filter = input.value.toLowerCase();
        const rows = document.querySelectorAll('#mainTable tbody tr');
        rows.forEach(row => {
            row.style.display = row.textContent.toLowerCase().includes(filter) ? '' : 'none';
        });
    };

    // ---- FORM VALIDATION ----
    const forms = document.querySelectorAll('form[id]');
    forms.forEach(form => {
        form.addEventListener('submit', function (e) {
            const requiredFields = form.querySelectorAll('[required]');
            let valid = true;
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    field.classList.add('invalid');
                    valid = false;
                } else {
                    field.classList.remove('invalid');
                }
            });
            if (!valid) {
                e.preventDefault();
                showToast('Please fill in all required fields.', 'error');
            }
        });
    });

    // ---- TOAST NOTIFICATION ----
    window.showToast = function (message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `flash flash-${type}`;
        toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i><span>${message}</span>`;
        toast.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999;min-width:280px;';
        document.body.appendChild(toast);
        setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 500); }, 3500);
    };

    // ---- KEYBOARD SHORTCUT: Ctrl+N = New Quotation ----
    document.addEventListener('keydown', e => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchInput = document.querySelector('.search-bar-input');
            if (searchInput) searchInput.focus();
            else window.location.href = '/search';
        }
    });

    // ---- CONFIRM BEFORE LEAVE IF FORM DIRTY ----
    const formDirtyWatch = document.querySelector('#quotationForm, #poForm');
    if (formDirtyWatch) {
        let isDirty = false;
        formDirtyWatch.querySelectorAll('input, textarea, select').forEach(el => {
            el.addEventListener('change', () => { isDirty = true; });
        });
        formDirtyWatch.addEventListener('submit', () => { isDirty = false; });
        window.addEventListener('beforeunload', e => {
            if (isDirty) {
                e.preventDefault();
                e.returnValue = '';
            }
        });
    }

    // ---- AMOUNT FORMAT ON BLUR ----
    document.querySelectorAll('.amount-display').forEach(el => {
        el.addEventListener('blur', () => {
            const v = parseFloat(el.value);
            if (!isNaN(v)) el.value = v.toFixed(2);
        });
    });

    // ---- ACTIVE NAV HIGHLIGHT ----
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-item').forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.startsWith(href) && href !== '/') {
            link.classList.add('active');
        }
    });
});
