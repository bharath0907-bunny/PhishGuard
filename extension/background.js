const API_URL = "http://localhost:8000/api/v1/analyze/url";

chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status === "complete" && tab.url && tab.url.startsWith("http")) {
    analyzeUrl(tab.url, tabId);
  }
});

async function analyzeUrl(url, tabId) {
  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url, source: "EXTENSION" })
    });

    if (response.ok) {
      const data = await response.json();
      
      // Update badge
      if (data.risk_score >= 60.0) {
        chrome.action.setBadgeText({ text: "!", tabId });
        chrome.action.setBadgeBackgroundColor({ color: "#EF4444", tabId });
        
        // Dispatch warning message to content script
        chrome.tabs.sendMessage(tabId, {
          action: "SHOW_WARNING_BANNER",
          data: data
        }).catch(() => {});
      } else if (data.risk_score >= 35.0) {
        chrome.action.setBadgeText({ text: "WARN", tabId });
        chrome.action.setBadgeBackgroundColor({ color: "#F59E0B", tabId });
      } else {
        chrome.action.setBadgeText({ text: "OK", tabId });
        chrome.action.setBadgeBackgroundColor({ color: "#22C55E", tabId });
      }

      // Store in session
      chrome.storage.local.set({ [url]: data });
    }
  } catch (e) {
    console.error("PhishGuard extension backend unreachable:", e);
  }
}
