const UI = {
    authSection: document.getElementById('nav-auth-section'),
    guestSection: document.getElementById('nav-guest-section'),
    usernameDisplay: document.getElementById('nav-username'),
    logoutBtn: document.getElementById('nav-logout')
};

function updateNavUI(user) {
    if (user) {
        // User is logged in
        if (UI.guestSection) UI.guestSection.classList.add('hidden');
        if (UI.authSection) UI.authSection.classList.remove('hidden');
        if (UI.usernameDisplay) UI.usernameDisplay.textContent = `Hello, ${user.username}`;
    } else {
        // User is logged out
        if (UI.authSection) UI.authSection.classList.add('hidden');
        if (UI.guestSection) UI.guestSection.classList.remove('hidden');
    }
}

async function checkAuth() {
    const storedUser = localStorage.getItem('perk_user');
    if (storedUser) {
        try {
            updateNavUI(JSON.parse(storedUser));
        } catch (e) {
            console.warn("Corrupt user data in local storage");
            localStorage.removeItem('perk_user');
            updateNavUI(null);
        }
    } else {
        updateNavUI(null);
    }

    try {
        const res = await fetch("/api/current-user", {credentials: "include"});
        if (res.ok) {
            const freshUser = await res.json();
            localStorage.setItem('perk_user', JSON.stringify(freshUser));
            updateNavUI(freshUser);
            return freshUser;
        } else {
            localStorage.removeItem('perk_user');
            updateNavUI(null);
            return null;
        }
    } catch (err) {
        console.warn("Auth check failed (network error?)", err);
        return null;
    }
}

async function handleLogout(e) {
    if (e) e.preventDefault();
    try {
        await fetch("/api/logout", {method: "POST", credentials: "include"});
    } catch (err) {
        console.error("Logout failed", err);
    } finally {
        localStorage.removeItem('perk_user');
        window.location.href = "login.html";
    }
}

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
    checkAuth();

    const logoutBtn = document.getElementById('nav-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener("click", handleLogout);
    }
});

// Helper for other pages to require auth
async function requireAuth(redirectTo = "login.html") {
    // Wait for the server check to confirm validity
    const user = await checkAuth();
    if (!user) {
        const next = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `${redirectTo}?next=${next}`;
    }
    return user;
}

function promptLoginRedirect(message = "Please log in to continue.", options = {}) {
    const {next = null, redirectTo = "login.html"} = options;
    if (message) {
        alert(message);
    }
    localStorage.removeItem('perk_user');

    const url = new URL(redirectTo, window.location.href);
    const nextValue = next ?? (window.location.pathname + window.location.search);
    url.searchParams.set("next", nextValue);
    window.location.href = url.toString();
}
