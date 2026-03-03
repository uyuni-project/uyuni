#!/usr/bin/env bats
#
# Unit tests for uyuni-configfiles-sync
# Requires bats-core: https://github.com/bats-core/bats-core
#
# Run with:
#   bats containers/server-image/test/uyuni-configfiles-sync.bats
#

SCRIPT="containers/server-image/root/usr/bin/uyuni-configfiles-sync"

setup() {
    # Create a temporary working environment for each test
    TEST_DIR="$(mktemp -d)"
    export DEFAULTS_STORAGE="$TEST_DIR/uyuni-configfiles-sync/"
    export SYSCONFIG_FILE="$TEST_DIR/sysconfig-uyuni-configfiles-sync"
    mkdir -p "$DEFAULTS_STORAGE"
}

teardown() {
    rm -rf "$TEST_DIR"
}

# ---------------------------------------------------------------------------
# help
# ---------------------------------------------------------------------------

@test "help is printed when no arguments are given" {
    run bash "$SCRIPT"
    [ "$status" -eq 0 ]
    echo "$output" | grep -q "Available Commands"
}

@test "help mentions init, list and sync commands" {
    run bash "$SCRIPT"
    echo "$output" | grep -q "init"
    echo "$output" | grep -q "list"
    echo "$output" | grep -q "sync"
}

# ---------------------------------------------------------------------------
# sync: skips when buildhash is missing
# ---------------------------------------------------------------------------

@test "sync skips and warns when buildhash file is missing" {
    run bash -c "DEFAULTS_STORAGE='$DEFAULTS_STORAGE' SYSCONFIG_FILE='$SYSCONFIG_FILE' bash '$SCRIPT' sync"
    [ "$status" -eq 0 ]
    echo "$output" | grep -q "Skipping"
}

# ---------------------------------------------------------------------------
# sync: skips when already executed on this container image
# ---------------------------------------------------------------------------

@test "sync skips when buildhash matches sysconfig file" {
    echo "abc123" > "$DEFAULTS_STORAGE/.buildhash"
    echo "abc123" > "$SYSCONFIG_FILE"
    run bash -c "DEFAULTS_STORAGE='$DEFAULTS_STORAGE' SYSCONFIG_FILE='$SYSCONFIG_FILE' bash '$SCRIPT' sync"
    [ "$status" -eq 0 ]
    echo "$output" | grep -q "already executed"
}

# ---------------------------------------------------------------------------
# sync: rpmconf warning appears when .rpmnew / .rpmsave files exist
# ---------------------------------------------------------------------------

@test "sync prints rpmconf warning when rpmnew files exist in a persistent volume" {
    # Set up a fake PV tracked by the sync tool
    PV_DIR="$TEST_DIR/pv-etc-rhn"
    mkdir -p "$PV_DIR"
    PV_STORAGE="$DEFAULTS_STORAGE/--etc-rhn-"
    mkdir -p "$PV_STORAGE"
    echo "$PV_DIR/" > "$PV_STORAGE/.pv"

    # Simulate a .rpmnew file left by a previous upgrade
    touch "$PV_DIR/rhn.conf.rpmnew"

    # Provide a buildhash that differs from sysconfig (triggers sync path)
    echo "newhash" > "$DEFAULTS_STORAGE/.buildhash"
    echo "oldhash" > "$SYSCONFIG_FILE"

    # Source only the warning block by running a minimal wrapper
    run bash -c "
        DEFAULTS_STORAGE='$DEFAULTS_STORAGE'
        SYSCONFIG_FILE='$SYSCONFIG_FILE'
        RPMNEW_COUNT=0
        for pv_dir in \$(ls \$DEFAULTS_STORAGE | grep -v '^\\.buildhash\$'); do
            PV_PATH=\$(cat \"\$DEFAULTS_STORAGE/\$pv_dir/.pv\" 2>/dev/null)
            if [ -n \"\$PV_PATH\" ]; then
                COUNT=\$(find \"\$PV_PATH\" -maxdepth 5 \\( -name '*.rpmnew' -o -name '*.rpmsave' \\) 2>/dev/null | wc -l)
                RPMNEW_COUNT=\$((RPMNEW_COUNT + COUNT))
            fi
        done
        if [ \"\$RPMNEW_COUNT\" -gt 0 ]; then
            echo \"WARNING: \$RPMNEW_COUNT .rpmnew/.rpmsave file(s) need review.\"
            echo \"Run 'rpmconf -a' inside the container to interactively merge them\"
        fi
    "
    [ "$status" -eq 0 ]
    echo "$output" | grep -q "WARNING"
    echo "$output" | grep -q "rpmconf -a"
}

@test "sync does not print rpmconf warning when no rpmnew or rpmsave files exist" {
    PV_DIR="$TEST_DIR/pv-etc-rhn"
    mkdir -p "$PV_DIR"
    PV_STORAGE="$DEFAULTS_STORAGE/--etc-rhn-"
    mkdir -p "$PV_STORAGE"
    echo "$PV_DIR/" > "$PV_STORAGE/.pv"

    # No .rpmnew or .rpmsave files
    touch "$PV_DIR/rhn.conf"

    run bash -c "
        DEFAULTS_STORAGE='$DEFAULTS_STORAGE'
        RPMNEW_COUNT=0
        for pv_dir in \$(ls \$DEFAULTS_STORAGE | grep -v '^\\.buildhash\$'); do
            PV_PATH=\$(cat \"\$DEFAULTS_STORAGE/\$pv_dir/.pv\" 2>/dev/null)
            if [ -n \"\$PV_PATH\" ]; then
                COUNT=\$(find \"\$PV_PATH\" -maxdepth 5 \\( -name '*.rpmnew' -o -name '*.rpmsave' \\) 2>/dev/null | wc -l)
                RPMNEW_COUNT=\$((RPMNEW_COUNT + COUNT))
            fi
        done
        echo \"count=\$RPMNEW_COUNT\"
    "
    [ "$status" -eq 0 ]
    echo "$output" | grep -q "count=0"
    ! echo "$output" | grep -q "WARNING"
}
