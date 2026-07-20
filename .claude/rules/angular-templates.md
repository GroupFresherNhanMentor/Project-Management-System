---
name: angular-templates
description: Always use separate .html template files for Angular components
metadata:
  type: feedback
---

Always use separate `.html` template files for Angular components (`templateUrl`). Do not use inline `template: \`...\`` strings.

**Why:** User preference — keeps components clean and maintainable.

**How to apply:** Whenever creating a new Angular component, create a paired `.html` file and reference it with `templateUrl: './component-name.html'` instead of writing the template inline.
