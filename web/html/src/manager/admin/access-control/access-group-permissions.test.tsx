import { Utils } from "utils/functions";
import Network from "utils/network";
import { click, render, screen, within } from "utils/test-utils";

import type { AccessGroupState } from "./access-group";
import AccessGroupPermissions, { type NamespaceItem } from "./access-group-permissions";
import { AccessMode } from "./access-mode";

type Permission = AccessGroupState["permissions"][string];
type PermissionType = "view" | "modify";

const buildNamespace = (overrides: Partial<NamespaceItem> = {}): NamespaceItem => ({
  namespace: "test.namespace",
  name: "Test namespace",
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

const namespaces = [
  buildNamespace({
    namespace: "systems",
    name: "Systems",
    description: "System permissions",
    children: [
      buildNamespace({
        namespace: "systems.details",
        name: "System details",
        description: "View system details",
      }),
      buildNamespace({
        namespace: "systems.history",
        name: "System history",
        description: "View system history",
      }),
    ],
  }),
];

const buildSelectedPermission = (namespace: string, type: PermissionType): Permission =>
  buildPermission({ namespace, [type]: true });

const renderPermissions = async (permissions: AccessGroupState["permissions"], onChange = jest.fn()) => {
  jest.spyOn(Network, "get").mockReturnValue(Utils.cancelable(Promise.resolve({ namespaces })));

  render(
    <AccessGroupPermissions
      state={{
        id: 1,
        name: "Test group",
        description: "",
        orgId: 1,
        orgName: "Test organization",
        accessGroups: [],
        permissions,
        users: [],
        errors: {},
        permissionsModified: false,
      }}
      onChange={onChange}
      errors={{}}
    />
  );

  const parentRow = (await screen.findByText("Systems")).closest("tr");
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

    expect(checkboxes.view.className).toContain("form-check-input");
    expect(checkboxes.modify.className).toContain("form-check-input");
  });

  test.each<PermissionType>(["view", "modify"])(
    "renders the parent %s permission as indeterminate when only one child is selected",
    async (type) => {
      const checkboxes = await renderPermissions({
        "systems.details": buildSelectedPermission("systems.details", type),
      });

      expect(checkboxes[type].checked).toBe(false);
      expect(checkboxes[type].indeterminate).toBe(true);
    }
  );

  test.each<PermissionType>(["view", "modify"])(
    "renders the parent %s permission as checked when all children are selected",
    async (type) => {
      const checkboxes = await renderPermissions({
        "systems.details": buildSelectedPermission("systems.details", type),
        "systems.history": buildSelectedPermission("systems.history", type),
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
      "systems.details": expect.objectContaining({ namespace: "systems.details", view: true }),
      "systems.history": expect.objectContaining({ namespace: "systems.history", view: true }),
    });
  });
});
