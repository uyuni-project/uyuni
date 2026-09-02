ARG BASE_IMAGE
FROM ${BASE_IMAGE}

# Clear directories to enforce deletion semantics while preserving log4j2.xml
RUN if [ -f /usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/classes/log4j2.xml ]; then \
        mv /usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/classes/log4j2.xml /tmp/log4j2.xml.bak; \
    fi && \
    rm -rf /usr/share/susemanager/www/tomcat/webapps/rhn/* \
           /usr/share/susemanager/www/htdocs/* \
           /usr/share/susemanager/db/schema-upgrade/* \
           /usr/share/susemanager/db/reportdb-schema-upgrade/*

# Copy the payload built outside
COPY .build/apr-changes-payload/ /

# Restore preserved log4j2.xml
RUN if [ -f /tmp/log4j2.xml.bak ]; then \
        mkdir -p /usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/classes && \
        mv /tmp/log4j2.xml.bak /usr/share/susemanager/www/tomcat/webapps/rhn/WEB-INF/classes/log4j2.xml; \
    fi

# Move Python and Setup files to their dynamic destinations
RUN set -ex; \
    PYTHON_SITEPACKAGES=$(python3 -c "import os, spacewalk; print(os.path.dirname(os.path.dirname(spacewalk.__file__)))") && \
    cp -r /payload-python/* "$PYTHON_SITEPACKAGES/" && \
    rm -rf /payload-python && \
    SETUP_PM_PATH=$(rpm -ql spacewalk-setup | grep Setup.pm | tr -d '\r') && \
    cp /payload-setup/Setup.pm "$SETUP_PM_PATH" && \
    rm -rf /payload-setup && \
    CERTS_PY_PATH=$(rpm -ql python3-spacewalk-certs-tools | grep mgr_ssl_cert_setup.py | tr -d '\r') && \
    cp /payload-certs/mgr_ssl_cert_setup.py "$CERTS_PY_PATH" && \
    rm -rf /payload-certs

# Prepare mirror symlink and cleanup mgrctl
RUN rm -f /usr/bin/mgrctl && \
    mkdir -p /srv/www/htdocs/pub && \
    ln -sf /mirror /srv/www/htdocs/pub/mirror && \
    chown root:root /mirror /srv/www/htdocs/pub/mirror

CMD ["/docker-entrypoint-init"]
