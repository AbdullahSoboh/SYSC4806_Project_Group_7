async function checkAuth() {
    const res = await fetch("/api/current-user");

    const loginLink = document.getElementById("login-link");
    const logoutBtn = document.getElementById("logout-btn");
    const welcome = document.getElementById("welcome");
    const profileLink = document.getElementById("profile-link");

    if (res.ok) {
        const user = await res.json();
        welcome.textContent = "Hi " + user.username;
        loginLink.style.display = "none";
        logoutBtn.style.display = "inline-block";
        if (profileLink) profileLink.style.display = "inline-block";
    } else {
        welcome.textContent = "";
        loginLink.style.display = "inline-block";
        logoutBtn.style.display = "none";
        if (profileLink) profileLink.style.display = "none";
    }
}

document.addEventListener("DOMContentLoaded", () => {
    checkAuth();

    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            await fetch("/api/logout", { method: "POST" });
            checkAuth();
        });
    }
});
