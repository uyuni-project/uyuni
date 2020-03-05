export default {
  'virtualization/guests/list/guests-list': () => import('./guests/list/guests-list.renderer'),
  'virtualization/guests/edit/guests-edit': () => import('./guests/edit/guests-edit.renderer'),
  'virtualization/guests/create/guests-create': () => import('./guests/create/guests-create.renderer'),
  'virtualization/guests/console/guests-console': () => import('./guests/console/guests-console.renderer'),
  'virtualization/pools/list/pools-list': () => import('./pools/list/pools-list.renderer'),
  'virtualization/pools/create/pools-create': () => import('./pools/create/pools-create.renderer'),
}

