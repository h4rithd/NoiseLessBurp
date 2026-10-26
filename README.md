# NoiseLessBurp

**A configurable HTTP history noise filter for Burp Suite Professional.** Focus on meaningful web and API traffic while hiding repetitive JavaScript, CSS, images, fonts, and extensionless static bundles.

**Developed by Harith Dilshan (h4rithd) with support from OpenAI.**

[![Version](https://img.shields.io/badge/version-v1.0.0-blue)](RELEASE_NOTES.md) [![License: MIT](https://img.shields.io/badge/license-MIT-green)](LICENSE) [![Burp Suite](https://img.shields.io/badge/Burp%20Suite-Bambda-orange)](https://portswigger.net/burp/documentation)

> **Important:** NoiseLessBurp is a **view filter**, not a proxy interception rule or HTTP deletion tool. Requests and responses remain in Burp history. Filtering does not drop traffic, alter requests, or modify your scope settings.

## Why NoiseLessBurp?

Burp's standard HTTP history extension and MIME filters do not always recognize bundle routes that have **no filename extension**, such as:

```text
GET /static/js?v=example
GET /bundles/sitejs?v=example
GET /assets/styles/css?v=example
GET /static/css?v=example
GET /bundles/widgets/js?v=example
```

These requests can produce **304 Not Modified**, with no useful MIME content. NoiseLessBurp checks both response MIME and the URL *path without query string* to recognize the assets. Normal HTML/JSON/XML APIs and useful error responses are preserved by default.

## Files

| File | Purpose |
|---|---|
| [NoiseLessBurp.bambda](NoiseLessBurp.bambda) | Importable Bambda for Proxy > HTTP history |
| [NoiseLessBurp.java](NoiseLessBurp.java) | Plain Java **Bambda body** for copy/paste and editing |
| [preserve-existing-settings.java](preserve-existing-settings.java) | Optional guard to prepend to Burp's converted Settings-mode script |
| [RELEASE_NOTES.md](RELEASE_NOTES.md) | Release notes and change log |
| [LICENSE](LICENSE) | MIT license |

The `.java` file is **not** a standalone Java application, Maven package, or JAR. It is the body of the `boolean matches(...)` function in Burp's Bambda editor.

## Requirements

- Burp Suite edition/version with **Proxy > HTTP history > Filter > Script mode (Bambdas)**. Developed for Burp Suite Professional 2026.4.3. Earlier versions may not provide these features.
- No Python, Jython, Maven, Gradle, browser extension, proxy add-on, or third-party libraries are required.
- Define **Target > Scope** if using the default scope-only setting.

## Installation: import Bambda

1. Open Burp Suite.
2. Go to **Extensions > Bambda library** and use the import control.
3. Import [NoiseLessBurp.bambda](NoiseLessBurp.bambda).
4. Go to **Proxy > HTTP history**, open the filter configuration.
5. Select **Script mode**, then **Load** the NoiseLessBurp filter from the library.
6. Select **Apply** or **Apply & close**.
7. Confirm **Filter on** is enabled. Use **Filter off** to compare the complete history.

Depending on the Burp version, menus and labels may vary. You can alternatively open Script mode and paste the entire contents of `NoiseLessBurp.java` into the Bambda function body, then apply it.

### Configure once per assessment

At the **top** of `NoiseLessBurp.java`, change any option between `true` and `false`, apply, then optionally save your customized version to the Bambda library.

```java
boolean SHOW_ONLY_IN_SCOPE            = true;
boolean HIDE_JAVASCRIPT               = true;
boolean HIDE_CSS                      = true;
boolean HIDE_IMAGES                   = true;
boolean HIDE_FONTS                    = true;
boolean HIDE_EXTENSIONLESS_BUNDLES    = true;
boolean PRESERVE_ERROR_RESPONSES      = true;
boolean PRESERVE_ANNOTATED_ITEMS      = true;
boolean HIDE_SOURCE_MAPS              = false;
```

**Scope toggle:** `SHOW_ONLY_IN_SCOPE=true` respects Burp's existing Target scope. Set it to `false` to show out-of-scope entries **without modifying Target > Scope**. If no target is in scope, the default filter can legitimately show zero rows.

## Full configuration reference

All options are editable **boolean** variables. No regex knowledge or compilation is required.

### Scope and response controls

| Option | Default | Meaning |
|---|---|---|
| `SHOW_ONLY_IN_SCOPE` | `true` | Display requests currently in Burp Target scope only |
| `SHOW_ONLY_WITH_PARAMETERS` | `false` | Keep only requests containing Burp-recognized parameters |
| `SHOW_REQUESTS_WITHOUT_RESPONSE` | `true` | Retain outstanding/no-response requests unless they match static rules |
| `HIDE_ASSETS_WITHOUT_RESPONSE` | `true` | Apply URL-only asset rules when a response has not arrived |
| `SHOW_STATUS_1XX` | `false` | Include informational response statuses |
| `SHOW_STATUS_2XX` | `true` | Include successful responses |
| `SHOW_STATUS_3XX` | `true` | Include redirects and 304 cache responses that survive asset filtering |
| `SHOW_STATUS_4XX` | `true` | Include client errors |
| `SHOW_STATUS_5XX` | `true` | Include server errors |

### Asset suppression

| Option | Default | Meaning |
|---|---|---|
| `HIDE_JAVASCRIPT` | `true` | Hide .js/.mjs/.cjs, script MIME, and recognized JS bundles |
| `HIDE_CSS` | `true` | Hide .css, CSS MIME, and recognized stylesheet bundles |
| `HIDE_IMAGES` | `true` | Hide common image paths/MIME types |
| `HIDE_FONTS` | `true` | Hide common font paths/WOFF MIME types |
| `HIDE_FLASH` | `true` | Hide legacy SWF/Flash assets |
| `HIDE_AUDIO_VIDEO` | `false` | Optionally hide common audio/video content |
| `HIDE_SOURCE_MAPS` | `false` | Hide `.map` resources when enabled. Kept visible by default for security review |
| `HIDE_EXTENSIONLESS_BUNDLES` | `true` | Recognize extensionless asset routes, e.g. `/static/js`, `/assets/styles/css` |

### Protection and detection switches

| Option | Default | Meaning |
|---|---|---|
| `FILTER_ONLY_GET_HEAD` | `true` | Only suppress assets on GET/HEAD; keep POST/PUT/PATCH/DELETE for security review |
| `PRESERVE_ERROR_RESPONSES` | `true` | Show 4xx and 5xx static requests when their status category is enabled |
| `PRESERVE_ANNOTATED_ITEMS` | `true` | Show items with notes or a highlight color, subject to earlier filters |
| `PRESERVE_DYNAMIC_MIME` | `true` | Show HTML, JSON, XML, YAML, and SSE responses even on asset-looking URLs |

### Rule order and important edge cases

1. **Scope, parameter and status restrictions are applied first.** Later exceptions do not override them.
2. **Annotated items and error responses are preserved** when within the selected scope/status.
3. **Non-GET/HEAD methods** are retained by default.
4. **Source maps** are independently controlled; with `HIDE_SOURCE_MAPS=false`, a `.map` file remains visible.
5. **Dynamic MIME responses** are retained by default, even on misleading file paths.
6. **Path and MIME-based asset rules** hide the remaining matching assets.

The extensionless bundle detector deliberately uses simple path heuristics. Some applications expose genuine API endpoints under `/scripts/`, `/css/` or with names ending in `js` or `css`. Inspect the unfiltered view during reconnaissance, and turn off the matching switch for a target that uses such routes dynamically.

## Example results with defaults

| Method | Path | Status / type | Displayed? | Reason |
|---|---|---|---|---|
| GET | `/static/js?v=1` | 304 | No | Recognized JavaScript bundle |
| GET | `/assets/styles/css?v=2` | 304 | No | Recognized CSS bundle |
| GET | `/bundles/widgets/js?v=3` | 304 | No | Recognized JavaScript bundle |
| GET | `/assets/site.css` | 200 CSS | No | Static CSS |
| GET | `/assets/logo.svg` | 200 SVG | No | Static image |
| POST | `/api/v1/session` | 200 JSON | Yes | Non-GET API |
| GET | `/api/v1/items` | 200 JSON | Yes | API MIME preserved |
| GET | `/js/app.js.map` | 200 | Yes | Source-map visibility enabled |
| GET | `/assets/app.js` | 404 | Yes | Error response preserved |
| GET | `/assets/app.js` | 500 | Yes | Server error preserved |
| GET | `/api/v1/items` out of scope | 200 JSON | No | Target scope switch |

These examples assume the requests are in Target scope unless explicitly noted.

## Preserve your existing Burp filter settings

**The standalone Bambda replaces the GUI's Settings-mode filter logic. It does not automatically merge existing settings.**

To preserve your currently configured checkboxes, status-code filters, search terms, or listener filters:

1. Set your existing options in **Proxy > HTTP history > Filter > Settings mode**.
2. Click **Convert to script**.
3. Paste [preserve-existing-settings.java](preserve-existing-settings.java) **before** the generated code without deleting the generated section.
4. Edit the guard's top-level boolean switches if needed.
5. Apply and save the resulting combined Bambda.

The guard only adds selected asset filtering. **An existing converted scope restriction cannot be disabled by the guard's scope toggle**, because the converted rules still execute below it. Likewise, a preservation exception in the guard cannot override restrictions from the generated script.

For a fully controllable in-scope-only toggle, use `NoiseLessBurp.bambda` as your complete filter.

## Suggested usage profiles

**Everyday API pentesting:** Leave the defaults enabled. They prioritize JSON/HTML responses and preserve 4xx/5xx messages.

**Frontend/SPA reconnaissance:** Temporarily set `HIDE_JAVASCRIPT=false` and `HIDE_SOURCE_MAPS=false`. Read bundles and source maps to discover routes and client-side behavior.

**Troubleshooting missing records:** Turn `SHOW_ONLY_IN_SCOPE=false`; then temporarily turn off each asset suppression switch, or toggle Burp's main history filter off. Check whether GUI-generated settings remain active.

**Asset and cache testing:** Disable `HIDE_JAVASCRIPT`, `HIDE_CSS`, and relevant asset hiding, and keep `SHOW_STATUS_3XX=true` to inspect 304/cache behavior.

## Known limitations

- This is a display-only filter. It does not prevent traffic creation or reduce storage usage in Burp's project.
- Client-side asset classification is heuristic. A false positive can hide a relevant request from the current view.
- Some static assets may have atypical names/MIME types and remain visible.
- The status toggles apply to responses with an HTTP status; no-response behavior is controlled separately.
- Script mode uses the Bambda's logic rather than magically inheriting Settings-mode GUI choices.
- The code does not download anything or contact any external API.
- It is **not** an automatic vulnerability detector or a substitute for assessing JavaScript resources.

## Validation checklist

After import, verify that (1) a typical API JSON request remains visible, (2) a static JS/CSS URL disappears, (3) an extensionless bundle ending in `/js?v=...` disappears even if its response is 304, (4) a static asset returning 404 or 500 remains visible, (5) toggling the scope option affects only the view, and (6) disabling the overall history filter restores previously hidden entries.

**Compatibility note:** Code structure and checks were reviewed outside Burp; a successful compile/run inside the exact Burp version remains the definitive integration check. Please report any Bambda importer, editor, or Montoya API incompatibility through GitHub Issues with the Burp version and error message.

## Attribution

**Developed by Harith Dilshan (h4rithd) with support from OpenAI.**

- Author: [h4rithd](https://github.com/h4rithd)
- Repository: [h4rithd/NoiseLessBurp](https://github.com/h4rithd/NoiseLessBurp)
- License: [MIT](LICENSE)
- License and attribution apply to this project, not to Burp Suite or PortSwigger.

This is an independent community project and is not an official PortSwigger or OpenAI product.

## References

- [PortSwigger: HTTP history script filters](https://portswigger.net/burp/documentation/desktop/tools/proxy/http-history/scripts)
- [PortSwigger Bambda examples](https://github.com/PortSwigger/bambdas)
- [Burp Suite documentation](https://portswigger.net/burp/documentation)
