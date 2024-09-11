#  pylint: disable=missing-module-docstring

import argparse
import logging

from lzreposync import db_utils, updates_util
from lzreposync.db_utils import (
    get_compatible_arches,
    get_channel_info_by_label,
    get_all_arches,
    create_channel,
    ChannelAlreadyExistsException,
    NoSourceFoundForChannel,
)
from lzreposync.import_utils import (
    import_package_batch,
    batched,
    import_repository_packages_in_batch,
)
from lzreposync.rpm_repo import RPMRepo
from spacewalk.common.repo import GeneralRepoException
from spacewalk.satellite_tools.repo_plugins.deb_src import DebRepo


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
        "-D",
        "--debug",
        help="Show debug messages",
        action="store_const",
        dest="loglevel",
        const=logging.DEBUG,
        default=logging.INFO,
    )

    parser.add_argument(
        "-c",
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

    args = parser.parse_args()

    # Remove any existing handlers (loggers)
    for handler in logging.root.handlers[:]:
        logging.root.removeHandler(handler)
    logging.getLogger().setLevel(args.loglevel)

    # Creating a new channel
    if args.channel_info:
        channel_label, channel_arch = args.channel_info[0], args.channel_info[1]
        print(
            f"Creating a new channel with label: {channel_label}, and arch: {channel_arch}"
        )
        try:
            channel = create_channel(
                channel_label=channel_label, channel_arch=channel_arch
            )
            print(
                f"Info: successfully created channel: {channel_label} -> id={channel.get_id()}, name={channel.get_label()}"
            )
        except ChannelAlreadyExistsException:
            print(f"Warn: failed to create channel {channel_label}. Already exists !!")
        return

    arch = args.arch
    if arch != ".*":
        arch = f"(noarch|{args.arch})"

    if args.url:
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

    else:
        # No url specified
        if args.channel:
            channel_label = args.channel
            channel = get_channel_info_by_label(channel_label)
            if not channel:
                logging.error("Couldn't fetch channel with label %s", channel_label)
                return
            compatible_arches = get_compatible_arches(channel_label)
            if args.arch and args.arch != ".*" and args.arch not in compatible_arches:
                logging.error(
                    "Not compatible arch: %s for channel: %s",
                    args.channel_arch,
                    args.channel,
                )
                return
            try:
                target_repos = db_utils.get_repositories_by_channel_label(channel_label)
            except NoSourceFoundForChannel as e:
                print("Error:", e.msg)
                return
            for repo in target_repos:
                if repo.repo_type == "yum":
                    rpm_repo = RPMRepo(
                        repo.repo_label, args.cache, repo.source_url, repo.channel_arch
                    )
                    logging.debug("Importing package for repo %s", repo.repo_label)
                    failed = import_repository_packages_in_batch(
                        rpm_repo,
                        args.batch_size,
                        channel,
                        compatible_arches=compatible_arches,
                        no_errata=args.no_errata,
                    )
                    logging.debug(
                        "Completed import for repo %s with %d failed packages",
                        repo.repo_label,
                        failed,
                    )
                elif repo.repo_type == "deb":
                    dep_repo = DebRepo(
                        repo.source_url,
                        args.cache,
                        pkg_dir="/tmp",
                        channel_label=repo.channel_label,
                    )
                    try:
                        dep_repo.verify()
                    except GeneralRepoException as e:
                        logging.error("__init__.py: Couldn't verify signature ! %s", e)
                        exit(0)

                    logging.debug("Importing package for repo %s", repo.repo_label)
                    failed = import_repository_packages_in_batch(
                        dep_repo,
                        args.batch_size,
                        channel,
                        compatible_arches=compatible_arches,
                    )
                    logging.debug(
                        "Completed import for repo %s with %d failed packages",
                        repo.repo_label,
                        failed,
                    )
                else:
                    # TODO: handle repositories other than yum and deb
                    logging.debug("Not supported repo type: %s", repo.repo_type)
                    continue

        else:
            logging.error("Either --url or --channel must be specified")
