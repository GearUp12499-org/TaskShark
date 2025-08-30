#!/usr/bin/env bash

VERSION_TAGS=$(git -C "$PROJECT_PATH" tag | sed -nE '/^v/M{s/v//;p}')

MAIN_BRANCH="dev"

for i in $VERSION_TAGS; do
  ./build_singular.sh "v$i" "$i"
done

# Add other snapshot versions here. Make sure 'dev' is last so the docs work.
# shellcheck disable=SC2043
for i in 0.2 dev; do
  # pull down branch
  git -C "$PROJECT_PATH" checkout --track "origin/$i"
  if [ "$i" = "$MAIN_BRANCH" ]; then
    IS_PRIMARY="yes"
  else
    IS_PRIMARY="no"
  fi

  ./build_singular.sh "$i" "_snapshot" "$IS_PRIMARY"
done

# Return to some sense of normalcy
git -C "$PROJECT_PATH" switch main
