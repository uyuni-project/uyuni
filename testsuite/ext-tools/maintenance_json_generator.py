import argparse
import json
import subprocess
import sys

import requests

# Default dictionary for client tools, essentially anything that is common between SUMA versions
defaultdict = {
    "sle12sp4_client": "/SUSE_Updates_SLE-Manager-Tools_12_x86_64/",
    "sle12sp4_minion": "/SUSE_Updates_SLE-Manager-Tools_12_x86_64/",
    "sle12sp5_client": "/SUSE_Updates_SLE-Manager-Tools_12_x86_64/",
    "sle12sp5_minion": "/SUSE_Updates_SLE-Manager-Tools_12_x86_64/",
    "sle15_client": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                     "/SUSE_Updates_SLE-Product-SLES_15-LTSS_x86_64/"],
    "sle15_minion": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                     "/SUSE_Updates_SLE-Product-SLES_15-LTSS_x86_64/"],
    "sle15sp1_client": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Product-SLES_15-SP1-LTSS_x86_64/"],
    "sle15sp1_minion": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Product-SLES_15-SP1-LTSS_x86_64/"],
    "sle15sp2_client": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Product-SLES_15-SP2-LTSS_x86_64/"],
    "sle15sp2_minion": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Product-SLES_15-SP2-LTSS_x86_64/"],
    "sle15sp3_client": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Module-Basesystem_15-SP3_x86_64/",
                        "/SUSE_Updates_SLE-Module-Server-Applications_15-SP3_x86_64/",
                        "/SUSE_Updates_SLE-Product-SLES_15-SP3-LTSS_x86_64/"],
    "sle15sp3_minion": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Module-Basesystem_15-SP3_x86_64/",
                        "/SUSE_Updates_SLE-Module-Server-Applications_15-SP3_x86_64/",
                        "/SUSE_Updates_SLE-Product-SLES_15-SP3-LTSS_x86_64/"],
    "sle15sp4_client": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Module-Basesystem_15-SP4_x86_64/",
                        "/SUSE_Updates_SLE-Module-Server-Applications_15-SP4_x86_64/"],
    "sle15sp4_minion": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Module-Basesystem_15-SP4_x86_64/",
                        "/SUSE_Updates_SLE-Module-Server-Applications_15-SP4_x86_64/"],
    "sle15sp5_client": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Module-Basesystem_15-SP5_x86_64/",
                        "/SUSE_Updates_SLE-Module-Server-Applications_15-SP5_x86_64/"],
    "sle15sp5_minion": ["/SUSE_Updates_SLE-Manager-Tools_15_x86_64/",
                        "/SUSE_Updates_SLE-Module-Basesystem_15-SP5_x86_64/",
                        "/SUSE_Updates_SLE-Module-Server-Applications_15-SP5_x86_64/"],
    "centos7_client": "/SUSE_Updates_RES_7-CLIENT-TOOLS_x86_64/",
    "centos7_minion": "/SUSE_Updates_RES_7-CLIENT-TOOLS_x86_64",
    "rocky8_minion": "/SUSE_Updates_RES_8-CLIENT-TOOLS_x86_64/",
    "ubuntu1804_minion": "/SUSE_Updates_Ubuntu_18.04-CLIENT-TOOLS_x86_64/",
    "ubuntu2004_minion": "/SUSE_Updates_Ubuntu_20.04-CLIENT-TOOLS_x86_64/",
    "ubuntu2204_minion": "/SUSE_Updates_Ubuntu_22.04-CLIENT-TOOLS_x86_64/",
    "debian10_minion": "/SUSE_Updates_Debian_10-CLIENT-TOOLS_x86_64/",
    "debian11_minion": "/SUSE_Updates_Debian_11-CLIENT-TOOLS_x86_64/",
    "opensuse153arm_minion": ["/SUSE_Updates_openSUSE-SLE_15.3/",
                              "/SUSE_Updates_SLE-Manager-Tools_15_aarch64/"],
    "opensuse154arm_minion": ["/SUSE_Updates_openSUSE-SLE_15.4/",
                              "/SUSE_Updates_SLE-Manager-Tools_15_aarch64/"],
    "opensuse155arm_minion": ["/SUSE_Updates_openSUSE-SLE_15.5/",
                              "/SUSE_Updates_SLE-Manager-Tools_15_aarch64/"],
    "rhel9_minion": "/SUSE_Updates_EL_9-CLIENT-TOOLS_x86_64/",
    "rocky9_minion": "/SUSE_Updates_EL_9-CLIENT-TOOLS_x86_64/",
    "alma9_minion": "/SUSE_Updates_EL_9-CLIENT-TOOLS_x86_64/",
    "oracle9_minion": "/SUSE_Updates_EL_9-CLIENT-TOOLS_x86_64/",
    "slemicro51_minion": ["/SUSE_Updates_SLE-Manager-Tools-For-Micro_5_x86_64/",
                          "/SUSE_Updates_SUSE-MicroOS_5.1_x86_64/"],
    "slemicro52_minion": ["/SUSE_Updates_SLE-Manager-Tools-For-Micro_5_x86_64/",
                          "/SUSE_Updates_SUSE-MicroOS_5.2_x86_64/"],
    "slemicro53_minion": ["/SUSE_Updates_SLE-Manager-Tools-For-Micro_5_x86_64/",
                          "/SUSE_Updates_SUSE-MicroOS_5.3_x86_64/",
                          "/SUSE_Updates_SLE-Micro_5.3_x86_64/"],
    "slemicro54_minion": ["/SUSE_Updates_SLE-Manager-Tools-For-Micro_5_x86_64/",
                          "/SUSE_Updates_SUSE-MicroOS_5.4_x86_64/",
                          "/SUSE_Updates_SLE-Micro_5.4_x86_64/"],
}

