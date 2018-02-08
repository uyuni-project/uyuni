#!/usr/bin/python

from spacewalk.common.rhnLog import log_debug
from spacewalk.server import rhnSQL
from datetime import datetime
import time
import os, rpm

_package_path_prefix = '/var/spacewalk/'

#
# - find every sync after install/update to 3.1
#
_query_find_all_affected_sync = rhnSQL.Statement("""
    select
        created,
        version
    from rhnVersionInfo vi
        join rhnPackageEVR evr
            on vi.evr_id = evr.id
    where evr.version like '3.1.%'
    order by created
""")

def get_affected_syncs():
    h = rhnSQL.prepare(_query_find_all_affected_sync)
    h.execute()
    affectedsyncs = h.fetchall_dict()
    return affectedsyncs


#
# - get the list of package ids for certain version
#
_query_all_package_ids_by_version = rhnSQL.Statement("""
    select id
    from rhnPackageEVR
    where version = :version
""")

def get_all_package_ids_by_version(version):
    h = rhnSQL.prepare(_query_all_package_ids_by_version)
    h.execute(version=version)
    affected_package_ids = h.fetchall_dict()
    return affected_package_ids


#
# - every found package (rhnPackage column) has "path" and "buildtime".
#
_query_package_by_id = rhnSQL.Statement("""
    select
        p.id,
        p.path,
        p.build_time,
        pn.name
    from rhnPackage p
        join rhnPackageName pn
            on p.name_id = pn.id
    where p.id = :id
""")

def get_package_by_id(id):
    h = rhnSQL.prepare(_query_package_by_id)
    h.execute(id=id)
    package = h.fetchone_dict()
    return package


#
# - find rpm via "path", parse the correct build time
#
def get_rpm_tag_build_time(rpm_file):
    """Returns rpm information by querying a rpm"""
    transaction_set = rpm.ts()
    fd = os.open(rpm_file, os.O_RDONLY)
    try:
        hdr = transaction_set.hdrFromFdno(fd)
    except rpm.error:
        fd = os.open(rpm_file, os.O_RDONLY)
        transaction_set.setVSFlags(rpm._RPMVSF_NOSIGNATURES)
        hdr = transaction_set.hdrFromFdno(fd)
    os.close(fd)
    return hdr[rpm.RPMTAG_BUILDTIME]


#
# - update package build_time in DB
#
_update_package_build_time = rhnSQL.Statement("""
    update rhnPackage
    set build_time = :build_time
    where id = :id
""")

def update_package_build_time_by_id(id, build_time):
    h = rhnSQL.prepare(_update_package_build_time)
    ret = h.execute(id=id, build_time=build_time)
    return ret


#
# - delete package repodata snippet from DB
#
_delete_repodata_snippet_by_id = rhnSQL.Statement("""
    delete
    from rhnPackageRepodata
    where package_id = :id
""")

def delete_repodata_snippet_by_id(id):
    h = rhnSQL.prepare(_delete_repodata_snippet_by_id)
    ret = h.execute(id=id)
    return ret


#
# - trigger regeneration of all metadata for all channels
#
_insert_trigger_to_regenerate_metadata_for_all_channels = rhnSQL.Statement("""
    insert into rhnRepoRegenQueue (id, CHANNEL_LABEL, REASON, FORCE)
    (
        select
            sequence_nextval('rhn_repo_regen_queue_id_seq'),
            C.label,
            'fix names for cloned patches',
            'Y'
        from rhnChannel C
    )
""")

def regenerate_metadata_for_all_channels():
    h = rhnSQL.prepare(_insert_trigger_to_regenerate_metadata_for_all_channels)
    ret = h.execute()
    return ret


if __name__ == '__main__':
    
    rhnSQL.initDB()

    something_changed = False

    # find every sync after install/update to 3.1
    all_syncs = get_affected_syncs()

    print 'Affected syncs versions:'

    for sync in all_syncs :
        print '-', sync['version']

        # find all package id list for the current sync version
        package_id_list = get_all_package_ids_by_version(sync['version'])
        print ''
        print 'Scanning', len(package_id_list), 'packages for the', sync['version'], ' sync version..'

        # iterate on each package found on the sync version
        for package_id_blob in package_id_list:

            package_id = package_id_blob['id']

            # extract the package from the database
            current_db_package = get_package_by_id(package_id)
            
            # get the absolute path of the package on the filesystem
            absolute_package_path = _package_path_prefix + current_db_package['path']

            # extract the rpm_tag_build_time from the rpm
            rpm_tag_build_time = datetime(*time.gmtime(get_rpm_tag_build_time(absolute_package_path))[:6])

            # compare RPM vs DB "buildtime"s
            if current_db_package['build_time'] != rpm_tag_build_time:
                print ''
                print '###############'
                print 'id =', package_id
                print 'name =', current_db_package['name']
                print 'path =', absolute_package_path
                print ''
                print '>>> DB package build time  :', current_db_package['build_time']
                print '<<< RPM package build time :', rpm_tag_build_time
                print ''

                # update the row in DB
                ret = update_package_build_time_by_id(package_id, rpm_tag_build_time)
                if ret == 1:
                    print 'build_time updated for the package id', package_id
                    something_changed = True
                else:
                  print 'something went wrong during update of the build_time for the package id', package_id

                # remove the stored metadata snippet for this package
                ret = delete_repodata_snippet_by_id(package_id)
                if ret == 1:
                    print 'repodata snipped removed for the package id', package_id
                    something_changed = True
                else:
                    print 'something went wrong during remove of the repodata snippet for the package id', package_id
                print '###############'

    # finally trigger regeneration of all metadata for all channels
    if something_changed:
        ret = regenerate_metadata_for_all_channels()
        print 'queued', ret, 'channel metadata regeneration'

    rhnSQL.commit()