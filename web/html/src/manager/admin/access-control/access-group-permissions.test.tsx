import { Utils } from "utils/functions";
import Network from "utils/network";
import { click, render, screen, within } from "utils/test-utils";

import type { AccessGroupState } from "./access-group";
import AccessGroupPermissions, { type NamespaceItem } from "./access-group-permissions";
import { type PermissionType, AccessMode } from "./access-mode";

type Permission = AccessGroupState["permissions"][string];
type NamespaceLeaf = Omit<NamespaceItem, "id"> & { id: number };
const permissionTypes: PermissionType[] = ["view", "modify"];

const buildNamespaceBranch = (overrides: Partial<NamespaceItem> = {}): NamespaceItem => ({
  namespace: "test",
  name: "test",
  description: "",
  isAPI: false,
  accessMode: AccessMode.NONE,
  children: [],
  ...overrides,
});

const buildNamespaceLeaf = (overrides: Partial<NamespaceLeaf> = {}): NamespaceLeaf => ({
  id: 1,
  namespace: "test.namespace",
  name: "namespace",
  description: "Test permission",
  isAPI: false,
  accessMode: AccessMode.READ_WRITE,
  children: [],
  ...overrides,
});

const buildPermission = (overrides: Partial<Permission> = {}): Permission => ({
  id: 1,
  namespace: "test.namespace",
  description: "Test permission",
  accessMode: AccessMode.READ_WRITE,
  view: false,
  modify: false,
  ...overrides,
});

const buildAccessGroupState = (overrides: Partial<AccessGroupState> = {}): AccessGroupState => ({
  id: 1,
  name: "Test group",
  description: "",
  orgId: 1,
  orgName: "Test organization",
  accessGroups: [],
  permissions: {},
  users: [],
  errors: {},
  permissionsModified: false,
  ...overrides,
});

const detailsNamespace = buildNamespaceLeaf({
  id: 1,
  namespace: "systems.details",
  name: "details",
  description: "View system details",
});

const historyNamespace = buildNamespaceLeaf({
  id: 2,
  namespace: "systems.history",
  name: "history",
  description: "View system history",
});

const namespaces = [
  buildNamespaceBranch({
    namespace: "systems",
    name: "systems",
    children: [detailsNamespace, historyNamespace],
  }),
];

const buildSelectedPermission = (namespace: NamespaceLeaf, type: PermissionType): Permission =>
  buildPermission({
    id: namespace.id,
    namespace: namespace.namespace,
    description: namespace.description,
    accessMode: namespace.accessMode,
    [type]: true,
  });

const renderPermissions = async (permissions: AccessGroupState["permissions"], onChange = jest.fn()) => {
  jest.spyOn(Network, "get").mockReturnValue(Utils.cancelable(Promise.resolve({ namespaces })));
  const state = buildAccessGroupState({ permissions });

  render(<AccessGroupPermissions state={state} onChange={onChange} errors={state.errors} />);

  const parentRow = (await screen.findByText("systems")).closest("tr");
  expect(parentRow).not.toBeNull();

  const [view, modify] = within(parentRow!).getAllByRole("checkbox") as HTMLInputElement[];
  return { view, modify };
};

describe("AccessGroupPermissions", () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("renders permission checkboxes using the reusable Check component", async () => {
    const checkboxes = await renderPermissions({});

    Object.values(checkboxes).forEach((checkbox) => {
      expect(checkbox.className).toContain("form-check-input");
    });
  });

  test.each<PermissionType>(permissionTypes)(
    "renders the parent %s permission as indeterminate when only one child is selected",
    async (type) => {
      const checkboxes = await renderPermissions({
        [detailsNamespace.namespace]: buildSelectedPermission(detailsNamespace, type),
      });

      expect(checkboxes[type].checked).toBe(false);
      expect(checkboxes[type].indeterminate).toBe(true);
    }
  );

  test.each<PermissionType>(permissionTypes)(
    "renders the parent %s permission as checked when all children are selected",
    async (type) => {
      const checkboxes = await renderPermissions({
        [detailsNamespace.namespace]: buildSelectedPermission(detailsNamespace, type),
        [historyNamespace.namespace]: buildSelectedPermission(historyNamespace, type),
      });

      expect(checkboxes[type].checked).toBe(true);
      expect(checkboxes[type].indeterminate).toBe(false);
    }
  );

  test("selecting a parent permission applies the change to all children", async () => {
    const onChange = jest.fn();
    const checkboxes = await renderPermissions({}, onChange);

    await click(checkboxes.view);

    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith({
      [detailsNamespace.namespace]: expect.objectContaining({ namespace: detailsNamespace.namespace, view: true }),
      [historyNamespace.namespace]: expect.objectContaining({ namespace: historyNamespace.namespace, view: true }),
    });
  });

  test("clearing a parent permission removes the permission from all children", async () => {
    const onChange = jest.fn();
    const checkboxes = await renderPermissions(
      {
        [detailsNamespace.namespace]: buildSelectedPermission(detailsNamespace, "view"),
        [historyNamespace.namespace]: buildSelectedPermission(historyNamespace, "view"),
      },
      onChange
    );

    await click(checkboxes.view);

    expect(onChange).toHaveBeenCalledTimes(1);
    expect(onChange).toHaveBeenCalledWith({
      [detailsNamespace.namespace]: undefined,
      [historyNamespace.namespace]: undefined,
    });
  });
});
