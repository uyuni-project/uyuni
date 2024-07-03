import logging
import sys

from spacewalk.satellite_tools.syncLib import log2, log
from spacewalk.server import rhnSQL
from spacewalk.server.importlib import importLib, mpmSource, packageImport
from spacewalk.server.importlib.backendOracle import SQLBackend
from spacewalk.server.importlib.importLib import InvalidArchError


# TODO: rename 'to_process' into 'package_batch'
# TODO: 'to_disassociate', 'to_link': are they important ?
def import_package_batch(to_process, batch_index=-1):
    # Prepare SQL statements
    rhnSQL.closeDB(committing=False, closing=False)  # TODO: not sure what this exactly do
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
    failed_packages = 0
    batch_size = len(to_process)
    initial_size = batch_size
    # all_packages = set()  # TODO: see reposync

    # Formatting the packages for importLib
    for package in to_process:

        # Ignoring packages with arch='aarch64_ilp32' for the moment
        # TODO: fix later. Note: we should make this more generalized with possibly other unrecognized archs
        if package["header"]["arch"] == "aarch64_ilp32":
            logging.debug("Ignoring package {} - Cannot process arch {}".
                          format(package["checksum"], package["header"]["arch"]))
            batch_size -= 1
            continue
        try:
            # print(f"INFO: Importing package. HEADER= {package['header'].keys()}")
            import_count += 1
            pkg = mpmSource.create_package(
                package["header"],
                size=package["package_size"],
                checksum_type=package["checksum_type"],
                checksum=package["checksum"],
                relpath="/var",  # TODO: what is 'relpath' ?
                org_id=1,  # TODO: correct
                header_start=package["header_start"],
                header_end=package["header_end"],
                channels=[]
            )

            if package['header'].is_source:
                mpm_src_batch.append(pkg)
            else:
                mpm_bin_batch.append(pkg)
        except (KeyboardInterrupt, rhnSQL.SQLError):
            raise
        except Exception:
            failed_packages += 1
            # TODO: maybe other stuff to do (like in the reposync)

    # Importing packages
    try:
        # Importing the batch of binary packages
        if mpm_bin_batch:
            log(0, " Importing a sub batch of {} Binary packages...".format(len(mpm_bin_batch)))
            importer = packageImport.PackageImport(
                mpm_bin_batch, backend, caller=upload_caller
            )
            importer.setUploadForce(1)
            importer.run()
            rhnSQL.commit()
            del importer.batch
            del mpm_bin_batch

        # Importing the batch of source packages
        if mpm_src_batch:
            log(0, " Importing a sub batch of {} Source packages...".format(len(mpm_src_batch)))
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
        failed_packages += 1
        e_message = f"Exception: {e}"
        log2(0, 1, e_message, stream=sys.stderr)
        raise e
    finally:
        # Cleanup if cache..if applied
        pass

        # package.clear_header()  # TODO See reposync
    rhnSQL.closeDB()
    log(
        0,
        " Pacakge batch #{} completed...".format(batch_index)
    )

    return initial_size-batch_size  # return the number of failed packages
    # TODO: return somthing else?
