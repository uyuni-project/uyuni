#  pylint: disable=missing-module-docstring

import argparse
import logging
from itertools import islice

from lzreposync import db_utils
from lzreposync.import_utils import import_package_batch
from lzreposync.rpm_repo import RPMRepo


# TODO: put this function in a better location
def batched(iterable, n):
    if n < 1:
        raise ValueError("n must be at least one")
    iterator = iter(iterable)
    while batch := tuple(islice(iterator, n)):
        yield batch


def import_repository_packages_batch(repository, batch_size):
    failed = 0
    packages = repository.get_packages_metadata()
    for i, batch in enumerate(batched(packages, batch_size)):
        failed += import_package_batch(batch, i)
    return failed


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
        "-c",
        "--cache",
        help="Path to the cache directory",
        dest="cache",
        default=".lzreposync-cache",
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
        "--channel",
        help="The channel label of which you want to synchronize repositories",
        dest="channel",
        type=str,
        default=None,
    )

    # TODO encapsulate everything in a class LzRepoSync

    args = parser.parse_args()
    arch = args.arch
    if arch != ".*":
        # pylint: disable-next=consider-using-f-string
        arch = "(noarch|{})".format(args.arch)

    logging.getLogger().setLevel(args.loglevel)
    if args.url:
        rpm_repository = RPMRepo(args.name, args.cache, args.url, arch)
        failed = import_repository_packages_batch(rpm_repository, args.batch_size)
        logging.debug("Completed import with %d failed packages", failed)

    else:
        # No url specified
        if args.channel:
            channel_label = args.channel
            target_repos = db_utils.get_repositories_by_channel_label(channel_label)
            for repo in target_repos:
                if repo.repo_type == "yum":
                    rpm_repository = RPMRepo(
                        repo.repo_label, args.cache, repo.source_url, repo.channel_arch
                    )
                    logging.debug("Importing package for repo %s", repo.repo_label)
                    failed = import_repository_packages_batch(
                        rpm_repository, args.batch_size
                    )
                    logging.debug(
                        "Completed import for repo %s with %d failed packages",
                        repo.repo_label,
                        failed,
                    )

                else:
                    # TODO: handle repositories other than rpm
                    logging.debug("Not supported repo type: %s", repo.repo_type)
                    continue
        else:
            logging.error("Either --url or --channel must be specified")
