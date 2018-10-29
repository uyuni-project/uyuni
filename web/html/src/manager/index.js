/* eslint-disable */
const pages = {
  'errors/not-found': './manager/errors/not-found.js',
  'visualization/hierarchy': './manager/visualization/hierarchy.js',
  'bootstrap-minions': './manager/bootstrap-minions.js',
  'cveaudit': './manager/cveaudit.js',
  'delete-system': './manager/delete-system.js',
  'delete-system-confirm': './manager/delete-system-confirm.js',
  'duplicate-systems-compare-delete': './manager/duplicate-systems-compare-delete.js',
  'group-config-channels': './manager/group-config-channels.js',
  'group-formula': './manager/group-formula.js',
  'group-formula-selection': './manager/group-formula-selection.js',
  'highstate': './manager/highstate.js',
  'image-build': './manager/image-build.js',
  'image-import': './manager/image-import.js',
  'image-profile-edit': './manager/image-profile-edit.js',
  'image-profiles': './manager/image-profiles.js',
  'image-store-edit': './manager/image-store-edit.js',
  'image-stores': './manager/image-stores.js',
  'image-view': './manager/image-view.js',
  'key-management': './manager/key-management.js',
  'menu': './manager/menu.js',
  'minion-config-channels': './manager/minion-config-channels.js',
  'minion-formula': './manager/minion-formula.js',
  'minion-formula-selection': './manager/minion-formula-selection.js',
  'notifications/notification-messages': './manager/notifications/notification-messages.renderer.js',
  'notifications/notifications': './manager/notifications/notifications.js',
  'org-config-channels': './manager/org-config-channels.js',
  'org-formula-catalog': './manager/org-formula-catalog.js',
  'org-formula-details': './manager/org-formula-details.js',
  'package-states': './manager/package-states.js',
  'products': './manager/products.js',
  'products-scc-dialog': './manager/products-scc-dialog.js',
  'remote-commands': './manager/remote-commands.js',
  'ssm-subscribe-channels': './manager/ssm-subscribe-channels.js',
  'subscribe-channels': './manager/subscribe-channels.renderer.js',
  'subscription-matching': './manager/subscription-matching.js',
  'taskotop': './manager/taskotop.js',
  'virtualhostmanager': './manager/virtualhostmanager.js',
  'virtualization/guests/list/guests-list.renderer': './manager/virtualization/guests/list/guests-list.renderer.js',
  'virtualization/guests/edit/guests-edit.renderer': './manager/virtualization/guests/edit/guests-edit.renderer.js',
  'activation-key/activation-key-channels.renderer': './manager/activation-key/activation-key-channels.renderer.js',
}

Object.keys(pages).forEach((key) => {
  pages[key] = ["@babel/polyfill/dist/polyfill", "core-js/shim", "regenerator-runtime/runtime", pages[key]];
  pages["javascript/manager/" + key] = pages[key];
  delete pages[key]
});

module.exports = {
  pages
}
