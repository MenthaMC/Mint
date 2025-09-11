#!/usr/bin/env bash

# requires curl & jq
# Credit: https://github.com/PurpurMC/Purpur
#
# Usage:
# upstreamCommit --folia HASH [--folia-branch BRANCH]
#
# flags:
#   --folia HASH           the commit hash to use for comparing commits between PaperMC/Folia (compare HASH...TARGET)
#   --folia-branch BRANCH  (Optional) target branch or ref for Folia when new foliaRef not present. Default: main

function getCommits() {
    curl -s -H "Accept: application/vnd.github.v3+json" "https://api.github.com/repos/$1/compare/$2...$3" \
        | jq -r '.commits[] | "'"$1"'@\(.sha[:8]) \(.commit.message | split("\r\n")[0] | split("\n")[0])" | sub("\\[ci( |-)skip]"; "[ci/skip]")'
}

(
set -e
PS1="$"

# default for target branch/ref
foliaBranch="ver/1.21.8"

# try to read the new foliaRef from git diff (if present)
newFoliaRef=$(git diff gradle.properties | awk '/^\+[[:space:]]*foliaRef[[:space:]]*=/ {print $NF}')

# arg-hash variable (set by --folia)
foliaHashArg=""

# parse args
while true; do
    case "$1" in
        --folia)
            foliaHashArg="$2"
            shift 2
            ;;
        --folia-branch)
            foliaBranch="$2"
            shift 2
            ;;
        "" )
            break
            ;;
        *)
            # unknown flag -> stop parsing
            break
            ;;
    esac
done

if [ -z "$foliaHashArg" ]; then
    echo "No folia hash provided. Usage: upstreamCommit --folia HASH [--folia-branch BRANCH]"
    exit 0
fi

# decide target ref: prefer the new foliaRef in gradle.properties if present, otherwise use foliaBranch
if [ -n "$newFoliaRef" ]; then
    targetRef="$newFoliaRef"
else
    targetRef="$foliaBranch"
fi

# get commits
foliaCommits=$(getCommits "PaperMC/Folia" "$foliaHashArg" "$targetRef")

if [ -z "$foliaCommits" ]; then
    echo "No Folia updates detected between $foliaHashArg and $targetRef."
    exit 0
fi

logsuffix="\n\nFolia Changes (PaperMC/Folia $foliaHashArg...$targetRef):\n$foliaCommits"
disclaimer="Upstream has released updates that appear to apply and compile correctly"
log="Updated Upstream (Folia)\n\n${disclaimer}${logsuffix}"

git add gradle.properties
echo -e "$log" | git commit -F -

) || exit 1
