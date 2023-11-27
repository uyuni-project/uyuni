ALTER TABLE suseImageInfo RENAME COLUMN action_id TO build_action_id;

ALTER TABLE suseImageInfo ADD inspect_action_id NUMERIC;

ALTER TABLE suseImageInfo ADD CONSTRAINT suse_imginfo_aid_insp_fk
    FOREIGN KEY (inspect_action_id, build_server_id)
    REFERENCES rhnServerAction (action_id, server_id);

DROP VIEW suseImageOverview;

CREATE OR REPLACE VIEW
suseImageOverview
(
    org_id,
    image_id,
    image_name,
    image_version,
    checksum_id,
    modified,
    image_arch_name,
    build_action_id,
    inspect_action_id,
    profile_id,
    store_id,
    build_server_id,
    security_errata,
    bug_errata,
    enhancement_errata,
    outdated_packages,
    installed_packages
)
AS
SELECT
    i.org_id, i.id, i.name, i.version, i.checksum_id, i.modified,
    ( select name from rhnServerArch where id = i.image_arch_id), i.build_action_id,
    i.inspect_action_id, i.profile_id, i.store_id, i.build_server_id,
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
FROM
    suseImageInfo i
;
