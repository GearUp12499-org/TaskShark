#!/usr/bin/env bash

# Inputs
GIT_REF=$1
VERSION=$2
IS_PRIMARY=$3

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

die() {
  echo "$r$bold${red}Fatal error$r${red}: $1$r"
  exit 3
}
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

DOC_ARCHIVE_PATH="build/docVersions"
DOC_ACTIVE_ARCHIVE_PATH="build/docVersionsActive"
DOC_PATH="build/docs"

# Actually build the thing

echo "$r$bold$green  Building $r$green $VERSION (from $GIT_REF)$r"

if [ "$VERSION" = "_snapshot" ]; then
  echo "$r$yellow    This is a snapshot version.$r"
fi
if [ "$IS_PRIMARY" = "yes" ]; then
  echo "$r$yellow    This is the primary version (finalized documentation)$r"
fi

pushd "$PROJECT_PATH" || die "Failed to change to project directory"

mkdir -p "$DOC_ARCHIVE_PATH" || die "Failed to create docs target path"
mkdir -p "$DOC_PATH" || die "Failed to create docs target path"

git switch --detach "$GIT_REF"
echo "$r$green    Building$bold maven artifacts$r"

if [ "$VERSION" = "_snapshot" ]; then
  ./gradlew publishAllPublicationsToBuildLocalRepository
else
  ./gradlew "-Pversion=$VERSION" publishAllPublicationsToBuildLocalRepository
fi

echo "$r$green    Building$bold documentation$r"

VER_OUT_PATH="$DOC_ARCHIVE_PATH/$VERSION"
mkdir -p "$VER_OUT_PATH"

if [ "$IS_PRIMARY" = "yes" ]; then
  ln -s "$(realpath $DOC_ARCHIVE_PATH)" "$DOC_ACTIVE_ARCHIVE_PATH"
  mkdir -p "$DOC_PATH"
else
  rm "$DOC_ACTIVE_ARCHIVE_PATH"
fi

if [ "$VERSION" = "_snapshot" ]; then
  ./gradlew :dok:dokkaGenerate
else
  ./gradlew "-Pversion=$VERSION" :dok:dokkaGenerate
fi

cp -r "dok/build/dokka/html/"* "$VER_OUT_PATH/"

if [ "$IS_PRIMARY" = "yes" ]; then
  cp -r "dok/build/dokka/html/"* "$DOC_PATH/"
fi

popd || die "Failed to leave project directory"

echo "$r$bold$green  Completed$r$green $VERSION (from $GIT_REF)$r"