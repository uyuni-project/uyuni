export default {
  'admin/config/monitoring': () => import('./config/monitoring.renderer'),
  'admin/setup/products/products': () => import('./setup/products/products'),
  'admin/task-engine-status/taskotop': () => import('./task-engine-status/taskotop')
}
