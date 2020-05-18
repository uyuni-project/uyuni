/* eslint-disable */
'use strict';

const React = require("react");

type IconProps = {
  /** Name of the icon key */
  type?: string,
  /** whether to show borders around the component */
  withBorders?: bool,
  /** Text to display as a tooltip */
  title?: string,
  /** whether to choose the size of the icon */
  size?: string,
  /** additional CSS classes like text colors etc */
  styleClass?: string,
}

const _SIZES = {
  XS: "12px",
  S: "18px",
  M: "24px",
  L: "30px",
  XL: "36px",
  XXL: "48px",
};

export function Icon({ withBorders, type, title, size, styleClass } : IconProps) {

  // https://icons.eosdesignsystem.com/cheatsheet
  const icons = {
    // self-explaining key for the desired picture
    "calendar": "event_note",
    "clock": "access_time",
    "arrow-right": "arrow_forward",

    // self-explaining key for the topic usage
    "action-failed": "cancel",
    "action-ok": "check_circle",
    "action-pending": "access_time",
    "action-running": "swap_horiz",
    "errata-bugfix": "critical_bug",
    "errata-enhance": "camera_enhance",
    "errata-security": "security",
    "errata-reboot": "sync",
    "errata-restart": "sync_problem",
    "event-type-errata": "healing",
    "event-type-package": "package",
    "event-type-preferences": "settings",
    "event-type-system": "desktop_windows",
    "file-directory": "folder", // folder_open
    "file-file": "description", // insert_drive_file
    "file-symlink": "symlink",
    "header-action": "access_time",
    "header-activation-key": "vpn_key",
    "header-calendar": "event_note", // date_range
    "header-chain": "action_chains",
    "header-channel": "tune", // settings_input_component
    "header-channel-configuration": "tune" , // settings_input_component, settings
    "header-channel-mapping": "sync",
    "header-chat": "chat", //chat_bubble
    "header-clock": "access_time",
    "header-config-system": "settings",
    "header-configuration": "configuration_file",
    "header-crash": "critical_bug",
    "header-errata": "healing",
    "header-event-history": "work", // card_travel, history
    "header-file": "description", // insert_drive_file
    "header-folder": "folder", // folder_open
    "header-help": "help", // help_outline
    "header-info": "info", // info_outline
    "header-kickstart": "autoinstallation",
    "header-list": "list", // view_list, format_list_bulleted
    "header-multiorg-big": "organization", // <-- size = XL
    "header-note": "receipt", // message, pin
    "header-organisation": "people_outline", // group
    "header-package": "packages",
    "header-package-add": "package",
    "header-package-del": "package",
    "header-package-extra": "package",
    "header-package-upgrade": "package_upgrade",
    "header-power": "power_settings_new",
    "header-preferences": "settings",
    "header-proxy": "proxy",
    "header-refresh": "refresh",
    "header-sandbox": "sandbox",
    "header-schedule": "schedule",
    "header-search": "search",
    "header-signout": "exit_to_app",
    "header-sitemap": "organization",
    "header-snapshot": "camera_alt",
    "header-snapshot-rollback": "snapshot_rollback",
    "header-symlink": "symlink",
    "header-system": "desktop_windows",
    "header-system-groups": "system_group",
    "header-system-physical": "desktop_windows",
    "header-system-virt-guest": "virtual_guest",
    "header-system-virt-host": "node",
    "header-taskomatic": "organisms", // background_tasks
    "header-user": "person_outline", // account_box, account_circle
    "header-users-big": "people_outline", // <-- size = XL // group
    "item-add": "add",
    "item-clone": "content_copy",
    "item-del": "delete", // delete_forever
    "item-disabled": "panorama_fish_eye", // <-- text-muted
    "item-download": "file_download",
    "item-download-csv": "csv_file",
    "item-edit": "edit",
    "item-enabled": "check", // <-- text-success
    "item-enabled-pending": "keyboard_tab", // <-- text-success
    "item-import": "low_priority",
    "item-search": "remove_red_eye",
    "item-ssm-add": "add_circle", // add_circle_outline
    "item-ssm-del": "remove_circle", // remove_circle_outline
    "item-upload": "file_upload",
    "item-order": "unfold_more",
    "item-error": "close", // <-- text-danger
    "item-error-pending": "keyboard_tab", // <-- text-danger
    "nav-bullet": "play_arrow", // keyboard_arrow_right
    "nav-page-first": "first_page",
    "nav-page-last": "last_page",
    "nav-page-next": "keyboard_arrow_right",
    "nav-page-prev": "keyboard_arrow_left",
    "nav-right": "arrow_forward",
    "nav-up": "arrow_drop_up",
    "repo-sync": "sync",
    "repo-schedule-sync": "event_note", // date_range
    "scap-nochange": "adjust", // <-- text-info
    "setup-wizard-creds-edit": "edit",
    "setup-wizard-creds-failed": "close", // text-danger",
    "setup-wizard-creds-make-primary": "star_border", // <-- text-starred
    "setup-wizard-creds-primary": "star", // <-- text-starred
    "setup-wizard-creds-subscriptions": "view_list",
    "setup-wizard-creds-verified": "check_box", // <-- text-success
    "sort-down": "arrow_downward", // arrow_drop_down_circle
    "sort-up": "arrow_upward",

    "system-state": "fa spacewalk-icon-salt-add",

    "system-bare-metal-legend": "domain",
    "system-bare-metal": "domain", // <-- size = S
    "system-crit": "error_outline", // <-- text-danger // report
    "system-kickstarting": "autoinstallation",
    "system-locked": "lock", // lock_outline
    "system-ok": "check_circle", // <-- text-success
    "system-physical": "desktop_windows",
    "system-reboot": "sync",
    "system-unentitled": "cancel",
    "system-unknown": "help_outline",
    "system-virt-guest": "virtual_guest",
    "system-virt-host": "node",
    "system-warn": "warning", // <-- text-warning
  };

  return (<i style={{fontSize: _SIZES[size.toUpperCase()]}} className={"eos-icons " + styleClass} title={title}>{icons[type]}</i>);
}

Icon.defaultProps = {
  type: undefined,
  withBorders: false,
  title: undefined,
  size: "S",
  className: undefined
}