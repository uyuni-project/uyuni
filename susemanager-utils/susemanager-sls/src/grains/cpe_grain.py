import re

def get_cpe_grain(grains):
    ret = {"cpe": ""}

    os_release = _parse_os_release("/etc/os-release")

    cpe = os_release.get('CPE_NAME')
    if cpe:
        ret["cpe"] = cpe
    else:
        derived_cpe = _derive_cpe(grains)
        if derived_cpe:
            ret["cpe"] = derived_cpe

    return ret

# Copy-pasted from https://github.com/saltstack/salt/blame/master/salt/grains/core.py
def _parse_os_release(*os_release_files):
    """
    Parse os-release and return a parameter dictionary

    This function will behave identical to
    platform.freedesktop_os_release() from Python >= 3.10, if
    called with ("/etc/os-release", "/usr/lib/os-release").

    See http://www.freedesktop.org/software/systemd/man/os-release.html
    for specification of the file format.
    """
    # These fields are mandatory fields with well-known defaults
    # in practice all Linux distributions override NAME, ID, and PRETTY_NAME.
    ret = {"NAME": "Linux", "ID": "linux", "PRETTY_NAME": "Linux"}

    errno = None
    for filename in os_release_files:
        try:
            with open(filename, 'r') as ifile:
                regex = re.compile("^([\\w]+)=(?:'|\")?(.*?)(?:'|\")?$")
                for line in ifile:
                    match = regex.match(line.strip())
                    if match:
                        # Shell special characters ("$", quotes, backslash,
                        # backtick) are escaped with backslashes
                        ret[match.group(1)] = re.sub(
                            r'\\([$"\'\\`])', r"\1", match.group(2)
                        )
            break
        except OSError as error:
            errno = error.errno
    else:
        raise OSError(
            errno, "Unable to read files {}".format(", ".join(os_release_files))
        )

    return ret


def _derive_cpe(grains):
    """
    Try to derive the CPE of the system based on the collected core grains.

    PS: This function is not guaranteed to derive the correct CPE as there could be a many 
    variances of the same OS that require different CPEs, for example, release and beta versions.

    Currently the function exclusively derives CPEs for Debian and Ubuntu. 
    These two operating systems are the primary focus, as they are intended to be supported by 
    the OVAL-based CVE auditing project, for which this modification is intended.

    TODO: reference the OVAL code
    """
    os = grains.get('os')
    os_release = grains.get('osrelease')
    if os == 'Debian':
        return "cpe:/o:debian:debian_linux:" + os_release
    elif os == "Ubuntu":
        return "cpe:/o:canonical:ubuntu_linux:" + os_release
    else:
        return None 