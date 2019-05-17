/* eslint-disable */

/*
How to add a new route:
All the routes exported on the files '<*folder_name*>/index.js' will be automatically registered.
Check the file content-management/index.js for an example
*/

const { readdirSync, statSync } = require('fs')
const { join } = require('path')

const oldPagesEntries = {
  'delete-system': './manager/delete-system.js',
  'delete-system-confirm': './manager/delete-system-confirm.js',
  'duplicate-systems-compare-delete': './manager/duplicate-systems-compare-delete.js',
  'group-config-channels': './manager/group-config-channels.js',
  'group-formula': './manager/group-formula.js',
  'group-formula-selection': './manager/group-formula-selection.js',
  'highstate': './manager/highstate.js',
  'key-management': './manager/key-management.js',
  'minion-config-channels': './manager/minion-config-channels.js',
  'minion-formula': './manager/minion-formula.js',
  'minion-formula-selection': './manager/minion-formula-selection.js',
  'notifications/notification-messages': './manager/notifications/notification-messages.renderer.js',
  'notifications/notifications': './manager/notifications/notifications.js',
  'org-config-channels': './manager/org-config-channels.js',
  'org-formula-catalog': './manager/org-formula-catalog.js',
  'org-formula-details': './manager/org-formula-details.js',
  'package-states': './manager/package-states.js',
  'remote-commands': './manager/remote-commands.js',
  'ssm-subscribe-channels': './manager/channels/ssm-subscribe-channels/ssm-subscribe-channels.js',
  'subscribe-channels': './manager/channels/subscribe-channels/subscribe-channels.renderer.js',
}

const readDirs = p => readdirSync(p).filter(f => statSync(join(p, f)).isDirectory())
const dirs = readDirs("./manager");

const dirsWithRoutes = dirs.filter(dir => {
  try {
    return require(`./${dir}`).entries
  } catch(e){
    return false;
  };
})


const newPagesEntries = dirsWithRoutes.reduce((newRoutes, nextDir) => {
  const nextDirRoutes = require(`./${nextDir}`).entries;
  const nextDirRoutesFormated = nextDirRoutes.reduce((routesFormated, nextRoute) => {
    const formatedKey = `${nextDir}/${nextRoute.split('.').slice(0, -1).join('.')}`;
    const formatedValue = `./manager/${nextDir}/${nextRoute}`;

    return {
      ...routesFormated,
      ...{[formatedKey]: formatedValue}
    }
  }, {});

  return {...newRoutes, ...nextDirRoutesFormated};
}, {});

const allPages = {...oldPagesEntries, ...newPagesEntries};

Object.keys(allPages).forEach((key) => {
  allPages["javascript/manager/" + key] = allPages[key];
  delete allPages[key]
});

module.exports = {
  pages: allPages
}
