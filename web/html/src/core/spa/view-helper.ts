/**
 * A list of updated page pathnames, e.g. `"/rhn/manager/foo/bar"`
 * NB! This must be in sync with java/code/src/com/suse/manager/webui/utils/ViewHelper.java
 */
const BOOTSTRAP_READY_PAGES: string[] = [
  "/rhn/YourRhn.do",
  "/rhn/account/UserPreferences.do",
  "/rhn/account/UserDetails.do",
  "/rhn/account/ChangeEmail.do",
  "/rhn/account/AccountDeactivation.do",
  "/rhn/account/Addresses.do",
  "/rhn/account/EditAddress.do",
  "/rhn/multiorg/OrgConfigDetails.do",
  "/rhn/manager/notification-messages",
  "rhn/channels/software/Search.do",
  "/rhn/systems/details/kickstart/PowerManagement.do",
  "/rhn/systems/ssm/provisioning/PowerManagementOperations.do",
  "/rhn/channels/ChannelDetail.do",
  "/rhn/software/packages/Details.do",
];

export const onEndNavigate = () => {
  const pathname = window.location.pathname;
  if (BOOTSTRAP_READY_PAGES.includes(pathname)) {
    document.body.className = document.body.className.replace("old-theme", "new-theme");
  } else {
    document.body.className = document.body.className.replace("new-theme", "old-theme");
  }
};
