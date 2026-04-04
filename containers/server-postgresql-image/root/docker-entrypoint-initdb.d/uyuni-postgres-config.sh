#!/bin/bash
# Adjust postgresql.conf for Uyuni
# This is run automatically at the first start of the container
# or it can be run manually later.

POSTGRESQL=/var/lib/pgsql/data/postgresql.conf
HBA_FILE=/var/lib/pgsql/data/pg_hba.conf
SSL_CERT=/etc/pki/tls/certs/spacewalk.crt
SSL_KEY=/etc/pki/tls/private/pg-spacewalk.key

postgres_reconfig() {
    echo "Setting $1 = $2"
    if test $(sed -n "/^$1[[:space:]]*=/p" $POSTGRESQL | wc -l) -ne 0; then
        sed -i "s|^$1[[:space:]]*=.*|$1 = $2|" $POSTGRESQL
    else
        echo "$1 = $2" >> $POSTGRESQL
    fi
}

# Get total memory in KB
TOTAL_MEM_KB=$(sed -n -e '/MemTotal:/{s|MemTotal:[[:space:]]*\([0-9]*\).*|\1| p}' /proc/meminfo)

# Check minimum memory requirement (255MB)
if [ "$TOTAL_MEM_KB" -lt $((0xff * 1024)) ]; then
    echo "WARNING: low memory: $TOTAL_MEM_KB"
    TOTAL_MEM_KB=$((0xff * 1024))
fi

# Binary rounding function
bin_rnd() {
    local value=$1
    local mbt=1
    while [ $value -gt 16 ]; do
        value=$((value / 2))
        mbt=$((mbt * 2))
    done
    echo $((mbt * value))
}

# Convert to MB
to_mb() {
    echo "$(($1 / 1024))MB"
}

# Get max_connections from current postgresql.conf
MAX_CONNECTIONS=$(sed -n -e '/^max_connections[[:space:]]*=/{s/.*=[[:space:]]*\([0-9]*\).*/\1/ p}' $POSTGRESQL)
[ -z "$MAX_CONNECTIONS" -o "$MAX_CONNECTIONS" -lt 400 ] && MAX_CONNECTIONS=500

EFFECTIVE_IO_CONCURRENCY=$(sed -n -e '/^effective_io_concurrency[[:space:]]*=/{s/.*=[[:space:]]*\([0-9]*\).*/\1/ p}' $POSTGRESQL)

if [ "$1" == "--hdd" ] ; then
    echo "Configuring for rotational HDD"
    IS_SSD=0
elif [ "$1" == "--ssd" ] ; then
    echo "Configuring for SSD"
    IS_SSD=1
elif [ "$EFFECTIVE_IO_CONCURRENCY" == 2 ] ; then
    echo "Rotational HDD is already configured"
    IS_SSD=0
else
    echo "Configuring for SSD"
    IS_SSD=1
fi

# Calculate values
SHARED_BUFFERS=$(bin_rnd $((TOTAL_MEM_KB / 4)))
EFFECTIVE_CACHE_SIZE=$(bin_rnd $((TOTAL_MEM_KB * 3 / 4)))
WORK_MEM=$(bin_rnd $(((TOTAL_MEM_KB - SHARED_BUFFERS) / (3 * MAX_CONNECTIONS))))
MAINTENANCE_WORK_MEM=$(bin_rnd $(( TOTAL_MEM_KB / 16 < 1048576 ? TOTAL_MEM_KB / 16 : 1048576 ))) # 1GB

# Apply configurations
postgres_reconfig "listen_addresses" "'*'"
postgres_reconfig "shared_buffers" "$(to_mb $SHARED_BUFFERS)"
postgres_reconfig "effective_cache_size" "$(to_mb $EFFECTIVE_CACHE_SIZE)"
postgres_reconfig "work_mem" "$(to_mb $WORK_MEM)"
postgres_reconfig "maintenance_work_mem" "$(to_mb $MAINTENANCE_WORK_MEM)"
postgres_reconfig "max_wal_size" "4096MB"
postgres_reconfig "min_wal_size" "1024MB"
postgres_reconfig "checkpoint_completion_target" "0.9"
postgres_reconfig "wal_buffers" "16MB"
postgres_reconfig "constraint_exclusion" "off"
postgres_reconfig "max_connections" "$MAX_CONNECTIONS"

# log to the stderr instead of the log file
postgres_reconfig "logging_collector" "off"
postgres_reconfig "log_destination" "stderr"

if [ "$IS_SSD" -eq 1 ]; then
    postgres_reconfig "random_page_cost" "1.1"
    postgres_reconfig "effective_io_concurrency" "200"
else
    postgres_reconfig "random_page_cost" "4"
    postgres_reconfig "effective_io_concurrency" "2"
fi

postgres_reconfig jit off

if [ -f $SSL_KEY ] ; then
    postgres_reconfig "ssl" "on"
    postgres_reconfig "ssl_cert_file" "'$SSL_CERT'"
    postgres_reconfig "ssl_key_file" "'$SSL_KEY'"
fi

mkdir -p /var/lib/pgsql/data/postgresql.conf.d
postgres_reconfig "include_dir" "'postgresql.conf.d'"

echo "postgresql.conf updated"

cat "$HBA_FILE" <<EOT
local replication,postgres all trust
host all all all scram-sha-256
EOT

echo "pg_hba.conf updated"
