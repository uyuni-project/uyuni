#  pylint: disable=missing-module-docstring
import logging
import os
import sys
from itertools import islice

from lzreposync import updates_util
from lzreposync.rpm_repo import RPMRepo
from lzreposync.updates_importer import UpdatesImporter
from spacewalk.satellite_tools.syncLib import log2, log
from spacewalk.server import rhnSQL
from spacewalk.server.importlib import importLib, packageImport
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.debPackage import debBinaryPackage
from spacewalk.server.importlib.headerSource import rpmBinaryPackage
from spacewalk.server.importlib.packageImport import ChannelPackageSubscription


def batched(iterable, n):
    if n < 1:
        raise ValueError("n must be at least one")
    iterator = iter(iterable)
    while batch := tuple(islice(iterator, n)):
        yield batch


def import_repository_updates(channel, repository, available_packages: dict):
    """
    Import the updates/patches of the given repo
    :available_packages: a dict of available packages in the format: { name-epoch-version-release-arch : 1 }
    """
    if not isinstance(repository, RPMRepo):
        logging.error("Can only import patches of rpm repositories !")
        return
    updateinfo_url = repository.find_metadata_file_url("updateinfo")
    if not updateinfo_url:
        logging.debug(
            "Couldn't find 'updateinfo' file in repository %s", repository.name
        )
    else:
        updateinfo_file = updates_util.download_file(updateinfo_url)
        if not updateinfo_file:
            logging.error("Couldn't download updateinfo file: %s", updateinfo_url)
            return
        notices = updates_util.get_updates(updateinfo_file)
        updates_importer = UpdatesImporter(
            channel_label=channel["label"], available_packages=available_packages
        )
        updates_importer.import_updates(notices)
        # Deleting the patches_importer will call the rhnSQL.closeDB() and free up a db connection
        del updates_importer
        os.remove(updateinfo_file)


def import_repository_packages_in_batch(
    repository, batch_size, channel=None, compatible_arches=None, no_errata=False
):
    """
    Return the number of failed packages
    """
    total_failed = 0
    packages = repository.get_packages_metadata()  # packages is a generator
    available_packages = {}
    for i, batch in enumerate(batched(packages, batch_size)):
        failed, _, avail_pkgs = import_package_batch(
            to_process=batch,
            compatible_archs=compatible_arches,
            batch_index=i,
            to_link=True,
            channel=channel,
        )
        total_failed += failed
        available_packages.update(avail_pkgs)
    # Importing updates/patches (sync errata)
    if not no_errata and isinstance(repository, RPMRepo):
        if not channel:
            logging.warning("Couldn't import updates, no channel was specified !")
        else:
            import_repository_updates(
                channel=channel,
                repository=repository,
                available_packages=available_packages,
            )

    del available_packages

    return total_failed


def import_packages_in_batch(
    packages, batch_size, channel=None, compatible_arches=None
):
    """
    Import packages form parser
    :packages: a generator of packages
    Return a tuple of (failed, available_packages)
    ( available_packages: a dict of available packages in the format: {name-epoch-version-release-arch:1} )
    """
    total_failed = 0
    # TODO: (enhancement) can we add parallelism/multithreading here ? discuss with team
    for i, batch in enumerate(batched(packages, batch_size)):
        failed, _, _ = import_package_batch(
            to_process=batch,
            compatible_archs=compatible_arches,
            batch_index=i,
            to_link=True,
            channel=channel,
        )
        total_failed += failed
    return total_failed


def disassociate_package(checksum_type, checksum, channel):
    log(
        3,
        # pylint: disable-next=consider-using-f-string
        "Disassociating package with checksum: %s (%s)" % (checksum, checksum_type),
    )
    h = rhnSQL.prepare(
        """
        delete from rhnChannelPackage cp
         where cp.channel_id = :channel_id
           and cp.package_id in (select p.id
                                   from rhnPackage p
                                   join rhnChecksumView c
                                     on p.checksum_id = c.id
                                  where c.checksum = :checksum
                                    and c.checksum_type = :checksum_type
                                )
    """
    )
    h.execute(
        channel_id=int(channel["id"]),
        checksum_type=checksum_type,
        checksum=checksum,
    )


def associate_package(pack, channel_label, channel):
    pack["channels"] = [{"label": channel_label, "id": channel["id"]}]

    return pack


def chunks(seq, n):
    return (seq[i : i + n] for i in range(0, len(seq), n))


