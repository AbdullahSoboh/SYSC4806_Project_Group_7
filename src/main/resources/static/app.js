let currentViewMode = 'all'; // 'all' | 'recommended'

function getMembershipSelect() {
    return document.getElementById('membership');
}

function setMembershipSelectMessage(message) {
    const select = getMembershipSelect();
    if (!select) return;

    select.innerHTML = '';
    const option = document.createElement('option');
    option.value = '';
    option.textContent = message;
    option.disabled = true;
    option.selected = true;
    option.defaultSelected = true;
    select.appendChild(option);
    select.disabled = true;
}

function populateMembershipSelect(memberships, preferredId = null) {
    const select = getMembershipSelect();
    if (!select) return;

    if (!Array.isArray(memberships) || memberships.length === 0) {
        setMembershipSelectMessage('No memberships available');
        return;
    }

    const previousValue = select.value;
    const desiredValue = preferredId !== null && preferredId !== undefined
        ? String(preferredId)
        : previousValue;

    select.innerHTML = '';
    select.disabled = false;

    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = 'Select membership';
    placeholder.disabled = true;
    select.appendChild(placeholder);

    memberships
        .sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''))
        .forEach((membership) => {
            const option = document.createElement('option');
            option.value = String(membership.id);
            option.textContent = membership.name ?? `Membership #${membership.id}`;
            select.appendChild(option);
        });

    const hasDesiredSelection = memberships.some(m => String(m.id) === desiredValue);
    placeholder.selected = !hasDesiredSelection;
    if (hasDesiredSelection) {
        select.value = desiredValue;
    } else {
        select.value = '';
    }
}

function deriveMembershipsFromPerks(perks) {
    if (!Array.isArray(perks)) return [];
    const map = new Map();
    perks.forEach((perk) => {
        const membership = perk?.membership;
        if (membership?.id && !map.has(membership.id)) {
            map.set(membership.id, {
                id: membership.id,
                name: membership.name ?? `Membership #${membership.id}`
            });
        }
    });
    return Array.from(map.values());
}

async function populateMembershipsFromPerks(preferredId = null) {
    try {
        const response = await fetch('/api/perks');
        if (!response.ok) throw new Error('Network response was not ok');
        const perks = await response.json();
        const memberships = deriveMembershipsFromPerks(perks);
        if (memberships.length === 0) {
            setMembershipSelectMessage('No memberships available');
            return;
        }
        populateMembershipSelect(memberships, preferredId);
    } catch (fallbackError) {
        console.error('Fallback membership load failed:', fallbackError);
        setMembershipSelectMessage('Unable to load memberships');
    }
}

async function fetchAndPopulateMemberships(preferredId = null) {
    setMembershipSelectMessage('Loading memberships...');
    try {
        const response = await fetch('/api/memberships');
        if (!response.ok) throw new Error(`Membership fetch failed: ${response.status}`);
        const memberships = await response.json();
        if (Array.isArray(memberships) && memberships.length > 0) {
            populateMembershipSelect(memberships, preferredId);
            return;
        }
        console.warn('Membership endpoint empty; falling back to perks');
        await populateMembershipsFromPerks(preferredId);
    } catch (error) {
        console.error('Error fetching memberships:', error);
        await populateMembershipsFromPerks(preferredId);
    }
}

function redirectIfUnauthorized(response, message) {
    if (!response) return false;
    if (response.status === 401 || response.status === 403) {
        promptLoginRedirect(message);
        return true;
    }
    return false;
}

async function createMembership(event) {
    event.preventDefault();
    const input = document.getElementById('new-membership-name');
    const button = document.getElementById('add-membership-btn');
    if (!input || !button) return;

    const name = input.value.trim();
    if (!name) {
        alert('Please enter a membership name.');
        return;
    }

    button.disabled = true;
    try {
        const response = await fetch('/api/memberships', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({name})
        });
        if (redirectIfUnauthorized(response, 'Please log in to create memberships.')) return;

        if (!response.ok) {
            if (response.status === 409) {
                alert('That membership already exists.');
                return;
            }
            throw new Error(`Failed to create membership (status ${response.status})`);
        }
        const membership = await response.json();
        input.value = '';
        await fetchAndPopulateMemberships(membership?.id ?? null);
    } catch (error) {
        console.error('Error creating membership:', error);
        alert('Failed to create membership.');
    } finally {
        button.disabled = false;
    }
}

