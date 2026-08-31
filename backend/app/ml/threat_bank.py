"""
Comprehensive Threat Intelligence & Knowledge Bank for Phishing and Smishing Detection.
Contains authentic brand registries, homoglyph mapping, TLD risk registries, and pattern heuristics.
"""

# High-risk urgency, coercion, and panic-inducing phrases common in Smishing & Phishing
URGENCY_PATTERNS = [
    r"\bimmediate(ly)?\b",
    r"\bwithin\s*(24|48|12|6|2|1)\s*(hours?|hrs?|mins?|minutes?)\b",
    r"\baccount\s+(suspended|locked|restricted|blocked|compromised|frozen|disabled|terminated)\b",
    r"\baction\s+required\b",
    r"\bfailure\s+to\s+(verify|respond|update|confirm)\b",
    r"\bunauthorized\s+(access|activity|transaction|charge|login|attempt)\b",
    r"\bsecurity\s+alert\b",
    r"\bcard\s+(declined|deactivated|cancelled|blocked)\b",
    r"\blast\s+(warning|notice|reminder)\b",
    r"\bverify\s+your\s+(identity|account|card|ssn|profile|pin)\b",
    r"\burge(nt)?\s+(notice|alert|message|update)\b",
    r"\bpayment\s+(failed|overdue|pending|declined)\b",
    r"\bfinal\s+(notice|warning)\b",
    r"\blawsuit|legal\s+action|arrest\s+warrant|penalty\s+fee\b"
]

# Targeted Brand Registry with their genuine primary domains and brand variants
TARGETED_BRANDS = {
    "chase": {
        "official_domains": ["chase.com", "jpmorganchase.com", "jpmorgan.com"],
        "keywords": ["chase", "chase-bank", "chaseonline", "chase-verify", "jpmorgan"]
    },
    "wellsfargo": {
        "official_domains": ["wellsfargo.com"],
        "keywords": ["wells", "fargo", "wellsfargo", "wf-secure", "wf-alert"]
    },
    "bankofamerica": {
        "official_domains": ["bankofamerica.com", "bofa.com"],
        "keywords": ["bofa", "bankofamerica", "bofa-alert", "boa-security"]
    },
    "citi": {
        "official_domains": ["citi.com", "citigroup.com", "citibank.com"],
        "keywords": ["citibank", "citi-alert", "citionline", "citi"]
    },
    "paypal": {
        "official_domains": ["paypal.com", "paypal.me", "py.pl"],
        "keywords": ["paypal", "paypal-service", "pay-pal", "paypal-security", "pp-update", "paypa1"]
    },
    "venmo": {
        "official_domains": ["venmo.com"],
        "keywords": ["venmo", "venmo-alert", "venmo-support"]
    },
    "cashapp": {
        "official_domains": ["cash.app", "cashapp.com", "square.com"],
        "keywords": ["cashapp", "cash-app", "square-cash"]
    },
    "zelle": {
        "official_domains": ["zellepay.com"],
        "keywords": ["zelle", "zellepay", "zelle-transfer"]
    },
    "usps": {
        "official_domains": ["usps.com", "usps.gov"],
        "keywords": ["usps", "us-post", "uspstracking", "usps-redelivery", "postal-track", "usps-parcel"]
    },
    "fedex": {
        "official_domains": ["fedex.com"],
        "keywords": ["fedex", "fed-ex", "fedextracking", "fedex-delivery", "fedx"]
    },
    "ups": {
        "official_domains": ["ups.com"],
        "keywords": ["ups", "ups-tracking", "upsdelivery", "united-parcel"]
    },
    "dhl": {
        "official_domains": ["dhl.com", "dhl-express.com"],
        "keywords": ["dhl", "dhl-express", "dhl-track", "dhl-delivery"]
    },
    "amazon": {
        "official_domains": ["amazon.com", "amzn.to", "amzn.com", "aws.amazon.com"],
        "keywords": ["amazon", "amzn", "amazon-security", "amazon-orders", "amz-prime"]
    },
    "netflix": {
        "official_domains": ["netflix.com"],
        "keywords": ["netflix", "netflix-billing", "netflix-update", "netflix-payment"]
    },
    "apple": {
        "official_domains": ["apple.com", "icloud.com"],
        "keywords": ["apple", "apple-id", "icloud-verify", "apple-support", "appleid"]
    },
    "microsoft": {
        "official_domains": ["microsoft.com", "live.com", "office.com", "outlook.com", "msn.com"],
        "keywords": ["microsoft", "msft", "office365-verify", "outlook-security", "msoffice"]
    },
    "google": {
        "official_domains": ["google.com", "accounts.google.com", "gmail.com", "youtube.com"],
        "keywords": ["google", "gsuite", "google-security", "gmail-support"]
    },
    "meta": {
        "official_domains": ["facebook.com", "instagram.com", "whatsapp.com", "meta.com"],
        "keywords": ["facebook", "instagram", "whatsapp", "meta-verify", "ig-support", "fb-security"]
    },
    "irs": {
        "official_domains": ["irs.gov"],
        "keywords": ["irs", "tax-refund", "gov-stimulus", "irs-gov", "internal-revenue"]
    },
    "coinbase": {
        "official_domains": ["coinbase.com"],
        "keywords": ["coinbase", "coinbase-pro", "coinbase-wallet"]
    },
    "binance": {
        "official_domains": ["binance.com", "binance.us"],
        "keywords": ["binance", "binance-verify", "binance-alert"]
    },
    "metamask": {
        "official_domains": ["metamask.io"],
        "keywords": ["metamask", "metamask-wallet", "metamask-seed"]
    }
}

