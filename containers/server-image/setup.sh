#!/bin/bash
if [[ "$MIGRATION" == 1 ]]; then
  #TODO if we want a migration image, we can put this statement in its setup.sh
  if [[ ! -f /root/.MIGRATION_SETUP_COMPLETE ]]; then
    mkdir -p /root/.ssh;
    ssh-keyscan -t rsa $HOST_FQDN >>~/.ssh/known_hosts;
    echo "$ID_RSA" > /root/.ssh/id_rsa;
    chmod 600 /root/.ssh/id_rsa;
    echo "$ID_RSA_PUB" > /root/.ssh/id_rsa.pub;

    for folder in /var/lib/pgsql \
                  /var/cache \
                  /var/spacewalk \
                  /var/log \
                  /srv/salt \
                  /srv/www/htdocs/pub \
                  /srv/www/cobbler \
                  /srv/www/os-images \
                  /srv/tftpboot \
                  /srv/formula_metadata \
                  /srv/pillar \
                  /srv/susemanager \
                  /srv/spacewalk \
                  /root \
                  /etc/apache2 \
                  /etc/rhn \
                  /etc/systemd/system/multi-user.target.wants \
                  /etc/salt \
                  /etc/tomcat \
                  /etc/cobbler \
                  /etc/sysconfig;
    do
      rsync -avz $HOST_FQDN:$folder/ $folder;
    done;
    rm -f /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT;
    ln -s /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT;
    echo 'server.no_ssl = 1' >> /etc/rhn/rhn.conf;
    #TODO Address is wrong, there's DNS issue. Comment out for now and remove the wrong file
    #sed "s/address=[^:]*:/address='$UYUNI_FQDN':/" -i /etc/rhn/taskomatic.conf;
    #sed "s/address=[^:]*:/address='$UYUNI_FQDN':/" -i /etc/tomcat/conf.d/remote_debug.conf;
    > /etc/rhn/taskomatic.conf;
    > /usr/lib/systemd/system/taskomatic.service.d/jmx.conf
    > /usr/lib/systemd/system/taskomatic.service.d/override.conf
    > /etc/tomcat/conf.d/remote_debug.conf;


    #SETUP POSTGRES
    rhn-ssl-tool --gen-ca --no-rpm --set-common-name=$UYUNI_FQDN --set-country=$CERT_COUNTRY --set-state=$CERT_STATE --set-city=$CERT_CITY --set-org=$CERT_O --set-org-unit=$CERT_OU --set-email=$MANAGER_ADMIN_EMAIL --password=$CERT_PASS --force
    HOST_SHORTFORM=${HOST_FQDN%%.*}
    cp /root/ssl-build/$HOST_SHORTFORM/server.crt /etc/pki/tls/certs/spacewalk.crt
    cp /root/ssl-build/$HOST_SHORTFORM/server.key /etc/pki/tls/private/pg-spacewalk.key
    chown postgres /etc/pki/tls/private/pg-spacewalk.key
    systemctl start postgresql

    #SETUP APACHE
    cp /root/ssl-build/$HOST_SHORTFORM/server.key /etc/pki/tls/private/spacewalk.key

    touch /root/.MANAGER_SETUP_COMPLETE
    touch /root/.MIGRATION_SETUP_COMPLETE
    sleep 5
    spacewalk-service start
  fi
else
        #TODO if we want an image just for new installation (no migration), we can put this statement in its setup.sh
  /usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s -n
fi

