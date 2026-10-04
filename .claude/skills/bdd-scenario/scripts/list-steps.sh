#!/usr/bin/env bash
# Lists all Cucumber step patterns (class: annotation pattern). Optional arg: grep filter.
root="$(git rev-parse --show-toplevel)/src/test/java"
grep -rHoE --include=*.java '@(Given|When|Then|And|But)\("([^"\\]|\\.)*"\)' "$root" 2>/dev/null \
  | sed -E 's#.*/([A-Za-z]+)\.java:#\1: #' | sort | { if [ -n "${1:-}" ]; then grep -i -- "$1"; else cat; fi; }
