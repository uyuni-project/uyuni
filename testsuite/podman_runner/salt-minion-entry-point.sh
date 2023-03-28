server=$1
activation_key=$2
echo "master: $1" >> /etc/venv-salt-minion/minion
echo "grains:" >> /etc/venv-salt-minion/minion
echo " susemanager:" >> /etc/venv-salt-minion/minion
echo "   activation_key: '$2'" >> /etc/venv-salt-minion/minion
echo >> /etc/venv-salt-minion/minion
echo $RANDOM > /etc/machine-id
/usr/bin/venv-salt-minion

