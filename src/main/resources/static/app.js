function getMembershipSelect() {
    return document.getElementById('membership');
}

function setMembershipSelectMessage(message) {
    const select = getMembershipSelect();
    if (!select) {
        return;
    }
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
    if (!select) {
        return;
    }

    if (!Array.isArray(memberships) || memberships.length === 0) {
        setMembershipSelectMessage('No memberships available');
        return;
    }

    const previousValue = select.value;
    const desiredValue =
        preferredId !== null && preferredId !== undefined
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

    const hasDesiredSelection = memberships.some(
        (membership) => String(membership.id) === desiredValue
    );
    placeholder.selected = !hasDesiredSelection;
    placeholder.defaultSelected = !hasDesiredSelection;
    if (hasDesiredSelection) {
        select.value = desiredValue;
    } else {
        select.value = '';
    }
}

function deriveMembershipsFromPerks(perks) {
    if (!Array.isArray(perks)) {
        return [];
    }
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
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
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
        if (!response.ok) {
            throw new Error(`Membership fetch failed with status ${response.status}`);
        }
        const memberships = await response.json();
        if (Array.isArray(memberships) && memberships.length > 0) {
            populateMembershipSelect(memberships, preferredId);
            return;
        }
        console.warn('Membership endpoint returned no data; falling back to perks');
        await populateMembershipsFromPerks(preferredId);
    } catch (error) {
        console.error('Error fetching memberships:', error);
        await populateMembershipsFromPerks(preferredId);
    }
}

