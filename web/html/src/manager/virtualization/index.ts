export default {
  "virtualization/guests/list/guests-list": () => import("./guests/list/guests-list.renderer"),
  "virtualization/guests/edit/guests-edit": () => import("./guests/edit/guests-edit.renderer"),
  "virtualization/guests/create/guests-create": () => import("./guests/create/guests-create.renderer"),
  "virtualization/guests/console/guests-console": () => import("./guests/console/guests-console.renderer"),
  "virtualization/pools/list/pools-list": () => import("./pools/list/pools-list.renderer"),
  "virtualization/pools/create/pools-create": () => import("./pools/create/pools-create.renderer"),
  "virtualization/pools/edit/pools-edit": () => import("./pools/edit/pools-edit.renderer"),
  "virtualization/nets/list/nets-list": () => import("./nets/list/nets-list.renderer"),
  "virtualization/nets/create/nets-create": () => import("./nets/create/nets-create.renderer"),
  "virtualization/nets/edit/nets-edit": () => import("./nets/edit/nets-edit.renderer"),
};
