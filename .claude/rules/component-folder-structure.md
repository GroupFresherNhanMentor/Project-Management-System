---
name: component-folder-structure
description: Child-only components go inside a components/ folder within the parent's folder
metadata:
  type: feedback
---

When a component has child components that are only used inside it, place those children in a `components/` subfolder within the parent's folder. Each child gets its own named subfolder.

**Structure:**
```
parent-feature/
  components/
    child-one/
      child-one.ts
      child-one.html
    child-two/
      child-two.ts
      child-two.html
  parent-feature.ts
  parent-feature.html
```

**Why:** Keeps child-only components clearly scoped to their parent feature and avoids polluting shared folders.

**How to apply:** Whenever creating components that are consumed by exactly one parent component, use this structure instead of a flat `tabs/`, `partials/`, or similar folder.
