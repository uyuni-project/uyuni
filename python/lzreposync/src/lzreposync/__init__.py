#  pylint: disable=missing-module-docstring

import argparse
import logging
from time import sleep

from lzreposync import db_utils, updates_util
from lzreposync.db_utils import (
    get_compatible_arches,
    get_channel_info_by_label,
    get_all_arches,
    create_test_channel,
    ChannelAlreadyExistsException,
    NoSourceFoundForChannel,
    get_all_channels,
    create_content_source,
    update_channel_status,
)
from lzreposync.import_utils import (
    import_package_batch,
    batched,
    import_repository_packages_in_batch,
)
from lzreposync.rpm_repo import RPMRepo, SignatureVerificationException
from spacewalk.common.repo import GeneralRepoException
from spacewalk.satellite_tools.repo_plugins.deb_src import DebRepo

SLEEP_TIME = 2  # Num of seconds between one sync and another


def _create_channel(channel_label, channel_arch):
    print(
        f"Creating a new channel with label: {channel_label}, and arch: {channel_arch}"
    )
    try:
        channel = create_test_channel(
            channel_label=channel_label, channel_arch=channel_arch
        )
        print(
            f"Info: successfully created channel: {channel_label} -> id={channel.get_id()}, name={channel.get_label()}"
        )
    except ChannelAlreadyExistsException:
        print(f"Warn: failed to create channel {channel_label}. Already exists !!")


def _add_content_source(channel_label, source_url, source_label, source_type="yum"):
    create_content_source(
        channel_label,
        repo_label=source_label,
        source_url=source_url,
        source_type=source_type,
    )


def _sync_channel(channel_label, cache_dir, batch_size=20, no_errata=False):
    """
    Synchronize the repositories of the given channel
    :return: A tuple that contains the success status and another return object (error message or a list of failed repos)
    """
    channel = get_channel_info_by_label(channel_label)
    channel_arch = channel["arch"].split("-", 1)[
        1
    ]  # Initially the value is like: 'channel-x86_64'
    if not channel:
        logging.error("Couldn't fetch channel with label %s", channel_label)
        return False, f"No channel with found with label {channel_label}"
    compatible_arches = get_compatible_arches(channel_label)
    if channel_arch and channel_arch != ".*" and channel_arch not in compatible_arches:
        logging.error(
            "Not compatible arch: %s for channel: %s",
            channel_arch,
            channel_label,
        )
        return False, f"Arch {channel_arch} is not compatible with {channel_label}"
    try:
        target_repos = db_utils.get_repositories_by_channel_label(channel_label)
    except NoSourceFoundForChannel as e:
        print("Error:", e.msg)
        return False, f"No source found for channel {channel_label}"

    failed_repos = []  # contains the list of labels of failed repos
    for repo in target_repos:
        if repo.repo_type == "yum":
            try:
                rpm_repo = RPMRepo(
                    repo.repo_label, cache_dir, repo.source_url, repo.channel_arch
                )
                logging.debug("Importing package for repo %s", repo.repo_label)
                failed = import_repository_packages_in_batch(
                    rpm_repo,
                    batch_size,
                    channel,
                    compatible_arches=compatible_arches,
                    no_errata=no_errata,
                )
                logging.debug(
                    "Completed import for repo %s with %d failed packages",
                    repo.repo_label,
                    failed,
                )
            except SignatureVerificationException as e:
                print(e.message)
                failed_repos.append(repo.repo_label)

        elif repo.repo_type == "deb":
            try:
                dep_repo = DebRepo(
                    repo.source_url,
                    cache_dir,
                    pkg_dir="/tmp",
                    channel_label=repo.channel_label,
                )
                dep_repo.verify()

                logging.debug("Importing package for repo %s", repo.repo_label)
                failed = import_repository_packages_in_batch(
                    dep_repo,
                    batch_size,
                    channel,
                    compatible_arches=compatible_arches,
                )
                logging.debug(
                    "Completed import for repo %s with %d failed packages",
                    repo.repo_label,
                    failed,
                )
            except GeneralRepoException as e:
                logging.error("__init__.py: Couldn't verify signature ! %s", e)
                failed_repos.append(repo.repo_label)

        else:
            # TODO: handle repositories other than yum and deb
            logging.debug("Not supported repo type: %s", repo.repo_type)
            continue
    return True, failed_repos


def _display_failed_repos(repos):
    """
    A display helper function
    """
    for repo_label in repos:
        print(f"=> Failed syncing repository: {repo_label}")


