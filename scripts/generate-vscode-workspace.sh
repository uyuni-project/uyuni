#!/usr/bin/env bash
set -e

#
# Generate a new VSCode workspace file based on the existing settings.json and launch.json template file.
# This is a simple copy-paste, it doesn't account for comments in jsonc etc, or any structural anomalies really.
#

defaultWorkspaceName="uyuni"
defaultHostName="server.tf.local"

scriptDir=$(dirname -- "$(readlink -f -- "$BASH_SOURCE")")
settings=$(<"$scriptDir/../.vscode/settings.json")
launch=$(<"$scriptDir/../.vscode/launch.json.template")

read -p "Enter new workspace name [$defaultWorkspaceName]: " workspaceName
workspaceName=${workspaceName:-"$defaultWorkspaceName"}

read -p "Enter default server hostname for launch configs [$defaultHostName]: " hostName
hostName=${hostName:-"$defaultHostName"}

# Indent every line, then remove it from the first line
settings=$(sed 's|^|  |g' <<< $settings)
settings="${settings:2}"

# Same as above
launch=$(sed 's|^|  |g' <<< $launch)
launch="${launch:2}"

# Replace hostname if applicable
launch=$(sed "s|server.tf.local|$hostName|" <<< $launch)

read -d '' workspace << EOF || true
{
  "folders": [
    {
      "path": "."
    }
  ],
  "settings": $settings,
  "launch": $launch
}
EOF

outputPath="$scriptDir/../$workspaceName.code-workspace"
echo "$workspace" > "$outputPath"
