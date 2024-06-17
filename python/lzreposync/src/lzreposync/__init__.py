import argparse
import logging
from itertools import islice

from lzreposync.importUtils import import_package_batch
from lzreposync.rpm_repo import RPMRepo


# TODO: put this function in a better location
def batched(iterable, n):
    if n < 1:
        raise ValueError('n must be at least one')
    iterator = iter(iterable)
    while batch := tuple(islice(iterator, n)):
        yield batch


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

    args = parser.parse_args()

    logging.getLogger().setLevel(args.loglevel)
    rpm_repository = RPMRepo(args.name, args.cache, args.url)  # TODO args.url should be args.repo, no ?
    packages = rpm_repository.get_packages_metadata()  # packages is a generator
    for batch in batched(packages, args.batch_size):
        logging.info(f"Importing a batch of {len(batch)} packages...")
        import_package_batch(batch)
