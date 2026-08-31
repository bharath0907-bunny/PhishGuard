package com.phishguard.mobile.analyzer

import java.util.regex.Pattern

object MessageParser {
    private val URL_PATTERN = Pattern.compile(
        "(?:(?:https?://)|(?:www\\.))[\\w/\\-?=%.]+\\.[\\w/\\-&?=%.]+|" +
        "\\b[a-zA-Z0-9.\\-]+\\.(?:com|net|org|xyz|top|tk|ml|ga|cf|gq|info|co|online|site|app|cc|link|live|buzz|icu|rest|sbs|cfd)(?:/[a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]*)?",
        Pattern.CASE_INSENSITIVE
    )

    fun extractUrls(text: String): List<String> {
        val urls = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(text)
        while (matcher.find()) {
            var u = matcher.group()
            if (!u.startsWith("http://") && !u.startsWith("https://")) {
                u = "http://$u"
            }
            urls.add(u)
        }
        return urls
    }
}
