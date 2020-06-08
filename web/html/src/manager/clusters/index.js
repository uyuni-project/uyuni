export default {
  'clusters/list': () => import('./list-clusters/list-clusters.renderer.js'),
  'clusters/cluster': () => import('./cluster/cluster.renderer.js'),
  'clusters/add': () => import('./add-cluster/add-cluster.renderer.js'),
  'clusters/join-node': () => import('./join-cluster/join-cluster.renderer.js'),
  'clusters/remove-node': () => import('./remove-node/remove-node.renderer.js'),
  'clusters/upgrade-cluster': () => import('./upgrade-cluster/upgrade-cluster.renderer.js')
}
