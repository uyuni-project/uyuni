type IconSize = "sm" | "md" | "lg" | "xl" | "2xl";
type IconStatus = "danger" | "warning" | "success" | "info" | "muted";

type BaseProps = {
  /** Any additional css classes for the icon, appended to the ones the icon itself resolves to */
  className?: string;
  /** Add ID to icon */
  id?: string;
  /** Add Key attribute to icon */
  key?: string;
  /** Size of icon. */
  size?: IconSize;
  /** Add status color of the icon */
  status?: IconStatus;
  /** Tooltip text, also set as the `title` attribute */
  title?: string;
  /** Tooltip placement, only has an effect together with `title` */
  tooltipPlacement?: "top" | "right" | "bottom" | "left";
   /** Tooltip style, only has an effect together with `title` */
  tooltipWide?: boolean;
  "aria-hidden"?: boolean;
};

/** Pass either a semantic `type` from the map below, or raw Font Awesome classes through `icon`, never both */
type Props = BaseProps &
  (
    | {
        /** Semantic icon name, see the `icons` map below */
        type: string;
        icon?: never;
      }
    | {
        /** Font Awesome classes, e.g. `"fa-question-circle"`. Use this when no `type` fits. */
        icon: string;
        type?: never;
      }
  );

// See https://fontawesome.com/v4/icons/
function IconTag(props: Props) {
  const icons = {
    "action-failed": "fa-times-circle-o",
    "action-ok": "fa-check-circle-o",
    "action-pending": "fa-clock-o", //icon-size-lg
    "action-running": "fa-exchange", //icon-size-lg
    "errata-bugfix": "fa-bug", //icon-size-lg
    "errata-enhance": "spacewalk-icon-enhancement", //icon-size-lg 
    "errata-security": "fa-shield", //icon-size-lg
    "errata-reboot": "fa-refresh", //icon-size-lg
    "errata-restart": "fa-archive", //icon-size-lg
    "external-link": "fa-external-link",
    "event-type-errata": "spacewalk-icon-patches",
    "event-type-package": "spacewalk-icon-packages",
    "event-type-preferences": "fa-cog",
    "event-type-system": "fa-desktop",
    "file-directory": "fa-folder-open-o",
    "file-file": "fa-file-text-o",
    "file-symlink": "spacewalk-icon-listicon-cfg-symlink",
    "header-action": "fa-clock-o",
    "header-activation-key": "fa-key",
    "header-calendar": "fa-calendar",
    "header-chain": "fa-chain",
    "header-channel": "spacewalk-icon-software-channels",
    "header-channel-configuration": "spacewalk-icon-software-channel-management",
    "header-channel-mapping": "fa-retweet",
    "header-chat": "fa-comment",
    "header-clock": "fa-clock-o",
    "header-config-system": "spacewalk-icon-config-system",
    "header-configuration": "spacewalk-icon-manage-configuration-files",
    "header-crash": "spacewalk-icon-bug-ex",
    "header-errata": "spacewalk-icon-patches",
    "header-errata-add": "spacewalk-icon-patch-install",
    "header-errata-del": "spacewalk-icon-patch-remove",
    "header-errata-set": "spacewalk-icon-patch-set",
    "header-errata-set-add": "pacewalk-icon-patchset-install",
    "header-event-history": "fa-suitcase",
    "header-file": "fa-file-text-o",
    "header-folder": "fa-folder-open-o",
    "header-globe": "fa-globe",
    "header-help": "fa-question-circle icon-size-md",
    "header-info": "fa-info-circle",
    "header-kickstart": "fa-rocket",
    "header-list": "fa-list",
    "header-multiorg-big": "fa-sitemap",
    "header-note": "spacewalk-icon-note-pin",
    "header-organisation": "fa-group",
    "header-package": "spacewalk-icon-packages",
    "header-package-add": "spacewalk-icon-package-add",
    "header-package-del": "spacewalk-icon-package-delete",
    "header-package-extra": "spacewalk-icon-package-extra",
    "header-package-upgrade": "spacewalk-icon-package-upgrade",
    "header-power": "fa-power-off",
    "header-preferences": "fa-cogs",
    "header-proxy": "spacewalk-icon-proxy",
    "header-refresh": "fa-refresh",
    "header-reloading": "fa-refresh fa-spin",
    "header-sandbox": "spacewalk-icon-sandbox",
    "header-schedule": "spacewalk-icon-schedule",
    "header-search": "fa-search",
    "header-signout": "fa-sign-out",
    "header-sitemap": "fa-sitemap",
    "header-snapshot": "fa-camera",
    "header-snapshot-rollback": "spacewalk-icon-snapshot-rollback",
    "header-subscriptions-big": "fa-list-alt",
    "header-symlink": "spacewalk-icon-listicon-cfg-symlink",
    "header-system": "fa-desktop",
    "header-system-groups": "spacewalk-icon-system-groups",
    "header-system-physical": "fa-desktop",
    "header-system-virt-guest": "spacewalk-icon-virtual-guest",
    "header-system-virt-host": "spacewalk-icon-virtual-host",
    "header-taskomatic": "fa-tachometer",
    "header-user": "fa-user",
    "header-users-big": "fa-group",
    "header-mgr-server": "spacewalk-icon-suse-manager",
    "item-add": "fa-plus",
    "item-clone": "fa-files-o",
    "item-del": "fa-trash-o",
    "item-disabled": "fa-circle-o", //text-muted
    "item-download": "fa-download",
    "item-download-csv": "spacewalk-icon-download-csv",
    "item-edit": "fa-edit",
    "item-enabled": "fa-check", //text-success
    "item-enabled-pending": "fa-hand-o-right", //text-success
    "item-import": "fa-level-down",
    "item-proxy-convert": "fa-arrow-up",
    "item-search": "fa-eye",
    "item-ssm-add": "fa-plus-circle",
    "item-ssm-del": "fa-minus-circle",
    "item-upload": "fa-upload",
    "item-order": "fa-sort",
    "item-error": "fa-times", //text-danger
    "item-error-pending": "fa-hand-o-right", //text-danger
    "nav-bullet": "fa-caret-right",
    "nav-page-first": "fa-angle-double-left",
    "nav-page-last": "fa-angle-double-right",
    "nav-page-next": "fa-angle-right",
    "nav-page-prev": "fa-angle-left",
    "nav-right": "fa-arrow-right",
    "nav-up": "fa-caret-up",
    "repo-sync": "fa-refresh",
    "repo-schedule-sync": "fa-calendar",
    "scap-nochange": "fa-dot-circle-o", //icon-size-lg text-info
    "setup-wizard-creds-edit": "fa-pencil",
    "setup-wizard-creds-failed": "fa-times-circle-o", //text-danger
    "setup-wizard-creds-make-primary": "fa-star-o", //text-starred"
    "setup-wizard-creds-primary": "fa-star text-starred", //text-starred"
    "setup-wizard-creds-subscriptions": "fa-th-list",
    "setup-wizard-creds-verified": "fa-check-square", //text-success
    "sort-down": "fa-arrow-circle-down",
    "sort-up": "fa-arrow-circle-up",
    "spinner": "fa-spinner fa-spin",
    "spacewalk-icon-salt": "spacewalk-icon-salt",
    "system-state": "spacewalk-icon-salt-add",
    "system-bare-metal-legend": "spacewalk-icon-bare-metal", //icon-size-lg
    "system-bare-metal": "spacewalk-icon-bare-metal",
    "system-crit": "fa-exclamation-circle", //icon-size-lg text-danger
    "system-kickstarting": "fa-rocket", //icon-size-lg
    "system-locked": "fa-lock", //icon-size-lg
    "system-ok": "fa-check-circle", //icon-size-lg text-success
    "system-physical": "fa-desktop", //icon-size-lg
    "system-reboot": "fa-refresh", // none
    "system-unentitled": "fa-times-circle", //icon-size-lg
    "system-unknown": "fa-question-circle", //icon-size-lg
    "system-virt-guest": "spacewalk-icon-virtual-guest", //icon-size-lg 
    "system-virt-host": "spacewalk-icon-virtual-host", //icon-size-lg 
    "system-warn": "fa-exclamation-triangle", //icon-size-lg text-warning
    "experimental": "fa-flask",
  };
  const ariaHidden = props["aria-hidden"] ?? true;
  const tooltipProps = props.title
    ? {
        "data-bs-toggle": "tooltip",
        "data-bs-placement": props.tooltipPlacement,
        "data-bs-custom-class": props.tooltipWide ? "wide-tooltip" : undefined,
      }
    : {};

  const sizeClass = props.size ? `icon-size-${props.size}` : undefined;
  const statusClass = props.status ? `text-${props.status}` : undefined;
  // Add the shared `fa` base class to Font Awesome and Spacewalk icons.
  const classNames = ["fa", props.type ? icons[props.type] : props.icon, props.className, sizeClass, statusClass]
    .filter(Boolean)
    .join(" ")
    .split(" ")
    // Callers that do spell out `fa` themselves shouldn't end up with it twice
    .filter((name, index, all) => name !== "" && all.indexOf(name) === index);

  return <i id={props.id} key={props.key} className={classNames.join(" ")} {...tooltipProps} title={props.title} aria-hidden={ariaHidden}></i>;
}

export { IconTag };
