#!/usr/bin/env bash

# strap: get the publishing stuff out of the way of the git repo and go
BOOTSTRAP_TO=/tmp/TaskSharkProduction

die() {
  printf "\e[1;31m%s\e[0m\n" "$1"
  exit 2
}

if [ -e "$BOOTSTRAP_TO" ]; then
  rm -r "$BOOTSTRAP_TO" || die "failed to clear $BOOTSTRAP_TO"
fi

mkdir -p /tmp/TaskSharkProduction

THIS=$(realpath "$(dirname "$0")")
cp "$THIS"/* "$BOOTSTRAP_TO/" || die "failed to copy to $BOOTSTRAP_TO"

PROJ=$(realpath .)
if ! [ -e "$PROJ/gradlew.bat" ]; then
  PROJ=$(realpath "$PROJ/..")
  if ! [ -e "$PROJ/gradlew.bat" ]; then
    die "can't find project files?"
  fi
fi

export PROJECT_PATH="$PROJ"
echo "project files: $PROJ"

pushd "$BOOTSTRAP_TO" || die "can't switch into $BOOTSTRAP_TO"

./build_all.sh

popd || die "can't switch out of $BOOTSTRAP_TO"

rm -r "$BOOTSTRAP_TO"
