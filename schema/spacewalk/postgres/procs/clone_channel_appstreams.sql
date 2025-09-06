CREATE OR REPLACE PROCEDURE clone_channel_appstreams(from_channel_id NUMERIC, to_channel_id NUMERIC)
AS $$
BEGIN
    -- Clone the AppStreams
    INSERT INTO suseAppStream (id, channel_id, name, stream, version, context, arch)
    SELECT
        nextval('suse_as_module_seq'),
        to_channel_id,
        name,
        stream,
        version,
        context,
        arch
    FROM suseAppStream
    WHERE channel_id = from_channel_id;

    -- Clone the Packages
    INSERT INTO suseAppstreamPackage (module_id, package_id)
    SELECT
        new_as.id,
        asp.package_id
    FROM suseAppstreamPackage asp
    JOIN suseAppStream old_as ON asp.module_id = old_as.id
    JOIN suseAppStream new_as ON old_as.name = new_as.name
                           AND old_as.stream = new_as.stream
                           AND old_as.version = new_as.version
                           AND old_as.context = new_as.context
                           AND old_as.arch = new_as.arch
                           AND new_as.channel_id = to_channel_id
    WHERE old_as.channel_id = from_channel_id;

    -- Clone the RPMs
    INSERT INTO suseAppstreamApi (module_id, rpm)
    SELECT
        new_as.id,
        asapi.rpm
    FROM suseAppstreamApi asapi
    JOIN suseAppStream old_as ON asapi.module_id = old_as.id
    JOIN suseAppStream new_as ON old_as.name = new_as.name
                           AND old_as.stream = new_as.stream
                           AND old_as.version = new_as.version
                           AND old_as.context = new_as.context
                           AND old_as.arch = new_as.arch
                           AND new_as.channel_id = to_channel_id
    WHERE old_as.channel_id = from_channel_id;

END;
$$
LANGUAGE plpgsql;
