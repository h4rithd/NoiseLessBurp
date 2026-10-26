/*
 * NoiseLessBurp v1.0.0
 * Burp Suite Proxy > HTTP history | View filter Bambda
 *
 * Developed by Harith Dilshan (h4rithd) with support from OpenAI.
 * Project: https://github.com/h4rithd/NoiseLessBurp
 * License: MIT
 *
 * Set the boolean switches below to true or false.
 * Visibility only. Never intercepts, modifies, blocks, or deletes traffic.
 */

// ====================== USER CONFIGURATION ======================
boolean SHOW_ONLY_IN_SCOPE           = true;  // Use Burp Target > Scope
boolean SHOW_ONLY_WITH_PARAMETERS    = false; // Show only parameterized requests
boolean SHOW_REQUESTS_WITHOUT_RESPONSE = true; // Keep pending/failed requests
boolean HIDE_ASSETS_WITHOUT_RESPONSE = true;  // Hide recognizably static URLs even without a response

boolean SHOW_STATUS_1XX              = false;
boolean SHOW_STATUS_2XX              = true;
boolean SHOW_STATUS_3XX              = true;
boolean SHOW_STATUS_4XX              = true;
boolean SHOW_STATUS_5XX              = true;

boolean HIDE_JAVASCRIPT              = true;  // .js, .mjs, JS response MIME
boolean HIDE_CSS                     = true;  // .css, CSS response MIME
boolean HIDE_IMAGES                  = true;  // png, jpeg, svg, webp, etc.
boolean HIDE_FONTS                   = true;  // woff/woff2/ttf/otf/eot
boolean HIDE_FLASH                   = true;  // swf/Flash
boolean HIDE_AUDIO_VIDEO             = false; // Keep media by default
boolean HIDE_SOURCE_MAPS             = false; // Keep .map for security analysis
boolean HIDE_EXTENSIONLESS_BUNDLES   = true;  // Handle extensionless paths ending in /js or /css

boolean FILTER_ONLY_GET_HEAD         = true;  // Never classify POST/PUT/etc. as assets
boolean PRESERVE_ERROR_RESPONSES     = true;  // Show 4xx/5xx even for static files
boolean PRESERVE_ANNOTATED_ITEMS     = true;  // Show noted/highlighted items
boolean PRESERVE_DYNAMIC_MIME        = true;  // Show HTML, JSON, XML, YAML, SSE
// ==================== END CONFIGURATION ========================

var req = requestResponse.request();
if (SHOW_ONLY_IN_SCOPE && !req.isInScope()) {
    return false;
}
if (SHOW_ONLY_WITH_PARAMETERS && !req.hasParameters()) {
    return false;
}

boolean hasResponse = requestResponse.hasResponse();
if (!hasResponse && !SHOW_REQUESTS_WITHOUT_RESPONSE) {
    return false;
}

int status = hasResponse ? requestResponse.response().statusCode() : 0;
if (hasResponse) {
    if (status >= 100 && status < 200 && !SHOW_STATUS_1XX) return false;
    if (status >= 200 && status < 300 && !SHOW_STATUS_2XX) return false;
    if (status >= 300 && status < 400 && !SHOW_STATUS_3XX) return false;
    if (status >= 400 && status < 500 && !SHOW_STATUS_4XX) return false;
    if (status >= 500 && status < 600 && !SHOW_STATUS_5XX) return false;
}

// Asset-filter exceptions never override scope, parameter, or status requirements.
if (PRESERVE_ANNOTATED_ITEMS &&
        (requestResponse.annotations().hasNotes() ||
         requestResponse.annotations().hasHighlightColor())) {
    return true;
}
if (PRESERVE_ERROR_RESPONSES && hasResponse && status >= 400) {
    return true;
}
if (FILTER_ONLY_GET_HEAD && !("GET".equalsIgnoreCase(req.method()) ||
                              "HEAD".equalsIgnoreCase(req.method()))) {
    return true;
}
if (!hasResponse && !HIDE_ASSETS_WITHOUT_RESPONSE) {
    return true;
}

var mime = requestResponse.mimeType();
// Inspect the URL path only; cache-busting query values are ignored.
String path = req.pathWithoutQuery().toLowerCase(java.util.Locale.ROOT);
String leaf = path.substring(path.lastIndexOf('/') + 1);

// Explicit .map setting has priority over dynamic MIME detection.
if (path.endsWith(".map")) {
    return !HIDE_SOURCE_MAPS;
}
if (PRESERVE_DYNAMIC_MIME && hasResponse &&
        (mime == MimeType.HTML || mime == MimeType.JSON || mime == MimeType.XML ||
         mime == MimeType.YAML || mime == MimeType.SSE)) {
    return true;
}

boolean scriptFile = path.endsWith(".js") || path.endsWith(".mjs") || path.endsWith(".cjs");
boolean cssFile = path.endsWith(".css");
boolean imageFile = path.endsWith(".png") || path.endsWith(".jpg") ||
        path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".bmp") ||
        path.endsWith(".ico") || path.endsWith(".svg") || path.endsWith(".webp") ||
        path.endsWith(".avif") || path.endsWith(".tif") || path.endsWith(".tiff");
boolean fontFile = path.endsWith(".woff") || path.endsWith(".woff2") ||
        path.endsWith(".ttf") || path.endsWith(".otf") || path.endsWith(".eot");
boolean mediaFile = path.endsWith(".mp3") || path.endsWith(".mp4") ||
        path.endsWith(".wav") || path.endsWith(".webm") || path.endsWith(".ogg");

boolean jsBundle = HIDE_EXTENSIONLESS_BUNDLES &&
        (leaf.equals("js") || (leaf.length() > 2 && leaf.endsWith("js")) ||
         path.contains("/js/") || path.contains("/scripts/"));
boolean cssBundle = HIDE_EXTENSIONLESS_BUNDLES &&
        (leaf.equals("css") || (leaf.length() > 3 && leaf.endsWith("css")) ||
         path.contains("/css/") || path.contains("/styles/") ||
         path.contains("/stylesheets/"));

// Separate switches mean disabling JavaScript filtering also disables JS bundles.
if (HIDE_JAVASCRIPT && (scriptFile || jsBundle || (hasResponse && mime == MimeType.SCRIPT))) {
    return false;
}
if (HIDE_CSS && (cssFile || cssBundle || (hasResponse && mime == MimeType.CSS))) {
    return false;
}
if (HIDE_IMAGES && (imageFile || (hasResponse &&
        (mime == MimeType.IMAGE_UNKNOWN || mime == MimeType.IMAGE_JPEG ||
         mime == MimeType.IMAGE_GIF || mime == MimeType.IMAGE_PNG ||
         mime == MimeType.IMAGE_BMP || mime == MimeType.IMAGE_TIFF ||
         mime == MimeType.IMAGE_SVG_XML)))) {
    return false;
}
if (HIDE_FONTS && (fontFile || (hasResponse &&
        (mime == MimeType.FONT_WOFF || mime == MimeType.FONT_WOFF2)))) {
    return false;
}
if (HIDE_FLASH && (path.endsWith(".swf") || (hasResponse && mime == MimeType.APPLICATION_FLASH))) {
    return false;
}
if (HIDE_AUDIO_VIDEO && (mediaFile || (hasResponse &&
        (mime == MimeType.SOUND || mime == MimeType.VIDEO)))) {
    return false;
}
return true;
