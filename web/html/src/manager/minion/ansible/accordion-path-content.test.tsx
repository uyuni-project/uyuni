import { render, server, click, waitFor, screen } from "utils/test-utils";
import AccordionPathContent from "./accordion-path-content";
import { createNewAnsiblePath } from "./ansible-path-type";

const API_INVENTORY_DETAILS = "/rhn/manager/api/systems/details/ansible/introspect-inventory/2";

describe("AccordionPathContent summary", () => {
  test("Render accordion collapsed element", async () => {
    const path = createNewAnsiblePath({
      id: 1,
      minionServerId: 1000,
      path: "/srv/ansible/playbooks",
      type: "playbook",
    });
    render(<AccordionPathContent path={path} onSelectPlaybook={() => {}} />);
    screen.getByText("/srv/ansible/playbooks");
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
    click(pathAccordionButtonToOpen);

    screen.getByText("Loading content..");

    // wait until the render loads and changes, then check for content
    await waitFor(() => {
      screen.getByText("Registered Systems:");
      screen.getByText("Unknown Hostnames:");
      screen.getByText("my-ansible-managed-client-1.tf.local");
      screen.getByText("minion.tf.local");
    });
  });
});
