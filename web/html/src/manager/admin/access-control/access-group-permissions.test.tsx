import { Utils } from "utils/functions";
import Network from "utils/network";
import { render, screen, within } from "utils/test-utils";

import type { AccessGroupState } from "./access-group";
import AccessGroupPermissions from "./access-group-permissions";

const namespaces = [
  {
    namespace: "systems",
    name: "Systems",
    description: "System permissions",
    isAPI: false,
    accessMode: "RW",
    children: [
      {
        id: 1,
        namespace: "systems.details",
        name: "System details",
        description: "View system details",
        isAPI: false,
        accessMode: "RW",
        children: [],
      },
      {
        id: 2,
        namespace: "systems.history",
        name: "System history",
        description: "View system history",
        isAPI: false,
        accessMode: "RW",
        children: [],
      },
    ],
  },
];

const detailsPermission = {
  id: 1,
  namespace: "systems.details",
  description: "View system details",
  accessMode: "RW",
  view: true,
  modify: false,
};

const historyPermission = {
  id: 2,
  namespace: "systems.history",
  description: "View system history",
  accessMode: "RW",
  view: true,
  modify: false,
};

const renderPermissions = async (permissions: AccessGroupState["permissions"]) => {
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
      onChange={jest.fn()}
      errors={{}}
    />
  );

  const parentRow = (await screen.findByText("Systems")).closest("tr");
  expect(parentRow).not.toBeNull();

  const [viewCheckbox] = within(parentRow!).getAllByRole("checkbox") as HTMLInputElement[];
  return viewCheckbox;
};

describe("AccessGroupPermissions", () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("renders a parent permission as indeterminate when only one child is selected", async () => {
    const viewCheckbox = await renderPermissions({ "systems.details": detailsPermission });

    expect(viewCheckbox.indeterminate).toBe(true);
  });

  test("renders a parent permission as checked when all children are selected", async () => {
    const viewCheckbox = await renderPermissions({
      "systems.details": detailsPermission,
      "systems.history": historyPermission,
    });

    expect(viewCheckbox.checked).toBe(true);
    expect(viewCheckbox.indeterminate).toBe(false);
  });
});
