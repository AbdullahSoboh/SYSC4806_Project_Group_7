let prefetchedUser = null;

async function getCurrentUser() {
    const res = await fetch("/api/current-user", { credentials: "include" });
    if (!res.ok) {
        return null;
    }
    return res.json();
}

async function checkAuth(prefetchedUser = null) {
    const user = prefetchedUser ?? await getCurrentUser();

    const loginLink = document.getElementById("login-link");
    const logoutBtn = document.getElementById("logout-btn");
    const welcome = document.getElementById("welcome");
    const profileLink = document.getElementById("profile-link");

    if (user) {
        welcome.textContent = "Hi " + user.username;
        if (loginLink) loginLink.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "inline-block";
        if (profileLink) profileLink.style.display = "inline-block";
    } else {
        if (welcome) welcome.textContent = "";
        if (loginLink) loginLink.style.display = "inline-block";
        if (logoutBtn) logoutBtn.style.display = "none";
        if (profileLink) profileLink.style.display = "none";
    }
}

async function requireAuth(options = {}) {
    const { redirectTo = "login.html" } = options;
    const user = await getCurrentUser();
    if (user) {
        prefetchedUser = user;
        return user;
    }

    const next = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.href = `${redirectTo}?next=${next}`;
    return null;
}

function promptLoginRedirect(message = "Please log in to continue.", options = {}) {
    const { next = null, redirectTo = "login.html" } = options;
    if (message) {
        alert(message);
    }
    const url = new URL(redirectTo, window.location.href);
    const nextValue = next ?? (window.location.pathname + window.location.search);
    url.searchParams.set("next", nextValue);
    window.location.href = url.toString();
}

document.addEventListener("DOMContentLoaded", () => {
    const cachedUser = prefetchedUser ?? window.__prefetchedUser ?? null;
    checkAuth(cachedUser);
    prefetchedUser = null;
    if (window) {
        window.__prefetchedUser = null;
    }

    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            await fetch("/api/logout", { method: "POST", credentials: "include" });
            checkAuth();
        });
    }
});
