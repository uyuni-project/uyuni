#!/bin/sh
set -eu

SETTINGS_FILE=/data/settings.js
FLOWS_FILE=/data/flows.json

if [ -z "${NODERED_CREDENTIAL_SECRET:-}" ]; then
    echo "ERROR: NODERED_CREDENTIAL_SECRET must be set." >&2
    exit 1
fi

# The admin API can deploy flows and a flow can run arbitrary code, so an
# unauthenticated editor is remote code execution. Refuse to start unless the
# editor is either protected by a password or switched off entirely.
# See https://nodered.org/docs/user-guide/runtime/securing-node-red
if [ -z "${NODERED_ADMIN_PASSWORD_HASH:-}" ] && [ "${NODERED_DISABLE_EDITOR:-}" != "true" ]; then
    echo "ERROR: the Node-RED editor and admin API would be unauthenticated." >&2
    echo "Set NODERED_ADMIN_PASSWORD_HASH to a bcrypt hash, or set" >&2
    echo "NODERED_DISABLE_EDITOR=true to run flows without the editor." >&2
    echo "Generate a hash with: node-red admin hash-pw" >&2
    exit 1
fi

# settings.js reads its values from the environment, so it is copied rather
# than substituted into. Interpolating secrets with sed would corrupt the file
# whenever a value contained a character that sed or JavaScript treats
# specially, and would let a crafted value inject code into the settings.
cp /opt/settings.js.template "$SETTINGS_FILE"

# Create default empty flows file if it does not exist
if [ ! -f "$FLOWS_FILE" ]; then
    echo "[]" > "$FLOWS_FILE"
fi

exec node-red --userDir /data --settings "$SETTINGS_FILE"
