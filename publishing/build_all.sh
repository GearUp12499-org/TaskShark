#!/usr/bin/env bash

VERSION_TAGS=$(git -C "$PROJECT_PATH" tag | sed -nE '/^v/M{s/v//;p}')

for i in $VERSION_TAGS; do
  ./build_singular.sh "v$i" "$i"
done

# Add other snapshot versions here. Make sure 'dev' is last so the docs work.
# shellcheck disable=SC2043
for i in 0.2 dev; do
  ./build_singular.sh "$i" "_snapshot"
done

# Return to some sense of normalcy
git -C "$PROJECT_PATH" switch main
