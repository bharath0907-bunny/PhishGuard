chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "SHOW_WARNING_BANNER") {
    injectWarningBanner(request.data);
  }
});

function injectWarningBanner(data) {
  if (document.getElementById("phishguard-warning-banner")) return;

  const banner = document.createElement("div");
  banner.id = "phishguard-warning-banner";
  banner.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 2147483647;
    background: #991B1B;
    color: #FFFFFF;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    padding: 14px 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 4px 20px rgba(0,0,0,0.4);
    font-size: 14px;
    font-weight: 500;
  `;

  banner.innerHTML = `
    <div style="display: flex; align-items: center; gap: 12px;">
      <span style="font-size: 20px;">🚨</span>
      <div>
        <strong style="font-weight: 700; text-transform: uppercase;">PhishGuard Security Alert:</strong>
        This website is identified as a <strong>${data.prediction}</strong> (${data.risk_score}% Risk). 
        <span style="opacity: 0.9;">${data.reasons?.[0] || 'Suspicious indicators detected.'}</span>
      </div>
    </div>
    <div style="display: flex; gap: 10px;">
      <button id="phishguard-leave-btn" style="background: #FFFFFF; color: #991B1B; border: none; padding: 6px 14px; border-radius: 6px; font-weight: 700; cursor: pointer;">Leave Site</button>
      <button id="phishguard-ignore-btn" style="background: transparent; color: #FFFFFF; border: 1px solid rgba(255,255,255,0.4); padding: 6px 12px; border-radius: 6px; cursor: pointer;">Ignore</button>
    </div>
  `;

  document.body.prepend(banner);

  document.getElementById("phishguard-leave-btn")?.addEventListener("click", () => {
    window.location.href = "about:blank";
  });

  document.getElementById("phishguard-ignore-btn")?.addEventListener("click", () => {
    banner.remove();
  });
}
