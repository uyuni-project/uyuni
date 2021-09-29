from spacewalk.common.rhnConfig import initCFG, CFG
from spacewalk.server import rhnSQL
import json

def getNotificationsTypeDisabled():
    """Return list of types which are disabled"""
    disabledTypes = []
    comp = CFG.getComponent()
    initCFG("java")
    if CFG.notifications_type_disabled:
        disabledTypes = CFG.notifications_type_disabled.split(",")
    initCFG(comp)
    return disabledTypes

class CreateBootstrapRepoFailed:

    def __init__(self, ident, detail=""):
        self.identifier = ident
        self.details = detail
        self.type = "CreateBootstrapRepoFailed"

    def store(self):
        if self.type in getNotificationsTypeDisabled():
            return

        i = rhnSQL.prepare("""
          SELECT users.id
            FROM (
                  SELECT wc.id,
                         (CASE WHEN (SELECT 1
                                       FROM rhnwebcontactchangelog wccl
                                       JOIN (
                                             SELECT web_contact_id, max(id) current_state
                                               FROM rhnwebcontactchangelog uch
                                           GROUP BY web_contact_id
                                            ) X ON X.current_state = wccl.id
                                       JOIN rhnwebcontactchangestate wcsc ON wccl.change_state_id = wcsc.id
                                      WHERE wcsc.label = 'disabled'
                                        AND X.web_contact_id = wc.id) = 1
                               THEN 1
                               ELSE 0
                           END) disabled
                    FROM rhnusergrouptype ugt
                    JOIN rhnusergroup ug ON ug.group_type = ugt.id
                    JOIN rhnusergroupmembers ugm ON ugm.user_group_id = ug.id
                    JOIN web_contact wc ON wc.id = ugm.user_id
                   WHERE ugt.label = 'satellite_admin'
                 ) users
           WHERE users.disabled = 0;
        """)
        i.execute()
        affected_user = i.fetchall_dict() or None
        if not affected_user:
            return

        mid = rhnSQL.Sequence('suse_notif_message_id_seq')()

        h = rhnSQL.prepare("""
          INSERT INTO suseNotificationMessage (id, type, data)
          VALUES (:mid, :type, :data)
        """)
        h.execute(mid=mid, type=self.type, data=json.dumps(self.__dict__))

        j = rhnSQL.prepare("""
          INSERT INTO suseUserNotification (id, user_id, message_id)
          VALUES (sequence_nextval('suse_user_notif_id_seq'), :uid, :mid)
        """)

        for user in affected_user:
            j.execute(uid=user['id'], mid=mid)
        rhnSQL.commit()