# Dictionary for SUMA 4.2 Server and Proxy, which is then added together with the common dictionary for client tools
nodesdict42 = {
    "server": ["/SUSE_Updates_SLE-Module-SUSE-Manager-Server_4.2_x86_64/",
               "/SUSE_Updates_SLE-Product-SUSE-Manager-Server_4.2_x86_64/",
               "/SUSE_Updates_SLE-Module-Basesystem_15-SP3_x86_64/",
               "/SUSE_Updates_SLE-Module-Server-Applications_15-SP3_x86_64/"],
    "proxy": ["/SUSE_Updates_SLE-Module-SUSE-Manager-Proxy_4.2_x86_64/",
              "/SUSE_Updates_SLE-Product-SUSE-Manager-Proxy_4.2_x86_64",
              "/SUSE_Updates_SLE-Module-Basesystem_15-SP3_x86_64/",
              "/SUSE_Updates_SLE-Module-Server-Applications_15-SP3_x86_64/"]
}
nodesdict42.update(defaultdict)

# Dictionary for SUMA 4.3 Server and Proxy, which is then added together with the common dictionary for client tools
nodesdict43 = {
    "server": ["/SUSE_Updates_SLE-Module-SUSE-Manager-Server_4.3_x86_64/",
               "/SUSE_Updates_SLE-Product-SUSE-Manager-Server_4.3_x86_64/",
               "/SUSE_Updates_SLE-Module-Basesystem_15-SP4_x86_64/",
               "/SUSE_Updates_SLE-Module-Server-Applications_15-SP4_x86_64/"],
    "proxy": ["/SUSE_Updates_SLE-Module-SUSE-Manager-Proxy_4.3_x86_64/",
              "/SUSE_Updates_SLE-Product-SUSE-Manager-Proxy_4.3_x86_64",
              "/SUSE_Updates_SLE-Module-Basesystem_15-SP4_x86_64/",
              "/SUSE_Updates_SLE-Module-Server-Applications_15-SP4_x86_64/"]
}
nodesdict43.update(defaultdict)


