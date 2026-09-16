-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DROP INDEX suse_img_repodigest_idx;

CREATE UNIQUE INDEX suse_img_repodigest_idx
    ON suseImageRepoDigest(repo_digest, image_history_id)
    
    ;
