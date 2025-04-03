FROM redis:latest

RUN apt-get update && apt-get install -y redis-tools

# Define ARG values for build-time configuration
ARG AWS_REDIS_USER
ARG AWS_REDIS_PASS
ARG AWS_REDIS_HOST
ARG AWS_REDIS_PORT

# Set the working directory for the backup
WORKDIR /data

# Define the backup file location
ENV BACKUP_FILE /data/backup.rdb

# Dump the remote Redis database from AWS into an RDB file using the correct redis-cli command
RUN redis-cli -u "redis://${AWS_REDIS_USER}:${AWS_REDIS_PASS}@${AWS_REDIS_HOST}:${AWS_REDIS_PORT}" --rdb $BACKUP_FILE

# Move the backup to the local Redis data directory (it will be mounted to Redis container by default)
RUN mv $BACKUP_FILE /data/dump.rdb

# Expose Redis default port
EXPOSE 6379

# Start Redis and load the backup
CMD redis-server --daemonize yes && echo "Backup completed and Redis started with the backup."
