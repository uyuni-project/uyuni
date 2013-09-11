<?xml version="1.0" encoding="UTF-8"?>
<pxt-passthrough>

  <pxt-use class="Sniglets::Channel" />
  <pxt-use class="Sniglets::Navi" />

<rhn-channel-details>
  <!-- Removed args: help-url="channel-mgmt-Custom_Channel_and_Package_Management-Managed_Software_Channel_Details.jsp" help-guide="channel-mgmt" -->
  <rhn-toolbar base="h1" img="/img/rhn-icon-channels.gif" deletion-type="software channel" deletion-url="/rhn/channels/manage/Delete.do?cid={channel_id}" deletion-acl="user_role(channel_admin); formvar_exists(cid)">
    Software Channel: {channel_name}
  </rhn-toolbar>
</rhn-channel-details>

    <rhn-navi-nav prefix="manage_channel" depth="0" file="/nav/manage_channel.xml" style="contentnav" />

</pxt-passthrough>