def import_package_batch(
    to_process, compatible_archs=None, batch_index=-1, to_link=True, channel=None
):
    """
    Importing rpm packages
    return a tuple of: (failed, skipped, available_packages)
    ( available_packages a dict of available packages in the format: {name-epoch-version-release-arch:1} )
    """
    if compatible_archs is None:
        compatible_archs = []
    available_packages = {}
    skipped = 0
    rhnSQL.closeDB(committing=False, closing=False)
    rhnSQL.initDB()

    backend = SQLBackend()
    mpm_bin_batch = importLib.Collection()  # bin packages
    mpm_src_batch = importLib.Collection()  # src packages

    # affected_channels = []  # TODO: not sure if we need it - discuss with team

    upload_caller = "server.app.uploadPackage"
    import_count = 0
    batch_size = len(to_process)
    initial_size = batch_size

    # Formatting the packages for importLib
    for package in to_process:
        if package["arch"] not in compatible_archs:
            logging.debug("Arch %s is not supported. Skipping...", package["arch"])
            continue
        # pylint: disable=W0703,W0706
        try:
            import_count += 1
            if isinstance(package, (rpmBinaryPackage, debBinaryPackage)):
                mpm_bin_batch.append(package)
            else:
                # it is rpmSourcePacakge
                mpm_src_batch.append(package)
            if compatible_archs and package.arch not in compatible_archs:
                # skip packages with incompatible architecture
                logging.debug(
                    "Skipping package %s with incompatible arch %s",
                    package.get("name"),
                    package.get("arch"),
                )
                skipped += 1
                continue
            epoch = ""
            if package["epoch"] and package["epoch"] != "0":
                # pylint: disable-next=consider-using-f-string
                epoch = "%s:" % package["epoch"]
            # pylint: disable-next=consider-using-f-string
            ident = "%s-%s%s-%s.%s" % (
                package["name"],
                epoch,
                package["version"],
                package["release"],
                package["arch"],
            )

            # the available_packages dict will be used later in the updates import
            # It holds information about all the imported packages
            # TODO discuss with team the use of it
            available_packages[ident] = 1
        except (KeyboardInterrupt, rhnSQL.SQLError):
            raise
        except Exception as e:
            e_message = f"Exception: {e}"
            log2(0, 1, e_message, stream=sys.stderr)
            raise e

    # Importing packages
    # pylint: disable=W0703,W0706
    try:
        # Importing the batch of binary packages
        if mpm_bin_batch:
            log(
                0,
                # pylint: disable-next=consider-using-f-string
                " Importing a sub batch of {} Binary packages...".format(
                    len(mpm_bin_batch)
                ),
            )
            importer = packageImport.PackageImport(
                mpm_bin_batch, backend, caller=upload_caller, import_signatures=False
            )
            importer.setUploadForce(1)
            importer.run()
            rhnSQL.commit()
            del importer.batch

        # Importing the batch of source packages
        if mpm_src_batch:
            # pylint: disable-next=consider-using-f-string
            log(
                0,
                # pylint: disable-next=consider-using-f-string
                " Importing a sub batch of {} Source packages...".format(
                    len(mpm_src_batch)
                ),
            )
            src_importer = packageImport.SourcePackageImport(
                mpm_src_batch, backend, caller=upload_caller
            )
            src_importer.setUploadForce(1)
            src_importer.run()
            rhnSQL.commit()

    except (KeyboardInterrupt, rhnSQL.SQLError):
        raise
    except Exception as e:
        e_message = f"Exception: {e}"
        log2(0, 1, e_message, stream=sys.stderr)
        raise e  # Ignore the package and continue
    finally:

        # Linking packages to the channel
        if to_link and channel:
            log(0, "")
            log(0, "  Linking packages to the channel.")

            # Packages to append to channel
            import_batches = list(
                chunks(
                    [
                        associate_package(pack, channel["label"], channel)
                        for pack in list(mpm_bin_batch) + list(mpm_src_batch)
                    ],
                    1000,
                )
            )
            count = 0
            for import_batch in import_batches:
                backend = SQLBackend()
                caller = "server.app.yumreposync"
                importer = ChannelPackageSubscription(
                    import_batch, backend, caller=caller, repogen=False
                )
                importer.run()
                backend.commit()
                del importer.batch
                count += len(import_batch)
                # pylint: disable-next=consider-using-f-string
                log(0, "    {} packages linked".format(count))

        rhnSQL.closeDB()
    del mpm_bin_batch
    del mpm_src_batch
    # pylint: disable-next=consider-using-f-string
    log(0, " Pacakge batch #{} completed...".format(batch_index))

    failed = initial_size - batch_size
    return failed, skipped, available_packages
