import re
import math
import urllib.parse
import ipaddress
from typing import Dict, Any, List, Optional
import tldextract
from .threat_bank import SUSPICIOUS_TLDS, URL_SHORTENERS, TARGETED_BRANDS, SAFE_DOMAINS, HOMOGLYPH_MAP

# Setup tldextract extractor instance
tld_extractor = tldextract.TLDExtract(cache_dir=None, suffix_list_urls=())

def calculate_entropy(text: str) -> float:
    """Calculates Shannon Entropy of a string to detect randomized/generated domains or paths."""
    if not text:
        return 0.0
    entropy = 0.0
    length = len(text)
    freq = {}
    for char in text:
        freq[char] = freq.get(char, 0) + 1
    for count in freq.values():
        p = count / length
        entropy -= p * math.log2(p)
    return round(entropy, 4)

def is_ip_address(hostname: str) -> bool:
    """Checks if hostname is an IPv4 or IPv6 address."""
    host = hostname.split(":")[0].strip("[]")
    try:
        ipaddress.ip_address(host)
        return True
    except ValueError:
        return False

def levenshtein_distance(s1: str, s2: str) -> int:
    """Computes Levenshtein edit distance between two strings."""
    if len(s1) < len(s2):
        return levenshtein_distance(s2, s1)
    if len(s2) == 0:
        return len(s1)

    previous_row = range(len(s2) + 1)
    for i, c1 in enumerate(s1):
        current_row = [i + 1]
        for j, c2 in enumerate(s2):
            insertions = previous_row[j + 1] + 1
            deletions = current_row[j] + 1
            substitutions = previous_row[j] + (c1 != c2)
            current_row.append(min(insertions, deletions, substitutions))
        previous_row = current_row
    return previous_row[-1]

def normalize_homoglyphs(text: str) -> str:
    """Normalizes visual character substitutions (e.g. paypa1 -> paypal, rn -> m)."""
    norm = text.lower()
    for char, rep in HOMOGLYPH_MAP.items():
        norm = norm.replace(char, rep)
    return norm

def detect_typosquatting(domain_name: str) -> Optional[str]:
    """
    Detects if a domain name is a typosquatted or homoglyph impersonation of a protected brand.
    """
    clean_domain = domain_name.lower().replace("-", "").replace(".", "")
    normalized_domain = normalize_homoglyphs(clean_domain)
    
    for brand, info in TARGETED_BRANDS.items():
        brand_clean = brand.lower().replace("-", "")
        # Exact match of brand inside foreign domain (e.g. chase-verify.com, paypal-support.com)
        if brand_clean in normalized_domain and len(clean_domain) != len(brand_clean):
            return brand
        
        # Levenshtein distance check (e.g., paypa1, chsee, wellsfargo1)
        if len(clean_domain) >= 4 and abs(len(clean_domain) - len(brand_clean)) <= 2:
            dist = levenshtein_distance(normalized_domain, brand_clean)
            if dist == 1 or (dist == 2 and len(brand_clean) >= 8):
                return brand
    return None