# TODO: [Whole file] Make better logging using the right log functions
def main():
    parser = argparse.ArgumentParser(
        description="Lazy reposync service",
        conflict_handler="resolve",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )

    parser.add_argument(
        "--url",
        "-u",
        help="The target url of the remote repository of which we'll "
        "parse the metadata",
        dest="url",
        type=str,
        default=None,
    )

    parser.add_argument(
        "-n",
        "--name",
        help="Name of the repository",
        dest="name",
        type=str,
        default="noname",
    )

    parser.add_argument(
        "-d",
        "--debug",
        help="Show debug messages",
        action="store_const",
        dest="loglevel",
        const=logging.DEBUG,
        default=logging.INFO,
    )

    parser.add_argument(
        "--cache",
        help="Path to the cache directory",
        dest="cache",
        default=".cache",
        type=str,
    )

    parser.add_argument(
        "-b",
        "--batch-size",
        help="Size of the batch (num of packages by batch)",
        dest="batch_size",
        default=20,
        type=int,
    )

    parser.add_argument(
        "-a",
        "--arch",
        help="A filter for package architecture. Can be a regex, for example: 'x86_64',  '(x86_64|arch_64)'",
        default=".*",
        dest="arch",
        type=str,
    )

    parser.add_argument(
        "-c",
        "--channel",
        help="The channel label of which you want to synchronize repositories",
        dest="channel",
        type=str,
        default=None,
    )

    parser.add_argument(
        "--type",
        help="Repo type (yum or deb)",
        dest="repo_type",
        type=str,
        default=None,
    )

    parser.add_argument(
        "--no-errata",
        help="Do not sync errata",
        action="store_true",
        dest="no_errata",
        default=False,
    )

    parser.add_argument(
        "--create-channel",
        help="Create a new channel by providing the 'channel_label', and the 'channel_arch' eg: x86_64.\n"
        "Eg: --create-channel test_channel x86_64",
        dest="channel_info",
        type=str,
        nargs=2,
    )

    parser.add_argument(
        "--create-content-source",
        help="Adding a new content source to channel by specifying 'channel_label', 'source_url', 'source_label' and 'type'\n"
        "Eg: --create-content-source test_channel https://download.opensuse.org/update/leap/15.5/oss/ leap15.5 yum",
        dest="source_info",
        type=str,
        nargs=4,
    )

    args = parser.parse_args()

    # Remove any existing handlers (loggers)
    for handler in logging.root.handlers[:]:
        logging.root.removeHandler(handler)
    logging.getLogger().setLevel(args.loglevel)

    # Creating a new channel
    if args.channel_info:
        _create_channel(args.channel_info[0], args.channel_info[1])
        return

    # Adding content source to channel
    if args.source_info:
        _add_content_source(
            args.source_info[0],
            args.source_info[1],
            args.source_info[2],
            args.source_info[3],
        )
        return

    arch = args.arch
    if arch != ".*":
        arch = f"(noarch|{args.arch})"

    if args.url:
        #### sync using url ###
        if not args.repo_type:
            print("ERROR: --type (yum/deb) must be specified when using --url")
            return
        if args.repo_type == "yum":
            repo = RPMRepo(args.name, args.cache, args.url, arch)
        elif args.repo_type == "deb":
            repo = DebRepo(args.url, args.cache, "/tmp")
            try:
                repo.verify()
            except GeneralRepoException as e:
                logging.error("__init__.py: Couldn't verify signature ! %s", e)
                exit(0)
        else:
            print(f"ERROR: not supported repo_type: {args.repo_type}")
            return
        compatible_arches = get_all_arches()
        failed = import_repository_packages_in_batch(
            repo, args.batch_size, compatible_arches=compatible_arches
        )
        logging.debug("Completed import with %d failed packages", failed)

    elif args.channel:
        #### sync using channel label ###
        _sync_channel(args.channel, args.cache, args.batch_size, args.no_errata)

    else:
        ### Execute Service Indefinitely               ###
        ### Continuously Looping over all the channels ###
        logging.info("Executing lzreposync service")
        while True:
            curr_channel_label = None
            try:
                all_channels = get_all_channels()
                if not all_channels:
                    print("No channels in the database! Leaving..")
                    return
                for channel in all_channels:
                    curr_channel_label = channel["channel_label"]
                    ch_status = channel["status"]
                    if ch_status == "pending" or ch_status == "failed":
                        print(
                            f"INFO: Start synchronizing channel: {curr_channel_label}"
                        )
                        # Update the channel status to 'in_progress'
                        update_channel_status(curr_channel_label, "in_progress")
                        success, res = _sync_channel(
                            curr_channel_label,
                            args.cache,
                            args.batch_size,
                            args.no_errata,
                        )
                        if not success:
                            channel["status"] = "failed"
                            update_channel_status(curr_channel_label, "failed")
                            print(
                                f"Failed synchronizing channel {curr_channel_label}. Error: {res}"
                            )
                        else:
                            if len(res) == 0:
                                # No failed repositories
                                update_channel_status(curr_channel_label, "done")
                                print(
                                    f"Successfully synchronized channel {curr_channel_label}"
                                )
                            else:
                                update_channel_status(curr_channel_label, "failed")
                                print(
                                    f"Failed to fully synchronize channel {curr_channel_label}. Finished with {len(res)} failed repos"
                                )
                                _display_failed_repos(res)
                sleep(SLEEP_TIME)
            except KeyboardInterrupt:
                print("Lzreposync is being stopped..")
                update_channel_status(curr_channel_label, "failed")
                exit(0)