def parse_args():
    parser = argparse.ArgumentParser(
        description="This script reads the open qam-manager requests and creates a json file that can be fed in the "
                    "BV testsuite pipeline")
    parser.add_argument("-v", "--version", dest="version",
                        help="Version of SUMA you want to run this script for, options are 42 for 4.2 or 43 for 4.3",
                        default="43", action='store')
    parser.add_argument("-i", "--mi_ids", dest="mi_ids", help="MI IDs", default=None, action='store')

    args = parser.parse_args()
    return args


def read_requests():
    # Find open requests
    result = object
    try:
        # TODO Find a better way to query the open requests, this is fragile because it depends on external utils
        #  being there.
        result = subprocess.run(["osc --apiurl https://api.suse.de qam open -G qam-manager"], shell=True,
                                stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except subprocess.CalledProcessError:
        print("The ibs command failed for some reason")
    output = result.stdout.decode('utf-8')
    lines = output.splitlines()
    # Create empty list to add the maintenance incidents from the output
    mi_ids = []
    for line in lines:
        if "ReviewRequest" in line:
            line1 = line.rstrip()
            line2 = line1.split(sep=":")
            mi_id = line2[3]
            mi_ids.append(mi_id)
    return mi_ids


def find_valid_repos(mi_ids, version):
    if version == '42':
        dict_version = nodesdict42
    elif version == '43':
        dict_version = nodesdict43
    else:
        print("You have not given one of the correct options, run the script with -h to see the correct ones")
        sys.exit(1)

    finaldict = {}
    for node, suffixraw in dict_version.items():
        for mi_id in mi_ids:
            if isinstance(suffixraw, str):
                suffix = suffixraw
                repo = create_url(mi_id, suffix)
                if repo is not None:
                    if node in finaldict:
                        # This is needed for mi_ids that have multiple repos for each node, e.g. basesystem and server
                        # apps for server
                        if mi_id in finaldict[node]:
                            for i in range(1, 100):
                                if str(mi_id) + '-' + str(i) not in finaldict[node]:
                                    finaldict[node][str(mi_id) + '-' + str(i)] = repo
                                    break
                        else:
                            finaldict[node][mi_id] = repo
                    else:
                        # for each mi_id we have multiple repos sometimes for each node
                        finaldict[node] = {mi_id: repo}
            elif isinstance(suffixraw, list):
                for suffix in suffixraw:
                    repo = create_url(mi_id, suffix)
                    if repo is not None:
                        if node in finaldict:
                            # This is needed for mi_ids that have multiple repos for each node, e.g. basesystem and
                            # server apps for server
                            if mi_id in finaldict[node]:
                                for i in range(1, 100):
                                    if str(mi_id) + '-' + str(i) not in finaldict[node]:
                                        finaldict[node][str(mi_id) + '-' + str(i)] = repo
                                        break
                            else:
                                # for each mi_id we have multiple repos sometimes for each node
                                finaldict[node][mi_id] = repo
                        else:
                            # for each mi_id we have multiple repos sometimes for each node
                            finaldict[node] = {mi_id: repo}

    # Format into json and print
    # Check that it's not empty and save to file
    if finaldict:
        with open('custom_repositories.json', 'w', encoding='utf-8') as f:
            json.dump(finaldict, f, indent=2)
    else:
        print("Dictionary is empty, something went wrong")
        sys.exit(1)


def create_url(mi_id, suffix):
    link = ["http://download.suse.de/ibs/SUSE:/Maintenance:/", str(mi_id)]

    if link[:-1] == str(mi_id):
        link.append(suffix)
        url = ''.join(link)
    else:
        link = ''.join(link)
        url = str(link) + suffix
    re = requests.get(url)
    if re.ok:
        return url


def main():
    args = parse_args()
    if args.mi_ids is not None:
        mi_ids = args.mi_ids.split(",")
    else:
        mi_ids = read_requests()
    find_valid_repos(mi_ids, args.version)


if __name__ == '__main__':
    main()
