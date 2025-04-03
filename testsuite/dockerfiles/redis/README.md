# Redis mirror

This Docker container includes a **Redis** instance, which we will use to query information from the test **code coverage** map. This data is preloaded during the Docker **build** process by connecting to the Redis instance hosted on AWS.

This approach allows us to avoid handling Redis **secrets** in the PR workflows, especially when running them with `workflow_run` in a **forked repository** using the fork's GitHub runner.

### Build

```
docker build --build-arg AWS_REDIS_USER="your-user" \
--build-arg AWS_REDIS_PASS="your-password" \
--build-arg AWS_REDIS_HOST="your-aws-redis-host" \
--build-arg AWS_REDIS_PORT="6379" \
-t redis:latest .
```
