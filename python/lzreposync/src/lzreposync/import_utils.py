#  pylint: disable=missing-module-docstring

import sys

from spacewalk.satellite_tools.syncLib import log2, log
from spacewalk.server import rhnSQL
from spacewalk.server.importlib import importLib, packageImport
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.debPackage import debBinaryPackage
from spacewalk.server.importlib.headerSource import rpmBinaryPackage
from spacewalk.server.importlib.packageImport import ChannelPackageSubscription


# TODO: remove this function from here and include it as a method in LzReposync class
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


# TODO: should be changed to 'self.channel_label' and 'self.channel' when changing to LzReposync class
def associate_package(pack, channel_label, channel):
    pack["channels"] = [{"label": channel_label, "id": channel["id"]}]

    return pack


def chunks(seq, n):
    return (seq[i : i + n] for i in range(0, len(seq), n))


# TODO: 'to_disassociate', 'to_link': are they important ?
# TODO: 'to_link'
# TODO: update the 'channel' and 'channel_label' parameters
def import_package_batch(
    to_process, batch_index=-1, to_link=True, channel=None, channel_label=None
):
    """
    Importing rpm packages
    """
    # Prepare SQL statements
    rhnSQL.closeDB(
        committing=False, closing=False
    )  # TODO: not sure what this exactly do
    rhnSQL.initDB()

    backend = SQLBackend()
    mpm_bin_batch = importLib.Collection()  # bin packages
    mpm_src_batch = importLib.Collection()  # src packages

    # affected_channels = []  # TODO: not sure if we need it

    upload_caller = "server.app.uploadPackage"  # TODO: not sure what this exactly do
    # pylint: disable-next=invalid-name
    # with cfg_component("server.susemanager") as CFG:
    #     mount_point = CFG.MOUNT_POINT
    import_count = 0
    batch_size = len(to_process)
    initial_size = batch_size
    # all_packages = set()  # TODO: see reposync

    # Formatting the packages for importLib
    for package in to_process:

        # pylint: disable=W0703,W0706
        try:
            import_count += 1
            if isinstance(package, rpmBinaryPackage) or isinstance(
                package, debBinaryPackage
            ):
                mpm_bin_batch.append(package)
            else:
                # it is rpmSourcePacakge
                mpm_src_batch.append(package)
        except (KeyboardInterrupt, rhnSQL.SQLError):
            raise
        except Exception as e:
            e_message = f"Exception: {e}"
            log2(0, 1, e_message, stream=sys.stderr)
            # raise e # Ignore the package and continue

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
            del mpm_bin_batch

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
            del mpm_src_batch

    except (KeyboardInterrupt, rhnSQL.SQLError):
        raise
    except Exception as e:
        e_message = f"Exception: {e}"
        log2(0, 1, e_message, stream=sys.stderr)
        # raise e # Ignore the package and continue
    finally:
        # Cleanup if cache..if applied
        pass

    # Disassociate packages TODO

    # TODO: update the linking process and condition checking (see how reposync handles it, each package a part)
    if to_link and channel:
        log(0, "")
        log(0, "  Linking packages to the channel.")

        # Packages to append to channel
        # TODO: can we do this reformat in a previous stage to avoid looping again over the lists (it depends on the to_link value)
        import_batches = list(
            chunks(
                [
                    associate_package(pack, channel_label, channel)
                    for pack in to_process
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

        # package.clear_header()  # TODO See reposync
    rhnSQL.closeDB()
    # pylint: disable-next=consider-using-f-string
    log(0, " Pacakge batch #{} completed...".format(batch_index))

    return initial_size - batch_size  # return the number of failed packages
    # TODO: update the return value
