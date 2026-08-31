const API_URL = "http://localhost:8000/api/v1/analyze/url";

document.addEventListener("DOMContentLoaded", async () => {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  if (tab && tab.url) {
    document.getElementById("current-url").innerText = tab.url;
    scanTabUrl(tab.url);
  }

  document.getElementById("rescan-btn").addEventListener("click", () => {
    if (tab && tab.url) scanTabUrl(tab.url);
  });
});

async function scanTabUrl(url) {
  const badgeEl = document.getElementById("badge");
  const scoreValEl = document.getElementById("score-val");
  const reasonsEl = document.getElementById("reasons-list");

  badgeEl.innerText = "SCANNING...";
  badgeEl.style.color = "#38BDF8";

  try {
    const res = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url, source: "EXTENSION_POPUP" })
    });

    if (res.ok) {
      const data = await res.json();
      scoreValEl.innerText = `${data.risk_score}%`;
      badgeEl.innerText = data.risk_level;

      if (data.risk_score >= 60) {
        badgeEl.style.background = "rgba(239, 68, 68, 0.2)";
        badgeEl.style.color = "#F87171";
        scoreValEl.style.color = "#F87171";
      } else if (data.risk_score >= 35) {
        badgeEl.style.background = "rgba(245, 158, 11, 0.2)";
        badgeEl.style.color = "#FBBF24";
        scoreValEl.style.color = "#FBBF24";
      } else {
        badgeEl.style.background = "rgba(34, 197, 94, 0.2)";
        badgeEl.style.color = "#4ADE80";
        scoreValEl.style.color = "#4ADE80";
      }

      reasonsEl.innerHTML = (data.reasons || []).map(r => `<div>• ${r}</div>`).join("");
    }
  } catch (e) {
    badgeEl.innerText = "OFFLINE";
    badgeEl.style.color = "#EF4444";
    reasonsEl.innerText = "Could not connect to PhishGuard backend engine.";
  }
}
