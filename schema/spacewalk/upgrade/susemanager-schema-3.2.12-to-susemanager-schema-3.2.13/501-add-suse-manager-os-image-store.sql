INSERT INTO suseImageStoreType (id, label, name) VALUES
    (sequence_nextval('suse_imgstore_type_id_seq'), 'os_image', 'OS Image');

INSERT INTO suseImageStore (id, label, uri, store_type_id, org_id)
SELECT
    sequence_nextval('suse_imgstore_id_seq'),
    'SUSE Manager OS Image Store',
    wc.id || '/',
    (SELECT id FROM suseImageStoreType WHERE label = 'os_image'),
    wc.id
FROM web_customer wc
WHERE wc.id NOT IN (SELECT org_id
                    FROM suseImageStore
                    WHERE store_type_id = (SELECT id FROM suseImageStoreType WHERE label = 'os_image')
                        AND label = 'suse-manager-os-image-store');
