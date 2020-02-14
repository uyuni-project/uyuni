#!/bin/bash

# on build host:
# ant -f manager-build.xml refresh-branding-jar deploy restart-tomcat restart-taskomatic -Ddeploy.host=server.tf.local
# ant -f manager-build.xml refresh-branding-jar deploy -Ddeploy.host=worker-1.tf.local
# scp -r akka_worker/* worker-1.tf.local://root
#
# on worker:
# spacewalk-service stop
# rcpostgresql stop
# ssh -L 9080:localhost:9080 -L 5432:localhost:5432 -L 2829:localhost:2829 uyunis.tf.local
# sh ./start-worker.sh

java -classpath /root/classes:/usr/share/rhn/lib/spacewalk-asm.jar:/usr/share/rhn/lib/rhn.jar:/usr/share/rhn/lib/java-branding.jar:/usr/share/spacewalk/taskomatic/* -Dfile.encoding=UTF-8 -Xms256m -Xmx4096m --add-modules java.annotation,com.sun.xml.bind --add-exports java.annotation/javax.annotation.security=ALL-UNNAMED --add-opens java.annotation/javax.annotation.security=ALL-UNNAMED com.suse.manager.tasks.RemoteActorManager
