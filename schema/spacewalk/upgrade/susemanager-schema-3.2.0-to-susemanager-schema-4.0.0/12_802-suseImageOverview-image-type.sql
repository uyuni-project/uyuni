DROP VIEW suseImageOverview;

CREATE OR REPLACE VIEW suseImageOverview
(
    org_id,
    image_id,
    image_name,
    image_version,
    image_type,
    checksum_id,
    modified,
    image_arch_name,
    build_action_id,
    inspect_action_id,
    profile_id,
    store_id,
    build_server_id,
    curr_revision_num,
    external_image,
    security_errata,
    bug_errata,
    enhancement_errata,
    outdated_packages,
    installed_packages
)
as
select
    i.org_id, i.id, i.name, i.version, i.image_type, i.checksum_id, i.modified,
    ( select name from rhnServerArch where id = i.image_arch_id), i.build_action_id,
    i.inspect_action_id, i.profile_id, i.store_id, i.build_server_id, i.curr_revision_num,
    i.external_image,
    ( select count(*) from rhnImageErrataTypeView ietv
      where
            ietv.image_id = i.id
        and ietv.errata_type = 'Security Advisory'),
    ( select count(*) from rhnImageErrataTypeView ietv
      where
            ietv.image_id = i.id
        and ietv.errata_type = 'Bug Fix Advisory'),
    ( select count(*) from rhnImageErrataTypeView ietv
      where
            ietv.image_id = i.id
        and ietv.errata_type = 'Product Enhancement Advisory'),
    ( select count(distinct p.name_id) from rhnPackage p, rhnImageNeededPackageCache inpc
      where
            inpc.image_id = i.id
        and p.id = inpc.package_id),
    (select count(*) from suseImageInfoPackage iip
     where
            iip.image_info_id = i.id)
from
    suseImageInfo i
;