def extract_url_features(url: str) -> Dict[str, Any]:
    """
    Extracts 35+ high-precision lexical, structural, brand impersonation, and behavioral features.
    """
    raw_url = url.strip()
    if not re.match(r"^https?://", raw_url, re.IGNORECASE):
        parsed = urllib.parse.urlparse("http://" + raw_url)
    else:
        parsed = urllib.parse.urlparse(raw_url)

    scheme = parsed.scheme.lower()
    netloc = parsed.netloc.lower()
    path = parsed.path
    query = parsed.query

    # Accurate TLD Extraction using Public Suffix List
    extracted = tld_extractor(raw_url)
    registered_domain = extracted.registered_domain.lower() if extracted.registered_domain else netloc
    domain_body = extracted.domain.lower() if extracted.domain else ""
    subdomain = extracted.subdomain.lower() if extracted.subdomain else ""
    suffix = ("." + extracted.suffix.lower()) if extracted.suffix else ""

    # Check if host is raw IP
    has_ip = is_ip_address(netloc)
    
    # Entropy calculation
    domain_entropy = calculate_entropy(domain_body if domain_body else netloc)
    path_entropy = calculate_entropy(path)
    
    # Shortener check
    is_shortened = registered_domain in URL_SHORTENERS or netloc in URL_SHORTENERS
    
    # Suspicious / high-abuse TLD check
    has_suspicious_tld = suffix in SUSPICIOUS_TLDS or any(netloc.endswith(stld) for stld in SUSPICIOUS_TLDS)
    
    # Check Punycode / Internationalized Domain Name (IDN) homograph attack
    has_punycode = "xn--" in netloc
    
    # Check open redirect pattern in query parameters
    has_open_redirect = bool(re.search(r"(?:redirect|url|next|goto|target|dest|rdir|link)=https?%3A%2F%2F", raw_url, re.IGNORECASE) or
                            re.search(r"(?:redirect|url|next|goto|target|dest|rdir|link)=https?://", raw_url, re.IGNORECASE))
    
    # Check brand spoofing / typosquatting
    detected_spoofed_brand = None
    is_whitelisted = False

    # Check if registered domain matches trusted master list
    if registered_domain in SAFE_DOMAINS or netloc in SAFE_DOMAINS:
        is_whitelisted = True

    if not is_whitelisted:
        # Check if domain mimics a brand via keywords or typosquatting
        for brand, info in TARGETED_BRANDS.items():
            is_legit_domain = any(registered_domain == off or registered_domain.endswith("." + off) for off in info["official_domains"])
            if is_legit_domain:
                is_whitelisted = True
                break
            
            # If not legit, does the URL contain the brand keywords or typosquatting?
            if any(k in raw_url.lower() for k in info["keywords"]):
                detected_spoofed_brand = brand
                break
        
        # Check algorithmic typosquatting & homoglyph spoofing
        if not detected_spoofed_brand and not is_whitelisted and domain_body:
            typo_match = detect_typosquatting(domain_body)
            if typo_match:
                detected_spoofed_brand = typo_match

    # Character and structural metrics
    url_len = len(raw_url)
    domain_len = len(netloc)
    path_len = len(path)
    
    dots_count = raw_url.count(".")
    hyphens_count = raw_url.count("-")
    underscores_count = raw_url.count("_")
    slashes_count = raw_url.count("/")
    at_count = raw_url.count("@")
    question_count = raw_url.count("?")
    equals_count = raw_url.count("=")
    amp_count = raw_url.count("&")
    percent_count = raw_url.count("%")
    digits_count = sum(c.isdigit() for c in raw_url)
    digits_ratio = round(digits_count / max(url_len, 1), 4)
    
    # Sensitive credential harvesting keywords inside URL path or parameters
    sensitive_keywords = ["login", "verify", "secure", "account", "update", "banking", 
                          "signin", "webscr", "password", "confirm", "wallet", "support",
                          "auth", "recover", "validation", "service", "billing", "re-authenticate",
                          "identity", "unlock", "suspended", "security"]
    keyword_count = sum(1 for kw in sensitive_keywords if kw in raw_url.lower())

    # Subdomain abuse (e.g. paypal.com.attacker.com or chase.secure-verify.net)
    has_subdomain_brand_abuse = False
    if subdomain and not is_whitelisted:
        for brand in TARGETED_BRANDS:
            if brand in subdomain:
                has_subdomain_brand_abuse = True
                detected_spoofed_brand = brand
                break

    features = {
        "url_length": url_len,
        "domain_length": domain_len,
        "path_length": path_len,
        "registered_domain": registered_domain,
        "subdomain": subdomain,
        "suffix": suffix,
        "tld": suffix,
        "is_https": scheme == "https",
        "has_ip_host": has_ip,
        "is_shortened": is_shortened,
        "has_suspicious_tld": has_suspicious_tld,
        "has_punycode": has_punycode,
        "has_open_redirect": has_open_redirect,
        "has_subdomain_brand_abuse": has_subdomain_brand_abuse,
        "dots_count": dots_count,
        "hyphens_count": hyphens_count,
        "underscores_count": underscores_count,
        "slashes_count": slashes_count,
        "at_symbol_count": at_count,
        "question_count": question_count,
        "equals_count": equals_count,
        "ampersand_count": amp_count,
        "percent_count": percent_count,
        "digits_count": digits_count,
        "digits_ratio": digits_ratio,
        "domain_entropy": domain_entropy,
        "path_entropy": path_entropy,
        "sensitive_keyword_count": keyword_count,
        "spoofed_brand": detected_spoofed_brand,
        "is_whitelisted": is_whitelisted,
        "subdomain_count": max(0, len(subdomain.split(".")) if subdomain else 0),
        "has_at_symbol": at_count > 0,
        "has_double_slash_in_path": "//" in path,
        "has_hex_encoding": "%20" in raw_url or bool(re.search(r"%[0-9a-fA-F]{2}", raw_url))
    }
    
    return features
