Each feature module must define its own exceptions in an `exception/` subfolder. Each error scenario gets its own class extending `AppException`.

**Structure:**
```
feature/
  exception/
    FeatureNotFoundException.java
    FeatureAlreadyExistsException.java
  service/
  repository/
  ...
```

**`AppException` contract:** takes `HttpStatus` + message. Never throw `AppException` directly from feature code — always use a named subclass.

**Exception class pattern:**
```java
package fpt.qn.pms.feature.exception;

import org.springframework.http.HttpStatus;
import fpt.qn.pms.common.exception.AppException;

public class FeatureNotFoundException extends AppException {
    public FeatureNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Feature not found");
    }
}
```

**HTTP status guide:**
- `NOT_FOUND` — entity does not exist (e.g., `UserNotFoundException`, `SprintNotFoundException`)
- `CONFLICT` — duplicate or state violation (e.g., `UsernameAlreadyExistsException`, `ActiveSprintAlreadyExistsException`)
- `BAD_REQUEST` — invalid input or illegal business operation (e.g., `InvalidDateRangeException`, `AssigneeNotInProjectException`)
- `UNAUTHORIZED` — authentication failure (e.g., `InvalidCredentialsException`)
- `FORBIDDEN` — access denied (e.g., `AccountLockedException`)
- `INTERNAL_SERVER_ERROR` — unexpected server-side failure (non-domain, e.g. missing mock data)

**Throw syntax:** Always use `new` keyword — both for direct throws and in `orElseThrow` lambdas. Never use method reference `ClassName::new`.

```java
// correct
throw new UserNotFoundException();
.orElseThrow(() -> new UserNotFoundException())

// wrong
.orElseThrow(UserNotFoundException::new)
```

**Why:** Named exceptions make the intent clear at the throw site, centralise the message and status in one place, and allow targeted `@ExceptionHandler` overrides if ever needed.

**How to apply:** Whenever adding a new error case in a feature service, create a new exception class in that feature's `exception/` folder instead of throwing `AppException` directly.
