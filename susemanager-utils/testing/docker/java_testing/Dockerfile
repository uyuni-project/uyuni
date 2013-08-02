# Container used to test python code of SUSE Manager
#
# VERSION               0.1

FROM sles11_sp2_base
# make sure the package repository is up to date
RUN zypper --non-interactive --gpg-auto-import-keys ref

RUN zypper in -y ant ant-junit java-1_6_0-ibm-devel sudo
