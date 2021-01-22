export default {
  'clusters/list': () => import('./list-clusters/list-clusters.renderer'),
  'clusters/cluster': () => import('./cluster/cluster.renderer'),
  'clusters/add': () => import('./add-cluster/add-cluster.renderer'),
  'clusters/join-node': () => import('./join-cluster/join-cluster.renderer'),
  'clusters/remove-node': () => import('./remove-node/remove-node.renderer'),
  'clusters/upgrade-cluster': () => import('./upgrade-cluster/upgrade-cluster.renderer')
}
