#!/bin/bash

# Stop hook: detect source file changes and prompt documentation review
# Reads JSON from stdin to check for stop_hook_active flag

INPUT=$(cat)

# Infinite loop guard: if we're already in a stop hook session, exit silently
STOP_HOOK_ACTIVE=$(echo "$INPUT" | jq -r '.stop_hook_active // false')
if [ "$STOP_HOOK_ACTIVE" = "true" ]; then
  exit 0
fi

# Get all changed files (staged + unstaged)
CHANGED_FILES=$(cd "$CLAUDE_PROJECT_DIR" && {
  git diff --name-only 2>/dev/null
  git diff --cached --name-only 2>/dev/null
} | grep '\.kt$' | grep '^src/' | sort -u)

# No source files changed — nothing to do
if [ -z "$CHANGED_FILES" ]; then
  exit 0
fi

FILE_LIST=$(echo "$CHANGED_FILES" | tr '\n' ', ' | sed 's/,$//')

cat <<EOF
{"decision": "block", "reason": "Source files were modified: ${FILE_LIST}. Before ending this session, check if any of these changes affect the public API. Search the documentation in /site/docs/ for references to the changed APIs and update any documentation that no longer matches the code. Use the /tone-of-voice skill for writing style guidelines."}
EOF
