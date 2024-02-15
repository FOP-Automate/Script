#!/bin/bash

# Organization name
ORG="FOP-Automate"

# Prefix to match
PREFIX="FOP-Automate/temp-repo-"

# List all repos in the organization and delete those starting with the specified prefix
gh repo list "$ORG" --limit 1000 | while read -r repo _; do
    if [[ $repo == $PREFIX* ]]; then
        echo "Deleting repository: $repo"
        gh repo delete "$repo" --yes
    fi
done
