-------------------------------------------------------------------------------
-- CONFIG CHANNEL -------------------------------------------------------------
-------------------------------------------------------------------------------
INSERT INTO rhnConfigChannel(
    id,
    org_id,
    confchan_type_id,
    name,
    label,
    description)
SELECT
   sequence_nextval('rhn_confchan_id_seq'),
   org_id,
   (SELECT id FROM rhnConfigChannelType WHERE label = 'state'),
   state_name,
   state_name,
   state_name
FROM suseCustomState
WHERE
    state_deleted = 'N';

-------------------------------------------------------------------------------
-- CONFIG FILE ----------------------------------------------------------------
-------------------------------------------------------------------------------
-- NAME
INSERT INTO rhnConfigFileName(
    id,
    path)
SELECT
    sequence_nextval('rhn_cfname_id_seq'),
    '/init.sls'
FROM DUAL
WHERE NOT EXISTS (SELECT id FROM rhnConfigFileName WHERE path = '/init.sls');


-- FILE
-- (latest_config_revision_id will be updated after the revision is created)
INSERT INTO rhnConfigFile(
    id,
    config_channel_id,
    config_file_name_id,
    state_id
)
SELECT
    sequence_nextval('rhn_conffile_id_seq'),
    cc.id,
    (SELECT id FROM rhnConfigFileName WHERE path = '/init.sls'),
    (SELECT id from rhnConfigFileState WHERE label = 'alive')
FROM rhnConfigChannel cc
WHERE
    cc.confchan_type_id IN (SELECT id from rhnConfigChannelType WHERE label = 'state');

-------------------------------------------------------------------------------
-- CONFIG REVISION ------------------------------------------------------------
-------------------------------------------------------------------------------
-- CONTENT
-- initial version is empty
SELECT lookup_checksum('md5', 'd41d8cd98f00b204e9800998ecf8427e') FROM DUAL;

INSERT INTO rhnConfigContent(
    id,
    contents,
    file_size,
    checksum_id,
    is_binary,
    delim_start,
    delim_end)
SELECT
    sequence_nextval('rhn_confcontent_id_seq'),
    '',
    0,
    lookup_checksum('md5', 'd41d8cd98f00b204e9800998ecf8427e'), -- empty content
    'N',
    '{|',
    '|}'
FROM DUAL;

-- REVISION
SELECT lookup_config_info('root', 'root', 644, null, null) FROM dual;
INSERT INTO rhnConfigRevision(
    id,
    revision,
    config_file_id,
    config_content_id,
    config_info_id,
    config_file_type_id
)
SELECT
    sequence_nextval('rhn_confrevision_id_seq'),
    1,
    cf.id,
    (SELECT MAX(id) FROM rhnConfigContent WHERE checksum_id = lookup_checksum('md5', 'd41d8cd98f00b204e9800998ecf8427e')),
    lookup_config_info('root', 'root', 644, null, null),
    (SELECT id FROM rhnConfigFileType WHERE label = 'sls')
FROM rhnConfigFile cf
WHERE
    config_file_name_id = (SELECT id FROM rhnConfigFileName WHERE path = '/init.sls') AND
    NOT EXISTS (SELECT id FROM rhnConfigRevision WHERE config_file_id = cf.id);


-- update latest_config_revision_id in rhnConfigFile
UPDATE rhnConfigFile cf
SET latest_config_revision_id = (SELECT MAX(id) FROM rhnConfigRevision WHERE config_file_id = cf.id)
WHERE
    cf.config_file_name_id IN (SELECT id FROM rhnConfigFileName WHERE path = '/init.sls') AND
    cf.latest_config_revision_id IS NULL;
