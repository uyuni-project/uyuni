import { render, server, click, waitFor } from "utils/test-utils";
import AccordionPathContent from "./accordion-path-content";
import { createNewAnsiblePath } from "./ansible-path-type";

const API_INVENTORY_DETAILS = "/rhn/manager/api/systems/details/ansible/introspect-inventory/2";

describe("AccordionPathContent summary", () => {
  test("Render accordion collapsed element", async () => {
    const path = createNewAnsiblePath({id: 1, minionServerId: 1000, path: "/srv/ansible/playbooks", type: "playbook"});
    const { getByText } = render(<AccordionPathContent path={path} onSelectPlaybook={() => {}} />);
    getByText("/srv/ansible/playbooks");
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
          "my-ansible-managed-client-4.tf.local"
        ],
        knownSystems: [
          {
            id: 10000,
            name: "minion.tf.local"
          }
        ],
        dump: "_meta:\n  hostvars:\n    minion.tf.local: {ansible_ssh_private_key_file: /etc/ansible/my_ansible_private_key}\n    my-ansible-managed-client-1.tf.local: {ansible_ssh_private_key_file: /etc/ansible/my_ansible_private_key}\n    my-ansible-managed-client-2.tf.local: {ansible_ssh_private_key_file: /etc/ansible/my_ansible_private_key}\n    my-ansible-managed-client-3.tf.local: {ansible_ssh_private_key_file: /etc/ansible/my_ansible_private_key}\n    my-ansible-managed-client-4.tf.local: {ansible_ssh_private_key_file: /etc/ansible/some_ssh_key}\nall:\n  children: [mygroup1, mygroup2, ungrouped]\nmygroup1:\n  hosts: [my-ansible-managed-client-1.tf.local, my-ansible-managed-client-2.tf.local]\nmygroup2:\n  hosts: [my-ansible-managed-client-3.tf.local]\nungrouped:\n  hosts: [minion.tf.local, my-ansible-managed-client-4.tf.local]\n"
      }
    };
    // server loading inventory details on click
    server.mockGetJson(API_INVENTORY_DETAILS, data);
    
    const path = createNewAnsiblePath({id: 2, minionServerId: 1000, path: "/etc/ansible/hosts", type: "inventory"}); // component props
    const { getByText, getByRole } = render(<AccordionPathContent path={path} onSelectPlaybook={() => {}} />); // load the component at initial state
    
    const pathAccordionButtonToOpen = getByRole("button", { name: path.path }) as HTMLButtonElement; // get the clickable element
    click(pathAccordionButtonToOpen);
    
    getByText("Loading content..");

    // wait until the render loads and changes, then check for content
    await waitFor(() => {
      getByText("Registered Systems:");
      getByText("Unknown Hostnames:");
      getByText("my-ansible-managed-client-1.tf.local");
      getByText("minion.tf.local");
    });
  });
});
