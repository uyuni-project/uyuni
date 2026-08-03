--
-- We only need to change the update tag for sle-module-live-patching15-sp7 channels
-- SP6 and older are in LTSS and due to this the patches are separated already
--

UPDATE susechanneltemplate
   SET update_tag = NULL
 WHERE channel_label LIKE 'sle-module-live-patching15-sp7%';

UPDATE rhnchannel
   SET update_tag = NULL
 WHERE org_id IS NULL
   AND label LIKE 'sle-module-live-patching15-sp7%';

-- This require a re-sync of the channels to get the errata back as standalone errata
DELETE FROM rhnerrata
 WHERE id IN (SELECT ce.errata_id
                FROM rhnchannel c
                JOIN rhnchannelerrata ce ON c.id = ce.channel_id
               WHERE c.org_id IS NULL
                 AND c.label LIKE 'sle-module-live-patching15-sp7%');

INSERT INTO rhnTaskQueue (id, org_id, task_name, task_data)
  SELECT nextval('rhn_task_queue_id_seq'), id, 'upgrade_satellite_mgr_sync_all', 0
   FROM web_customer
  WHERE id = 1;

