# NoiseLessBurp v1.0.0

**Initial public release** | 2026-10-26

Developed by Harith Dilshan (h4rithd) with support from OpenAI.

## Highlights

- Importable Burp HTTP history view filter in `NoiseLessBurp.bambda`.
- Easy-to-edit boolean configuration at the top of the script.
- Burp Target scope filtering enabled by default and independently switchable.
- JS, CSS, image, font, Flash, and extensionless asset suppression.
- Handles extensionless `/js` and `/css` routes even on HTTP 304 responses.
- Preserves API responses, annotated requests, source maps, and error evidence by default.
- Optional snippet to retain Burp's converted Settings-mode filters.
- Detailed installation, configuration, edge case, and troubleshooting guidance.

## Install

Download `NoiseLessBurp.bambda` from the repository, import through **Extensions > Bambda library**, and load it under **Proxy > HTTP history > Filter > Script mode**.

## Notes

This filter changes only HTTP history visibility. It does not intercept, drop, or modify traffic.

**Release status:** These are versioned release notes committed to the repository. Publishing a GitHub Releases entry requires the GitHub Releases UI or a separate authorized API workflow.
