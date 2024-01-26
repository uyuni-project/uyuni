#  pylint: disable=missing-module-docstring,invalid-name
#! /usr/bin/python

from locust import HttpLocust, TaskSet, task
import yaml

# pylint: disable-next=unspecified-encoding
with open("locust_config.yml", "r") as stream:
    try:
        LocustConf = yaml.load(stream)
    except yaml.YAMLError as exc:
        print(exc)

server = LocustConf["server"]
username = LocustConf["username"]
password = LocustConf["password"]


# pylint: disable-next=missing-class-docstring
class UserBehavior(TaskSet):
    def on_start(self):
        """on_start is called when a Locust start before any task is scheduled"""
        # don't verify ssl certs
        self.login()
        self.client.verify = False

    def login(self):
        self.client.post("/", {"username": username, "password": password})

    @task(1)
    def index(self):
        self.client.get("rhn/YourRhn.do")

    @task(2)
    def overview(self):
        self.client.get("rhn/manager/systems/list/all")


class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    host = server
    # These are the minimum and maximum time respectively, in milliseconds, that a simulated user will wait between executing each task.
    min_wait = 5000
    max_wait = 9000
