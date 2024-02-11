#!/usr/bin/python3
# pylint: disable=missing-module-docstring,invalid-name
import argparse
import os
import sys
import xml.etree.ElementTree as ET
import datetime
import re
import subprocess


def run_osc_api(api_call, api, config_file, data="", method="GET"):
    """
    Run osc api calls.
    Parameters
    ----------
    api_call: The api call, for example "/about" or "/source/project"
    api: The build service api, for example "https://api.opensuse.org"
    config_file: Usually ~/.oscrc or ~/.config/osc/oscrc
    """
    params = [
        "osc",
        # pylint: disable-next=consider-using-f-string
        "--config={}".format(config_file),
        "-A",
        api,
        "api",
        api_call,
        "-X",
        method,
    ]
    if data != "":
        params.append("-d")
        params.append(data)
    # pylint: disable-next=subprocess-run-check
    sp_result = subprocess.run(params, stdout=subprocess.PIPE)
    data = sp_result.stdout
    return ET.fromstring(data)


# pylint: disable-next=redefined-outer-name
def add(args):
    api = args.api
    project = args.project
    pr_project = args.prproject
    pull_number = args.pullnumber
    maintainer = args.setmaintainer
    disable_publish = args.disablepublish
    pr_project = pr_project + ":" + pull_number
    target_repo = args.repo
    config_file = args.configfile

    if not os.path.exists(config_file):
        # pylint: disable-next=consider-using-f-string
        print("ERROR: config file {} not found".format(config_file))
        sys.exit(-1)

    print("DEBUG: getting api version for debugging purposes")
    root = run_osc_api("/about", api, config_file)
    revision = root.find("revision").text
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: API version: {}".format(revision))

    # pylint: disable-next=consider-using-f-string
    print("DEBUG: getting meta data from {}".format(project))
    # pylint: disable-next=consider-using-f-string
    root = run_osc_api("/source/{}/_meta".format(project), api, config_file)
    result = root.find("title").text
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: found metadata for project with title {}".format(result))

    # pylint: disable-next=consider-using-f-string
    print("DEBUG: adapting project meta for new project {}".format(pr_project))
    root.set("name", pr_project)
    new_title = "Build for Pull Request #" + pull_number
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: setting title to {}".format(new_title))
    root.find("title").text = new_title

    if maintainer != "":
        # pylint: disable-next=consider-using-f-string
        print("DEBUG: Adding user {} as the only maintainer".format(maintainer))
        for user in root.findall("person"):
            root.remove(user)
        for group in root.findall("group"):
            root.remove(group)
        new_person = ET.fromstring(
            # pylint: disable-next=consider-using-f-string
            '<person userid="{}" role="maintainer"/>'.format(maintainer)
        )
        root.append(new_person)

    if disable_publish:
        print("DEBUG: disabling publishing")
        publish_node = root.find("publish")
        # pylint: disable-next=singleton-comparison
        if publish_node != None:
            root.remove(publish_node)
        node = ET.fromstring("<publish><disable/></publish>")
        root.append(node)

    root.find("description").text = str(datetime.datetime.now())

    print("DEBUG: adapting list of repositories")
    for repo in root.findall("repository"):
        if not re.match(target_repo, repo.get("name")):
            # pylint: disable-next=consider-using-f-string
            print("DEBUG: skipping {} repo".format(repo.get("name")))
            root.remove(repo)
            continue
        for child in repo.findall("path"):
            repo.remove(child)
        for child in repo.findall("arch"):
            if child.text != "x86_64":
                print("DEBUG skipping arch " + child.text)
                repo.remove(child)
        for child in repo.findall("releasetarget"):
            repo.remove(child)
        print(
            # pylint: disable-next=consider-using-f-string
            "DEBUG: Adding setting repository {} to use path {}".format(
                repo.get("name"), project
            )
        )
        new_path = ET.fromstring(
            # pylint: disable-next=consider-using-f-string
            '<path project="{}" repository="{}" />'.format(project, repo.get("name"))
        )
        repo.append(new_path)

    # pylint: disable-next=consider-using-f-string
    print("DEBUG: creating new project: {}".format(pr_project))
    data = ET.tostring(root)
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: data: {}".format(data))
    root = run_osc_api(
        # pylint: disable-next=consider-using-f-string
        "/source/{}/_meta".format(pr_project),
        api,
        config_file,
        data=data,
        method="PUT",
    )
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: result: {}".format(root.get("code")))


# pylint: disable-next=redefined-outer-name,unused-argument
def print_usage(args):
    print("Use -h for help")


# pylint: disable-next=redefined-outer-name
def remove(args):
    api = args.api
    pr_project = args.prproject
    pull_number = args.pullnumber
    pr_project = pr_project + ":" + pull_number
    config_file = args.configfile
    interactive = not args.noninteractive

    if not os.path.exists(config_file):
        # pylint: disable-next=consider-using-f-string
        print("ERROR: config file {} not found".format(config_file))
        sys.exit(-1)

    print("DEBUG: getting api version for debugging purposes")
    root = run_osc_api("/about", api, config_file)
    revision = root.find("revision").text
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: API version: {}".format(revision))

    # pylint: disable-next=consider-using-f-string
    print("DEBUG: removing project {}".format(pr_project))
    answer = ""
    if interactive:
        while answer != "y" and answer != "n":
            answer = input("Are you sure you want to remove it?(y/n)")
        if answer == "n":
            print("OK. Maybe another day. Bye!")
            sys.exit(-1)
    root = run_osc_api(
        # pylint: disable-next=consider-using-f-string
        "/source/{}".format(pr_project),
        api,
        config_file,
        method="DELETE",
    )
    # pylint: disable-next=consider-using-f-string
    print("DEBUG: result: {}".format(root.get("code")))


parser = argparse.ArgumentParser(
    description="This utility helps you manage an obs project for a Pull Request"
)
parser.add_argument(
    "--api",
    help="Build Service API, defaults to https://api.opensuse.org",
    default="https://api.opensuse.org",
)
parser.add_argument(
    "--configfile",
    help="Config file where username and password are store, by default $HOME/.oscrc",
    default=os.environ["HOME"] + "/.oscrc",
)
parser.add_argument(
    "--prproject",
    help="Parent project for Pull Requests, defaults to systemsmanagement:Uyuni:Master:PR",
    default="systemsmanagement:Uyuni:Master:PR",
)
parser.set_defaults(func=print_usage)

subparser = parser.add_subparsers()
parser_add = subparser.add_parser("add", help="add project")
parser_add.add_argument(
    "--project",
    help='Project from which to "branch" from, defaults to systemsmanagement:Uyuni:Master',
    default="systemsmanagement:Uyuni:Master",
)
parser_add.add_argument(
    "--repo",
    help="Repo to build for, defaults to openSUSE.*|SLE.*",
    default="openSUSE.*|SLE.*",
)
parser_add.add_argument("pullnumber", help="Pull Request number, for example 1")
parser_add.add_argument(
    "--setmaintainer", help="Set this user as the only maintainer", default=""
)
parser_add.add_argument(
    "--disablepublish", help="Disable the publish", action="store_true", default=False
)
parser_add.set_defaults(func=add)

parser_remove = subparser.add_parser("remove", help="remove project")
parser_remove.add_argument("pullnumber", help="Pull Request number, for example 1")
parser_remove.add_argument(
    "--noninteractive",
    help="Non interactive. This is yes by default!",
    action="store_true",
    default=False,
)
parser_remove.set_defaults(func=remove)


args = parser.parse_args()
args.func(args)
