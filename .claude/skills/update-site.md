# Update Site Documentation

Perform a full audit of the Aigentic documentation site against the current codebase.

## Instructions

1. **Discover public API files**: Search `src/core/` for public Kotlin classes, interfaces, functions, and annotations that form the user-facing API. Focus on:
   - DSL entry points and builder functions
   - Data classes used as agent inputs/outputs
   - Annotations (e.g., `@AigenticParameter`)
   - Tool interfaces and implementations
   - Outcome types and message types

2. **Discover documentation files**: Search `site/docs/` for all documentation pages (`.md` and `.mdx` files).

3. **Compare and identify drift**: For each documentation page, check whether:
   - Function signatures match the current code
   - Type names, parameter names, and return types are accurate
   - Code examples compile against the current API
   - Descriptions accurately reflect current behavior
   - Imports in examples are correct

4. **Update documentation**: Fix any discrepancies found. When updating:
   - Use the `/tone-of-voice` skill for writing style guidelines
   - Keep code examples minimal and focused
   - Ensure imports are complete and correct

5. **Verify**: Run the site build to check for errors after making changes.
