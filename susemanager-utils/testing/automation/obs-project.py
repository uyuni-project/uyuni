#!/usr/bin/python3
import argparse
import os
import sys
import urllib.request
import xml.etree.ElementTree as ET
import datetime
import configparser
import re

def add(args):
    api = args.api
    project = args.project
    pr_project = args.prproject
    pull_number = args.pullnumber
    maintainer=args.setmaintainer
    disable_publish = args.disablepublish
    pr_project = pr_project + ":" + pull_number
    target_repo = args.repo
    config_file = args.configfile

    if (not os.path.exists(config_file)):
        print("ERROR: config file {} not found".format(config_file))
        sys.exit(-1)

    config = configparser.ConfigParser()
    try:
        config.read(config_file)
    except IOError as e:
        print("ERROR: Can't read config file ".format(e))
        sys.exit(-1)

    auth_user = config[api]["user"]
    auth_passwd = config[api]["pass"]

    if (auth_user == "" or auth_passwd == ""):
        print("ERROR: could not find user or password in config file")
        sys.exit(-1)

    print("DEBUG: getting api version for debugging purposes")
    req = urllib.request.Request("{}/about".format(api))
    with urllib.request.urlopen(req) as response:
        data = response.read()
    root = ET.fromstring(data)
    revision = root.find("revision").text
    print("DEBUG: API version: {}".format(revision))

    print("DEBUG: getting meta data from {}".format(project))
    passman = urllib.request.HTTPPasswordMgrWithDefaultRealm()
    url = api + "/source/" + project + "/_meta"
    passman.add_password(None, url, auth_user, auth_passwd)
    authhandler = urllib.request.HTTPBasicAuthHandler(passman)
    opener = urllib.request.build_opener(authhandler)
    urllib.request.install_opener(opener)
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req) as response:
        data = response.read()
    root = ET.fromstring(data)
    result = root.find("title").text
    print("DEBUG: found metadata for project with title {}".format(result))

    print("DEBUG: adapting project meta for new project {}".format(pr_project))
    root.set("name", pr_project)
    new_title = "Build for Pull Request #" + pull_number
    print("DEBUG: setting title to {}".format(new_title))
    root.find("title").text = new_title 

    if (maintainer!=""):
        print("DEBUG: Adding user {} as the only maintainer".format(auth_user))
        for user in root.findall("person"):
            root.remove(user)
        for group in root.findall("group"):
            root.remove(group)
        new_person = ET.fromstring("<person userid=\"{}\" role=\"maintainer\"/>".format(auth_user))
        root.append(new_person)

    if (disable_publish):
        print("DEBUG: disabling publishing")
        root.remove(root.find("publish"))
        node = ET.fromstring("<publish><disable/></publish>")
        root.append(node)

    root.find("description").text=str(datetime.datetime.now())

    print("DEBUG: adapting list of repositories")
    for repo in root.findall("repository"):
        if not re.match(target_repo, repo.get("name")):
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
        print("DEBUG: Adding setting repository {} to use path {}".format(repo.get("name"), project))
        new_path = ET.fromstring("<path project=\"{}\" repository=\"{}\" />".format(project, repo.get("name")))
        repo.append(new_path)

    print("DEBUG: creating new project: {}".format(pr_project))
    passman = urllib.request.HTTPPasswordMgrWithDefaultRealm()
    url = api + "/source/" + pr_project + "/_meta"
    passman.add_password(None, url, auth_user, auth_passwd)
    authhandler = urllib.request.HTTPBasicAuthHandler(passman)
    opener = urllib.request.build_opener(authhandler)
    urllib.request.install_opener(opener)
    data = ET.tostring(root)
    req = urllib.request.Request(url, data = data, method="PUT")
    with urllib.request.urlopen(req) as response:
        data = response.read()
    root = ET.fromstring(data)
    print("DEBUG: result: {}".format(root.get("code")))

def print_usage(args):
    print("Use -h for help")

def remove(args):
    api = args.api
    pr_project = args.prproject
    pull_number = args.pullnumber
    pr_project = pr_project + ":" + pull_number
    config_file = args.configfile
    interactive = not args.noninteractive

    if (not os.path.exists(config_file)):
        print("ERROR: config file {} not found".format(config_file))
        sys.exit(-1)

    config = configparser.ConfigParser()
    try:
        config.read(config_file)
    except IOError as e:
        print("ERROR: Can't read config file ".format(e))
        sys.exit(-1)

    auth_user = config[api]["user"]
    auth_passwd = config[api]["pass"]

    if (auth_user == "" or auth_passwd == ""):
        print("ERROR: could not find user or password in config file")
        sys.exit(-1)

    print("DEBUG: getting api version for debugging purposes")
    req = urllib.request.Request("{}/about".format(api))
    with urllib.request.urlopen(req) as response:
        data = response.read()
    root = ET.fromstring(data)
    revision = root.find("revision").text
    print("DEBUG: API version: {}".format(revision))

    print("DEBUG: removing project {}".format(pr_project))
    answer = ""
    if (interactive):
        while (answer!="y" and answer!="n"):
            answer = input("Are you sure you want to remove it?(y/n)")
        if (answer == "n"):
            print("OK. Maybe another day. Bye!")
            sys.exit(-1)
    passman = urllib.request.HTTPPasswordMgrWithDefaultRealm()
    url = api + "/source/" + pr_project 
    passman.add_password(None, url, auth_user, auth_passwd)
    authhandler = urllib.request.HTTPBasicAuthHandler(passman)
    opener = urllib.request.build_opener(authhandler)
    urllib.request.install_opener(opener)
    data = ET.tostring(root)
    req = urllib.request.Request(url, data = data, method="DELETE")
    with urllib.request.urlopen(req) as response:
        data = response.read()
    root = ET.fromstring(data)
    print("DEBUG: result: {}".format(root.get("code")))


parser = argparse.ArgumentParser(description="This utility helps you manage an obs project for a Pull Request")
parser.add_argument('--api', help="Build Service API, defaults to https://api.opensuse.org", default="https://api.opensuse.org")
parser.add_argument('--configfile', help="Config file where username and password are store, by default $HOME/.oscrc", default=os.environ["HOME"] + "/.oscrc")
parser.add_argument('--prproject', help="Parent project for Pull Requests, defaults to systemsmanagement:Uyuni:Master:PR", default="systemsmanagement:Uyuni:Master:PR")
parser.set_defaults(func=print_usage)

subparser = parser.add_subparsers()
parser_add = subparser.add_parser("add", help="add project")
parser_add.add_argument('--project', help="Project from which to \"branch\" from, defaults to systemsmanagement:Uyuni:Master", default="systemsmanagement:Uyuni:Master")
parser_add.add_argument('--repo', help="Repo to build for, defaults to openSUSE.*|SLE.*", default="openSUSE.*|SLE.*")
parser_add.add_argument('pullnumber', help="Pull Request number, for example 1")
parser_add.add_argument('--setmaintainer', help="Set this user as the only maintainer", default="")
parser_add.add_argument('--disablepublish', help="Disable the publish", action="store_true", default=False)
parser_add.set_defaults(func=add)

parser_remove = subparser.add_parser("remove", help="remove project")
parser_remove.add_argument('pullnumber', help="Pull Request number, for example 1")
parser_remove.add_argument('--noninteractive', help="Non interactive. This is yes by default!", action="store_true", default=False)
parser_remove.set_defaults(func=remove)


args = parser.parse_args()
args.func(args)
