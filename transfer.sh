# Script to ease the development of the VEX processing module

# TODO:

# evr_t


SERVER="uyuni.test"

podman --log-level=debug system connection add server ssh://root@$SERVER
podman system connection ls # Debug
export CONTAINER_CONNECTION=server # NOTE: this will only apply to the current shell instance  
echo $CONTAINER_CONNECTION # Debug

<<<<<<< HEAD
scp ../uyuni-VEX/schema/spacewalk/common/tables/suseVEXAnnotations.sql root@$SERVER:/tmp/
scp ../uyuni-VEX/schema/spacewalk/common/tables/suseVEXHash.sql root@$SERVER:/tmp/
ssh root@$SERVER "mgrctl cp /tmp/suseVEXAnnotations.sql server:/tmp/"
ssh root@$SERVER "mgrctl cp /tmp/suseVEXHash.sql server:/tmp/"

ssh root@$SERVER "rm /tmp/vex-processor/ -r"
scp -r ../uyuni-VEX/python/spacewalk/server/vex root@$SERVER:/tmp/vex-processor
ssh root@$SERVER "mgrctl cp /tmp/vex-processor server:/tmp/"

ssh root@$SERVER "mgrctl exec 'python3 -m venv /tmp/vex-processor/venv --system-site-packages'"

ssh root@$SERVER "mgrctl exec  '/tmp/vex-processor/venv/bin/pip install pytest packageurl-python psycopg2'"

# Apply migrations
scp ../uyuni-VEX/schema/spacewalk/upgrade/susemanager-schema-5.1.3-to-susemanager-schema-5.1.4/* root@$SERVER:/tmp/migrations
=======
# scp ../uyuni-VEX/schema/spacewalk/common/tables/suseVEXAnnotations.sql root@$SERVER:/tmp/
# scp ../uyuni-VEX/schema/spacewalk/common/tables/suseVEXHash.sql root@$SERVER:/tmp/
# ssh root@$SERVER "mgrctl cp /tmp/suseVEXAnnotations.sql server:/tmp/"
# ssh root@$SERVER "mgrctl cp /tmp/suseVEXHash.sql server:/tmp/"

# ssh root@$SERVER "rm /tmp/vex-processor/ -r"
# scp -r ../uyuni-VEX/python/spacewalk/server/vex root@$SERVER:/tmp/vex-processor
# ssh root@$SERVER "mgrctl cp /tmp/vex-processor server:/tmp/"

# ssh root@$SERVER "mgrctl exec 'python3 -m venv /tmp/vex-processor/venv --system-site-packages'"

# ssh root@$SERVER "mgrctl exec  '/tmp/vex-processor/venv/bin/pip install pytest packageurl-python psycopg2'"

# Apply migrations
scp ../uyuni-VEX/schema/spacewalk/upgrade/susemanager-schema-5.1.4-to-susemanager-schema-5.1.5/* root@$SERVER:/tmp/migrations
>>>>>>> 060ad82a2fc (Scanner via api works)

#ssh root@uyuni.test "sudo psql -U spacewalk -d susemanager -f suseVEXAnnotations.sql"
#ssh root@uyuni.test "sudo psql -U spacewalk -d susemanager -f suseVEXHash.sql"

#psql -U spacewalk -d susemanager -c "SELECT * FROM rhncve;"
#psql -U spacewalk -d susemanager -c "SELECT * FROM suseovalplatform;"
#psql -U spacewalk -d susemanager -c "SELECT * FROM suseovalvulnerablepackage;"