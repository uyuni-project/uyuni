/* eslint-disable */

/*
How to add a new route:
All the routes exported on the files '<*folder_name*>/index.js' will be automatically registered.
Check the file content-management/index.js for an example
*/

const { readdirSync, statSync } = require('fs')
const { join } = require('path')

const oldPagesEntries = {
  'notifications/notification-messages': './manager/notifications/notification-messages.renderer.js',
  'notifications/notifications': './manager/notifications/notifications.js',
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


const pagesEntries = dirsWithRoutes.reduce((newRoutes, nextDir) => {
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

const allPages = {...pagesEntries};

Object.keys(allPages).forEach((key) => {
  allPages["javascript/manager/" + key] = allPages[key];
  delete allPages[key]
});

module.exports = {
  pages: allPages
}