function setViewMode(mode) {
    currentViewMode = mode;

    // Update Tabs UI
    const tabAll = document.getElementById('tab-all');
    const tabForMe = document.getElementById('tab-forme');

    // Toggle active classes
    if (mode === 'recommended') {
        tabAll.classList.remove('active');
        tabForMe.classList.add('active');
    } else {
        tabForMe.classList.remove('active');
        tabAll.classList.add('active');
    }

    // Clear existing list and reload
    const container = document.getElementById('perk-list-container');
    if (container) container.innerHTML = '';

    // Reset sort snapshot when switching views to avoid confusion
    clearSortSnapshot();

    fetchAndRenderPerks();
}

const SORT_OPTION_CONFIG = Object.freeze({
    mostPopular: {sortBy: 'score', direction: 'desc'},
    leastPopular: {sortBy: 'score', direction: 'asc'}
});

let activeSortSnapshot = null;
let sortWarningElement = null;
let suppressSortWarning = false;

function showSortWarning() {
    if (!sortWarningElement || suppressSortWarning) return;
    sortWarningElement.classList.remove('hidden');
}

function hideSortWarning() {
    if (!sortWarningElement) return;
    sortWarningElement.classList.add('hidden');
}

function resolveSortConfig(value) {
    return SORT_OPTION_CONFIG[value]
        ? {...SORT_OPTION_CONFIG[value]}
        : {sortBy: null, direction: null};
}

function markSortSnapshotActive(config) {
    if (config && config.sortBy) {
        activeSortSnapshot = {...config};
    } else {
        activeSortSnapshot = null;
    }
    hideSortWarning();
}

function clearSortSnapshot() {
    activeSortSnapshot = null;
    hideSortWarning();
}

function getPerkFilters(sortConfig = null) {
    const searchInput = document.getElementById('perk-search-input');
    const search = searchInput ? searchInput.value.trim() : '';

    const sortBy = sortConfig?.sortBy ?? activeSortSnapshot?.sortBy ?? null;
    const direction = sortConfig?.direction ?? activeSortSnapshot?.direction ?? null;

    return {search, sortBy, direction};
}

