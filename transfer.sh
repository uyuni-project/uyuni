# Script to ease the development of the VEX processing module

podman --log-level=debug system connection add server ssh://root@uyuni.test
podman system connection ls # Debug
export CONTAINER_CONNECTION=server # NOTE: this will only apply to the current shell instance  
echo $CONTAINER_CONNECTION # Debug

ssh root@uyuni.test "sudo psql -U spacewalk -d susemanager -f suseVEXAnnotations.sql"

ssh root@uyuni.test "rm /tmp/vex-processor/ -r"

scp /home/naibu3/Documentos/UCO/TFG/uyuni-VEX/schema/spacewalk/common/tables/suseVEXAnnotations.sql root@uyuni.test:/tmp/
scp -r /home/naibu3/Documentos/UCO/TFG/uyuni-VEX/python/spacewalk/server/vex root@uyuni.test:/tmp/vex-processor

ssh root@uyuni.test "mgrctl cp /tmp/suseVEXAnnotations.sql server:/tmp/"
ssh root@uyuni.test "mgrctl cp /tmp/vex-processor server:/tmp/"

ssh root@uyuni.test "mgrctl exec 'python3 -m venv /tmp/vex-processor/venv --system-site-packages'"
ssh root@uyuni.test "mgrctl exec  '. /tmp/vex-processor/venv/bin/activate'"

ssh root@uyuni.test "mgrctl exec  './tmp/vex-processor/venv/bin/pip install pytest'"
ssh root@uyuni.test "mgrctl exec  './tmp/vex-processor/venv/bin/pip install packageurl-python'"
ssh root@uyuni.test "mgrctl exec  './tmp/vex-processor/venv/bin/pip install psycopg2'"

#psql -U spacewalk -d susemanager -c "SELECT * FROM rhncve;"
#psql -U spacewalk -d susemanager -c "SELECT * FROM suseovalplatform;"
#psql -U spacewalk -d susemanager -c "SELECT * FROM suseovalvulnerablepackage;"