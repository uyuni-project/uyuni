#! /bin/bash
python3 -m venv venv
venv/bin/pip install flask pytest pyOpenSSL
ln -s ../spacewalk .
ln -s ../uyuni .
ln -s ../rhn .
echo "venv/bin/python3 -m pytest -s tests/"
