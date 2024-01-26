/**
 * A list of updated page pathnames, e.g. `"/rhn/manager/foo/bar"`
 * NB! This must be in sync with java/code/src/com/suse/manager/webui/utils/ViewHelper.java
 */
const BOOTSTRAP_READY_PAGES: string[] = [];

export const onEndNavigate = () => {
  const pathname = window.location.pathname;
  if (BOOTSTRAP_READY_PAGES.includes(pathname)) {
    document.body.className = document.body.className.replace("old-theme", "new-theme");
  } else {
    document.body.className = document.body.className.replace("new-theme", "old-theme");
  }
};