# High-risk / high-abuse TLDs heavily observed in smishing & fast-flux phishing campaigns
SUSPICIOUS_TLDS = {
    ".tk", ".ml", ".ga", ".cf", ".gq", ".top", ".xyz", ".club", ".work",
    ".buzz", ".icu", ".cam", ".rest", ".online", ".site", ".live", ".app",
    ".fit", ".surf", ".monster", ".bar", ".sbs", ".cfd", ".click", ".link",
    ".bond", ".quest", ".beauty", ".hair", ".stream", ".bid", ".loan",
    ".win", ".racing", ".cricket", ".party", ".vip", ".casa", ".cyou"
}

# Common URL Shorteners used to disguise destinations
URL_SHORTENERS = {
    "bit.ly", "tinyurl.com", "t.co", "is.gd", "buff.ly", "ow.ly", "rebrand.ly",
    "cutt.ly", "tiny.cc", "shorturl.at", "soo.gd", "s.id", "v.gd", "qr.ae",
    "rb.gy", "bl.ink", "hyperurl.co", "tr.ee"
}

# Known Safe & Whitelisted Master Domains
SAFE_DOMAINS = {
    "google.com", "accounts.google.com", "messages.google.com", "android.com", "youtube.com", "gmail.com",
    "apple.com", "icloud.com", "microsoft.com", "live.com", "office.com", "outlook.com", "msn.com",
    "amazon.com", "amzn.to", "github.com", "gitlab.com", "paypal.com", "paypal.me", "chase.com",
    "wellsfargo.com", "bankofamerica.com", "citi.com", "citibank.com", "usps.com", "fedex.com",
    "ups.com", "dhl.com", "netflix.com", "wikipedia.org", "cloudflare.com", "linkedin.com",
    "twitter.com", "x.com", "facebook.com", "instagram.com", "whatsapp.com", "slack.com",
    "zoom.us", "irs.gov", "gov.uk", "spotify.com", "uber.com", "airbnb.com", "nytimes.com",
    "cnn.com", "bbc.com", "stackoverflow.com", "reddit.com"
}

# Character Substitution / Homoglyph lookalike mapping
HOMOGLYPH_MAP = {
    '0': 'o',
    '1': 'l',
    '|': 'l',
    '!': 'i',
    '@': 'a',
    '$': 's',
    '3': 'e',
    '5': 's',
    '8': 'b',
    'vv': 'w',
    'rn': 'm'
}
