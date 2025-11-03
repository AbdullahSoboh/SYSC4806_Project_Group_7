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
                <strong>${perk.title ?? ""}</strong><br>
                        ${perk.description ?? ""}<br>
                        ${perk.product ?? ""} • ${perk.membership ?? ""}<br>
                        <small>Location: ${perk.location?.trim() || 'Global'}</small><br>
                        <small>Expiry: ${perk.expiryDate ?? perk.expiry_date ?? "No expiry"}</small>
            </div>
        `).join('');
        perkListContainer.innerHTML = htmlContent;
    } catch (error) {
        perkListContainer.textContent = 'Failed to load perks.';
        console.error('Error fetching perks:', error);
    }
}
document.addEventListener("DOMContentLoaded", fetchAndRenderPerks);

//Test the POST
async function addPerk(e) {
  e.preventDefault();

  const perk = {
    title: document.getElementById('title').value,
    description: document.getElementById('description').value,
    product: document.getElementById('product').value,
    membership: document.getElementById('membership').value,
    location: document.getElementById('location').value.trim() || null,
    expiryDate: document.getElementById('expiryDate').value || null,
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
    await fetchAndRenderPerks(); // Refresh the perk list
  } catch (error) {
    console.error('Error adding perk:', error);
    alert('Failed to add perk.');
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("new-perk-form");
  form.addEventListener("submit", addPerk);
});

