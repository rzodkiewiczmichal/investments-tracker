#!/usr/bin/env bash
# Prints the next Flyway migration version number.
dir="$(git rev-parse --show-toplevel)/src/main/resources/db/migration"
last=$(ls "$dir" | sed -nE 's/^V([0-9]+)__.*/\1/p' | sort -n | tail -1)
echo $((last + 1))
