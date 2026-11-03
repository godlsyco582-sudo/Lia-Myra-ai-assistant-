package com.Lia.assistant.forge

object ForgeHtml {

    fun stripFences(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```html", ignoreCase = true)) {
            clean = clean.substringAfter("```html").trimStart()
        } else if (clean.startsWith("```")) {
            clean = clean.substringAfter("```").trimStart()
        }
        if (clean.endsWith("```")) {
            clean = clean.substringBeforeLast("```").trimEnd()
        }
        return clean
    }

    fun looksLikeHtml(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("<!doctype html") ||
                lower.contains("<html") ||
                (lower.contains("<head") && lower.contains("<body")) ||
                (lower.contains("<div") && lower.contains("</div>"))
    }

    fun repairTruncated(html: String): String {
        val stripped = stripFences(html)
        if (stripped.contains("</html>", ignoreCase = true)) {
            return stripped
        }
        if (stripped.length < 14000) {
            return stripped
        }

        val sb = StringBuilder(stripped)
        val lower = stripped.lowercase()

        val openScript = lower.lastIndexOf("<script")
        val closeScript = lower.lastIndexOf("</script>")
        if (openScript > closeScript) {
            sb.append("\n</script>")
        }

        val openStyle = lower.lastIndexOf("<style")
        val closeStyle = lower.lastIndexOf("</style>")
        if (openStyle > closeStyle) {
            sb.append("\n</style>")
        }

        if (!lower.contains("</body>")) {
            sb.append("\n</body>")
        }
        if (!lower.contains("</html>")) {
            sb.append("\n</html>")
        }
        return sb.toString()
    }

    fun withSafetyNet(html: String): String {
        val safetyScript = """
<script>
(function() {
  window.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
      var elements = document.querySelectorAll('*');
      for (var i = 0; i < elements.length; i++) {
        var el = elements[i];
        var style = window.getComputedStyle(el);
        if (style.opacity === '0' && style.position !== 'fixed' && style.pointerEvents !== 'none' && !el.hasAttribute('aria-hidden') && el.tagName !== 'CANVAS') {
          el.style.opacity = '1';
          el.style.transform = 'none';
        }
      }
    }, 1400);
  });
})();
</script>
""".trimIndent()

        return if (html.contains("</body>", ignoreCase = true)) {
            html.replace("</body>", "$safetyScript\n</body>")
        } else {
            html + safetyScript
        }
    }
}
