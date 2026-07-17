import { click, render, screen, server } from "utils/test-utils";

import AccordionPathContent from "./accordion-path-content";
import { createNewAnsiblePath } from "./ansible-path-type";

const API_INVENTORY_DETAILS = "/rhn/manager/api/systems/details/ansible/introspect-inventory/2";

describe("AccordionPathContent summary", () => {
  test("Render accordion collapsed element", () => {
    const path = createNewAnsiblePath({
      id: 1,
      minionServerId: 1000,
      path: "/srv/ansible/playbooks",
      type: "playbook",
    });
    render(<AccordionPathContent path={path} onSelectPlaybook={() => {}} />);
    expect(screen.getByText("/srv/ansible/playbooks")).toBeDefined();
  });

  test("Open accordion and load element details", async () => {
    // INVENTORY_DETAILS_DATA_MOCK
    const data = {
      success: true,
      data: {
        unknownSystems: [
          "my-ansible-managed-client-1.tf.local",
          "my-ansible-managed-client-2.tf.local",
          "my-ansible-managed-client-3.tf.local",
          "my-ansible-managed-client-4.tf.local",
        ],
        knownSystems: [
          {
            id: 10000,
            name: "minion.tf.local",
          },
        ],
        dump: "The Inventory dump content for ACE Editor",
      },
    };
    // server loading inventory details on click
    server.mockGetJson(API_INVENTORY_DETAILS, data);

    const path = createNewAnsiblePath({ id: 2, minionServerId: 1000, path: "/etc/ansible/hosts", type: "inventory" }); // component props
    render(<AccordionPathContent path={path} onSelectPlaybook={() => {}} />); // load the component at initial state

    const pathAccordionButtonToOpen = screen.getByRole("button", { name: path.path }) as HTMLButtonElement; // get the clickable element
    await click(pathAccordionButtonToOpen);

    expect(screen.getByText("Loading content..")).toBeDefined();

    // wait until the render loads and changes, then check for content
    expect(await screen.findByText("Registered Systems:")).toBeDefined();
    expect(await screen.findByText("Unknown Hostnames:")).toBeDefined();
    expect(await screen.findByText("my-ansible-managed-client-1.tf.local", { exact: false })).toBeDefined();
    expect(await screen.findByText("minion.tf.local")).toBeDefined();
  });
});
