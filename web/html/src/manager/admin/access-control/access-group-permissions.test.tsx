import { Utils } from "utils/functions";
import Network from "utils/network";
import { render, screen, within } from "utils/test-utils";

import AccessGroupPermissions from "./access-group-permissions";

describe("AccessGroupPermissions", () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test("renders a parent permission as indeterminate when only one child is selected", async () => {
    jest.spyOn(Network, "get").mockReturnValue(
      Utils.cancelable(
        Promise.resolve({
          namespaces: [
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
          ],
        })
      )
    );

    render(
      <AccessGroupPermissions
        state={{
          id: 1,
          name: "Test group",
          description: "",
          orgId: 1,
          orgName: "Test organization",
          accessGroups: [],
          permissions: {
            "systems.details": {
              id: 1,
              namespace: "systems.details",
              description: "View system details",
              accessMode: "RW",
              view: true,
              modify: false,
            },
          },
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
    expect(viewCheckbox.indeterminate).toBe(true);
  });
});
