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

async function fetchAndRenderPerks() {
    const perkListContainer = document.getElementById('perk-list-container');
    perkListContainer.textContent = 'Loading perks...';
    try {
        const response = await fetch('/api/perks'); // fetch the perk data
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
            <div class="perk-item">
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
    } catch (error) {
        perkListContainer.textContent = 'Failed to load perks.';
        console.error('Error fetching perks:', error);
        setMembershipSelectMessage('Unable to load memberships');
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

        const updatedPerk = await response.json();

        const scoreElement = document.getElementById(`score-${perkId}`);
        if (scoreElement && updatedPerk.score !== undefined) {
            scoreElement.textContent = updatedPerk.score;
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

    const perk = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        product: document.getElementById('product').value,
        location: document.getElementById('location').value.trim() || null,
        expiryDate: document.getElementById('expiryDate').value || null,
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
        await fetchAndRenderPerks(); // Refresh the perk list (also refreshes memberships)
    } catch (error) {
        console.error('Error adding perk:', error);
        alert('Failed to add perk.');
    }
}

document.addEventListener("DOMContentLoaded", () => {
    fetchAndPopulateMemberships();
    fetchAndRenderPerks();

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
});