async function fetchAndRenderPerks(options = {}) {
    const {
        preserveScrollPosition = false,
        sortConfig = null
    } = options;

    const perkListContainer = document.getElementById('perk-list-container');
    if (!perkListContainer) return;

    const previousScrollPosition = preserveScrollPosition ? window.scrollY : null;

    if (perkListContainer.children.length === 0) {
        perkListContainer.textContent = 'Loading perks...';
    }

    try {
        let perks = [];
        const {search, sortBy, direction} = getPerkFilters(sortConfig);

        // 1. FETCH DATA
        if (currentViewMode === 'recommended') {
            const url = '/api/perks/recommended';
            const response = await fetch(url);

            if (response.status === 401 || response.status === 403) {
                perkListContainer.innerHTML = `<p>Please <a href="login.html">log in</a> to view your recommended perks.</p>`;
                return;
            }
            if (!response.ok) throw new Error('Network response was not ok');
            perks = await response.json();

            // 2a. CLIENT-SIDE FILTERING (Mirrors Backend Logic)
            if (search) {
                const lowerSearch = search.toLowerCase();
                perks = perks.filter(p =>
                    (p.title && p.title.toLowerCase().includes(lowerSearch)) ||
                    (p.product && p.product.toLowerCase().includes(lowerSearch))
                );
            }

            // 2b. CLIENT-SIDE SORTING (Mirrors Backend Logic)
            if (sortBy === 'score') {
                perks.sort((a, b) => {
                    const sA = a.score || 0;
                    const sB = b.score || 0;
                    return direction === 'asc' ? sA - sB : sB - sA;
                });
            }

        } else {
            // "ALL PERKS" - Server-side Filtering/Sorting
            const params = new URLSearchParams();
            if (search) params.append('search', search);
            if (sortBy) params.append('sortBy', sortBy);
            if (direction) params.append('direction', direction);

            const url = '/api/perks' + (params.toString() ? `?${params.toString()}` : '');
            const response = await fetch(url);
            if (!response.ok) throw new Error('Network response was not ok');
            perks = await response.json();
        }

        // 3. RENDER
        if (!Array.isArray(perks) || perks.length === 0) {
            perkListContainer.textContent = currentViewMode === 'recommended'
                ? 'No recommended perks found. Add memberships to your profile!'
                : 'No perks available.';
            return;
        }
        perkListContainer.textContent = '';

        const htmlContent = perks.map(perk => `
            <div class="perk-item" id="perk-item-${perk.id}">
                <button class="vote-btn delete-btn" data-id="${perk.id}">&times;</button> 
                <div class="perk-votes">
                    <button class="vote-btn upvote-btn" data-id="${perk.id}">▲</button>
                    <span class="vote-score" id="score-${perk.id}">${perk.score ?? 0}</span>
                    <button class="vote-btn downvote-btn" data-id="${perk.id}">▼</button>
                </div>
                <div class="perk-details">
                    <strong>${perk.title ?? ""}</strong><br>
                    ${perk.description ?? ""}<br>
                    ${perk.product ?? ""} • ${perk.membership?.name ?? ""}<br>
                    <small>Location: ${perk.location?.trim() || 'Global'}</small><br>
                    <small>Expiry: ${perk.expiryDate ?? perk.expiry_date ?? "No expiry"}</small>
                </div>
            </div>
        `).join('');

        perkListContainer.innerHTML = htmlContent;
        if (preserveScrollPosition && previousScrollPosition !== null) {
            window.scrollTo(0, previousScrollPosition);
        }
    } catch (error) {
        perkListContainer.textContent = 'Failed to load perks.';
        console.error('Error fetching perks:', error);
    }
}

async function handleVote(perkId, voteType) {
    const button = document.querySelector(`.${voteType}-btn[data-id="${perkId}"]`);
    if (button) button.disabled = true;

    try {
        const response = await fetch(`/api/perks/${perkId}/${voteType}`, {method: 'POST'});

        if (redirectIfUnauthorized(response, 'Please log in to vote on perks.')) return;
        if (!response.ok) throw new Error(`Failed to ${voteType}`);

        await response.json();

        const shouldSkipFetch = Boolean(activeSortSnapshot);
        if (shouldSkipFetch) {
            updatePerkScoreDisplay(perkId, voteType);
            showSortWarning();
        } else {
            // Re-fetch using current mode
            await fetchAndRenderPerks({preserveScrollPosition: true});
        }

    } catch (error) {
        console.error(`Error ${voteType}ing:`, error);
        alert(`Failed to record ${voteType}.`);
    } finally {
        if (button) button.disabled = false;
    }
}

function updatePerkScoreDisplay(perkId, voteType) {
    const scoreElement = document.getElementById(`score-${perkId}`);
    if (!scoreElement) return;
    const currentScore = Number(scoreElement.textContent) || 0;
    const delta = voteType === 'upvote' ? 1 : -1;
    scoreElement.textContent = String(currentScore + delta);
}

async function handleDelete(perkId) {
    if (!confirm('Are you sure you want to delete this perk?')) return;

    try {
        const response = await fetch(`/api/perks/${perkId}`, {method: 'DELETE'});
        if (redirectIfUnauthorized(response, 'Please log in to delete perks.')) return;
        if (!response.ok) throw new Error(`Failed to delete perk (status ${response.status})`);

        const perkElement = document.getElementById(`perk-item-${perkId}`);
        if (perkElement) perkElement.remove();
    } catch (error) {
        console.error('Error deleting perk:', error);
        alert('Failed to delete perk.');
    }
}

