FROM opensuse/leap:15.4
#FIXME: we should not use master repo but stable, but there is no python3.fbtftp
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/Other/openSUSE_Leap_15.4/ uyuni-other && \
    zypper ref -f && \
    zypper -n install python3-PyYAML \
    python3-fbtftp \
    python3-requests && \
    zypper clean -a
COPY tftp_wrapper.py /usr/bin/
RUN chmod +x /usr/bin/tftp_wrapper.py
CMD /usr/bin/tftp_wrapper.py
