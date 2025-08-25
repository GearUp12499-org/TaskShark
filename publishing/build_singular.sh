#!/usr/bin/env bash

# Inputs
GIT_REF=$1
VERSION=$2

# Expect exported: PROJECT_PATH

# Make it look cool idk
style() {
  printf "\e[%sm" "$1"
}

r=$(style "0")
bold=$(style "1")
green=$(style "32")
yellow=$(style "33")
red=$(style "31")

# Check that everything is ok

if [ -z "$PROJECT_PATH" ] || ! [ -d "$PROJECT_PATH" ]; then
  echo "$r$bold${red}Environment error"
  echo "$r${red}  Set (export) ${yellow}PROJECT_PATH${red} to the project directory.$r"
  exit 2
fi

if [ -z "$GIT_REF" ] || [ -z "$VERSION" ]; then
  echo "$r$bold${red}Missing arguments"
  echo "$r${red}Usage: build_singular.sh <git_ref> <version>"
  echo "$yellow  use '_snapshot' as version for snapshot builds$r"
  exit 1
fi

# Actually build the thing

echo "$r$bold$green  Building $r$green $VERSION (from $GIT_REF)$r"

if [ "$VERSION" = "_snapshot" ]; then
  echo "$r$yellow    This is a snapshot version.$r"
fi

die() {
  echo "$r$bold${red}Fatal error$r${red}: $1$r"
  exit 3
}

pushd "$PROJECT_PATH" || die "Failed to change to project directory"

git switch --detach "$GIT_REF"

if [ "$VERSION" = "_snapshot" ]; then
  ./gradlew publishAllPublicationsToBuildLocalRepository
else
  ./gradlew "-Pversion=$VERSION" publishAllPublicationsToBuildLocalRepository
fi

popd || die "Failed to leave project directory"

echo "$r$bold$green  Completed$r$green $VERSION (from $GIT_REF)$r"