async function createMembership(event) {
    event.preventDefault();
    const input = document.getElementById('new-membership-name');
    const button = document.getElementById('add-membership-btn');
    if (!input || !button) {
        return;
    }

    const name = input.value.trim();
    if (!name) {
        alert('Please enter a membership name.');
        return;
    }

    button.disabled = true;
    try {
        const response = await fetch('/api/memberships', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({name})
        });
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

const SORT_OPTION_CONFIG = Object.freeze({
    mostPopular: { sortBy: 'score', direction: 'desc' },
    leastPopular: { sortBy: 'score', direction: 'asc' }
});

let activeSortSnapshot = null;
let sortWarningElement = null;
let suppressSortWarning = false;

function showSortWarning() {
    if (!sortWarningElement || suppressSortWarning) {
        return;
    }
    sortWarningElement.classList.remove('hidden');
}

function hideSortWarning() {
    if (!sortWarningElement) {
        return;
    }
    sortWarningElement.classList.add('hidden');
}

function resolveSortConfig(value) {
    return SORT_OPTION_CONFIG[value]
        ? {...SORT_OPTION_CONFIG[value]}
        : { sortBy: null, direction: null };
}

function markSortSnapshotActive(config) {
    activeSortSnapshot = config ? {...config} : null;
    hideSortWarning();
}

function clearSortSnapshot() {
    activeSortSnapshot = null;
    hideSortWarning();
}

function getPerkFilters(sortConfig = null) {
    const searchInput = document.getElementById('perk-search-input');
    const search = searchInput ? searchInput.value.trim() : '';

    const sortBy = sortConfig?.sortBy ?? null;
    const direction = sortConfig?.direction ?? null;

    return { search, sortBy, direction };
}

async function fetchAndRenderPerks(options = {}) {
    const {
        preserveScrollPosition = false,
        sortConfig = null
    } = options;
    const perkListContainer = document.getElementById('perk-list-container');
    if (!perkListContainer) {
        return;
    }
    const previousScrollPosition = preserveScrollPosition ? window.scrollY : null;
    const hasExistingContent = perkListContainer.children.length > 0;
    const previousHeight = hasExistingContent ? perkListContainer.offsetHeight : null;

    if (!hasExistingContent) {
        perkListContainer.textContent = 'Loading perks...';
    } else if (previousHeight !== null) {
        perkListContainer.style.minHeight = `${previousHeight}px`;
    }
    try {
        const { search, sortBy, direction } = getPerkFilters(sortConfig);

        // Build query string
        const params = new URLSearchParams();
        if (search) {
            params.append('search', search);
        }
        if (sortBy) {
            params.append('sortBy', sortBy);
        }
        if (direction) {
            params.append('direction', direction);
        }

        const url = '/api/perks' + (params.toString() ? `?${params.toString()}` : '');
        const response = await fetch(url); // fetch the perk data
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const perks = await response.json(); // parse JSON format

        if (!Array.isArray(perks) || perks.length === 0) {
            perkListContainer.textContent = 'No perks available.';
            return;
        }
        perkListContainer.textContent = '';

        const htmlContent = perks.map(perk => `
            <div class="perk-item" data-perk-id="${perk.id}">
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
        setMembershipSelectMessage('Unable to load memberships');
    } finally {
        if (perkListContainer) {
            perkListContainer.style.minHeight = '';
        }
    }
}

/**
 * Handles sending a vote request to the API and updating the UI.
 * @param {string} perkId - The ID of the perk to vote on.
 * @param {'upvote' | 'downvote'} voteType - The type of vote.
 */
async function handleVote(perkId, voteType) {
    const button = document.querySelector(`.${voteType}-btn[data-id="${perkId}"]`);
    if (button) {
        button.disabled = true;
    }

    try {
        const response = await fetch(`/api/perks/${perkId}/${voteType}`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error(`Failed to ${voteType}`);
        }

        // We don't actually need the body anymore, but you can leave this line:
        await response.json();

        const shouldSkipFetch = Boolean(activeSortSnapshot);
        if (shouldSkipFetch) {
            updatePerkScoreDisplay(perkId, voteType);
            showSortWarning();
        } else {
            // 🔁 Re-fetch the perk list using current search + sort
            await fetchAndRenderPerks({ preserveScrollPosition: true });
        }

    } catch (error) {
        console.error(`Error ${voteType}ing:`, error);
        alert(`Failed to record ${voteType}.`);
    } finally {
        if (button) {
            button.disabled = false;
        }
    }
}

function updatePerkScoreDisplay(perkId, voteType) {
    const scoreElement = document.getElementById(`score-${perkId}`);
    if (!scoreElement) {
        return;
    }
    const currentScore = Number(scoreElement.textContent) || 0;
    const delta = voteType === 'upvote' ? 1 : -1;
    scoreElement.textContent = String(currentScore + delta);
}

//Test the POST
async function addPerk(e) {
    e.preventDefault();

    const membershipSelect = getMembershipSelect();
    const membershipId = membershipSelect?.value;
    if (!membershipId) {
        alert('Please select a membership.');
        return;
    }

    const membershipName =
        membershipSelect.options[membershipSelect.selectedIndex]?.textContent ?? null;
    const membershipIdNumber = Number(membershipId);
    if (Number.isNaN(membershipIdNumber)) {
        alert('Invalid membership selection.');
        return;
    }

    const expiryDateValue = document.getElementById('expiryDate').value;

    if (expiryDateValue) {
        const today = new Date();
        today.setHours(0, 0, 0, 0); // Set to local midnight

        // Parse YYYY-MM-DD string
        const [year, month, day] = expiryDateValue.split('-').map(Number);
        // Create date as local midnight (month is 0-indexed)
        const localInputDate = new Date(year, month - 1, day);

        if (localInputDate < today) {
            alert('Expiry date cannot be in the past. Please select today or a future date.');
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

    if (!perk.title || !perk.description || !perk.product || !perk.membership) {
        alert('Please fill in all required fields.');
        return;
    }

    try {
        const response = await fetch('/api/perks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(perk)
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        document.getElementById('new-perk-form').reset();
        clearSortSnapshot();
        await fetchAndRenderPerks(); // Refresh the perk list (also refreshes memberships)
    } catch (error) {
        console.error('Error adding perk:', error);
        alert('Failed to add perk.');
    }
}

document.addEventListener("DOMContentLoaded", () => {
    fetchAndPopulateMemberships();
    clearSortSnapshot();
    fetchAndRenderPerks();

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
    if (form) {
        form.addEventListener("submit", addPerk);
    }

    const addMembershipButton = document.getElementById('add-membership-btn');
    if (addMembershipButton) {
        addMembershipButton.addEventListener('click', createMembership);
    }

    const perkListContainer = document.getElementById('perk-list-container');
    if (perkListContainer) {
        perkListContainer.addEventListener('click', (event) => {
            const target = event.target;
            const perkId = target.dataset.id;

            if (!perkId) {
                return;
            }

            if (target.classList.contains('upvote-btn')) {
                handleVote(perkId, 'upvote');
            } else if (target.classList.contains('downvote-btn')) {
                handleVote(perkId, 'downvote');
            }
        });
    }
    const searchInput = document.getElementById('perk-search-input');
        if (searchInput) {
            let searchTimeoutId;
            searchInput.addEventListener('input', () => {
                // small debounce so we don't spam the API on every keystroke
                clearTimeout(searchTimeoutId);
                searchTimeoutId = setTimeout(() => {
                    clearSortSnapshot();
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
                await fetchAndRenderPerks({ preserveScrollPosition: true });
                return;
            }

            const sortConfig = resolveSortConfig(sortValue);
            await fetchAndRenderPerks({ sortConfig, preserveScrollPosition: true });
            markSortSnapshotActive(sortConfig);
            sortSelect.selectedIndex = 0; // visually reset to placeholder
        });
    }
});