async function addPerk(e) {
    e.preventDefault();

    const membershipSelect = getMembershipSelect();
    const membershipId = membershipSelect?.value;
    if (!membershipId) {
        alert('Please select a membership.');
        return;
    }

    const membershipName = membershipSelect.options[membershipSelect.selectedIndex]?.textContent ?? null;
    const membershipIdNumber = Number(membershipId);

    const expiryDateValue = document.getElementById('expiryDate').value;
    if (expiryDateValue) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const [year, month, day] = expiryDateValue.split('-').map(Number);
        const localInputDate = new Date(year, month - 1, day);
        if (localInputDate < today) {
            alert('Expiry date cannot be in the past.');
            return;
        }
    }

    const perk = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        product: document.getElementById('product').value,
        location: document.getElementById('location').value.trim() || null,
        expiryDate: expiryDateValue || null,
        membership: {
            id: membershipIdNumber,
            name: membershipName
        },
    };

    try {
        const response = await fetch('/api/perks', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(perk)
        });

        if (redirectIfUnauthorized(response, 'Please log in to add perks.')) return;
        if (!response.ok) throw new Error('Network response was not ok');

        document.getElementById('new-perk-form').reset();

        // If we added a perk, we probably want to see "All Perks" to confirm it's there
        if (currentViewMode !== 'all') {
            setViewMode('all');
        } else {
            await fetchAndRenderPerks();
        }
    } catch (error) {
        console.error('Error adding perk:', error);
        alert('Failed to add perk.');
    }
}

document.addEventListener("DOMContentLoaded", () => {
    fetchAndPopulateMemberships();
    fetchAndRenderPerks();

    // Check auth for tab visibility
    const user = localStorage.getItem('perk_user');
    if (user) {
        const tabForMe = document.getElementById('tab-forme');
        if (tabForMe) tabForMe.style.display = 'block';
    }

    // Tab Listeners
    const tabAll = document.getElementById('tab-all');
    const tabForMe = document.getElementById('tab-forme');
    if (tabAll) tabAll.addEventListener('click', () => setViewMode('all'));
    if (tabForMe) tabForMe.addEventListener('click', () => setViewMode('recommended'));

    sortWarningElement = document.getElementById('sort-sync-warning');
    const sortWarningDismissButton = document.getElementById('sort-warning-dismiss');
    if (sortWarningDismissButton) {
        sortWarningDismissButton.addEventListener('click', hideSortWarning);
    }
    const sortWarningSuppressButton = document.getElementById('sort-warning-suppress');
    if (sortWarningSuppressButton) {
        sortWarningSuppressButton.addEventListener('click', () => {
            suppressSortWarning = true;
            hideSortWarning();
        });
    }

    const form = document.getElementById("new-perk-form");
    if (form) form.addEventListener("submit", addPerk);

    const addMembershipButton = document.getElementById('add-membership-btn');
    if (addMembershipButton) addMembershipButton.addEventListener('click', createMembership);

    const perkListContainer = document.getElementById('perk-list-container');
    if (perkListContainer) {
        perkListContainer.addEventListener('click', (event) => {
            const target = event.target;
            const perkId = target.dataset.id;
            if (!perkId) return;

            if (target.classList.contains('upvote-btn')) handleVote(perkId, 'upvote');
            else if (target.classList.contains('downvote-btn')) handleVote(perkId, 'downvote');
            else if (target.classList.contains('delete-btn')) handleDelete(perkId);
        });
    }

    const searchInput = document.getElementById('perk-search-input');
    if (searchInput) {
        let searchTimeoutId;
        searchInput.addEventListener('input', () => {
            clearTimeout(searchTimeoutId);
            searchTimeoutId = setTimeout(() => {
                fetchAndRenderPerks();
            }, 300);
        });
    }

    const sortSelect = document.getElementById('perk-sort-select');
    if (sortSelect) {
        sortSelect.addEventListener('change', async () => {
            const sortValue = sortSelect.value;
            if (!sortValue) {
                clearSortSnapshot();
                sortSelect.selectedIndex = 0;
                await fetchAndRenderPerks({preserveScrollPosition: true});
                return;
            }

            const sortConfig = resolveSortConfig(sortValue);
            await fetchAndRenderPerks({sortConfig, preserveScrollPosition: true});
            markSortSnapshotActive(sortConfig);
            sortSelect.selectedIndex = 0;
        });
    }
});
