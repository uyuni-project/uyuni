DO $$
    BEGIN
        IF EXISTS
            (
                SELECT 1
                FROM information_schema.columns
                WHERE table_name='suseimagerepodigest' AND column_name='image_history_id'
            )
        THEN
            CREATE TEMP TABLE tmpImageDigest AS
                 SELECT name,
                   version,
                   image_type,
                   org_id,
                   store_id,
                   revision_num,
                   suseImageBuildHistory.created,
                   suseImageBuildHistory.modified,
                   repo_digest
                 FROM suseImageInfo
                     LEFT JOIN suseImageBuildHistory ON suseImageInfo.id = suseImageBuildHistory.image_info_id
                     LEFT JOIN suseImageRepoDigest ON suseImageBuildHistory.id = suseImageRepoDigest.image_history_id;

            CREATE TEMP TABLE tmpImageHistory AS
            SELECT name,
                   version,
                   image_type,
                   checksum_id,
                   image_arch_id,
                   org_id,
                   profile_id,
                   store_id,
                   build_server_id,
                   external_image,
                   revision_num,
                   curr_revision_num,
                   suseImageBuildHistory.created,
                   suseImageBuildHistory.modified
                 FROM suseImageInfo
                     LEFT JOIN suseImageBuildHistory ON suseImageInfo.id = suseImageBuildHistory.image_info_id;

            INSERT INTO suseImageInfo (
                  id,
                  name,
                  version,
                  image_type,
                  image_arch_id,
                  org_id,
                  profile_id,
                  store_id,
                  build_server_id,
                  external_image,
                  curr_revision_num,
                  created,
                  modified,
                  obsolete
                )
                SELECT sequence_nextval('suse_imginfo_imgid_seq'),
                       name,
                       version,
                       image_type,
                       image_arch_id,
                       org_id,
                       profile_id,
                       store_id,
                       build_server_id,
                       external_image,
                       revision_num,
                       created,
                       modified,
                       'Y'
                     FROM tmpImagehistory
                         WHERE revision_num != curr_revision_num;


            DROP TABLE suseImageRepoDigest;
            DROP SEQUENCE suse_img_repodigest_id_seq;
            DROP TABLE suseImageBuildHistory;

            CREATE TABLE suseImageRepoDigest
            (
                id             NUMERIC NOT NULL
                                 CONSTRAINT suse_rdigest_id_pk PRIMARY KEY,
                image_info_id    NUMERIC NOT NULL,
                repo_digest    VARCHAR(255) NOT NULL,
                created        TIMESTAMPTZ
                                 DEFAULT (current_timestamp) NOT NULL,
                modified       TIMESTAMPTZ
                                 DEFAULT (current_timestamp) NOT NULL,
                CONSTRAINT suse_rdigest_imginfo_fk FOREIGN KEY (image_info_id)
                    REFERENCES suseImageInfo (id) ON DELETE CASCADE
            );


            CREATE SEQUENCE suse_img_repodigest_id_seq;

            INSERT INTO suseImageRepoDigest (id, image_info_id, repo_digest, created, modified)
                   SELECT sequence_nextval('suse_img_repodigest_id_seq'), suseImageInfo.id, repo_digest, tmpImageDigest.created, tmpImageDigest.modified
                       FROM suseImageInfo, tmpImageDigest
                          WHERE suseImageInfo.name = tmpImageDigest.name
                            AND suseImageInfo.version = tmpImageDigest.version
                            AND suseImageInfo.image_type = tmpImageDigest.image_type
                            AND suseImageInfo.org_id = tmpImageDigest.org_id
                            AND suseImageInfo.store_id = tmpImageDigest.store_id
                            AND suseImageInfo.curr_revision_num = tmpImageDigest.revision_num;

            DROP TABLE tmpImageDigest;
            DROP TABLE tmpImageHistory;

        END IF;
    END
$$ LANGUAGE plpgsql;
