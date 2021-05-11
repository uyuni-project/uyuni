import { render, server, waitFor } from "utils/test-utils";
import { AnsibleControlNode } from "./ansible-control-node";

const API_PATH_LIST = "/rhn/manager/api/systems/details/ansible/paths/1000";

describe("Ansible control node path configuration", () => {
  test("Path list", async () => {
    // PATH_LIST_DATA_MOCK
    const data = {
      "success": true,
      "data": [
        {
          "id": 1,
          "minionServerId": 1000,
          "type": "playbook",
          "path": "/srv/playbooks"
        },
        {
          "id": 2,
          "minionServerId": 1000,
          "type": "inventory",
          "path": "/srv/playbooks/orion_dummy/hosts"
        }
      ]
    };
    // server loading path list on loading
    server.mockGetJson(API_PATH_LIST, data);
    
    const { getByText } = render(<AnsibleControlNode minionServerId={1000} />); // load the component at initial state
        
    getByText("Loading..");

    // wait until the render loads and changes, then check for content
    await waitFor(() => {
      getByText("Playbook Directories");
      getByText("Inventory Files");
      getByText("Add a Playbook directory");
      getByText("Add an Inventory file");
      getByText("/srv/playbooks");
      getByText("/srv/playbooks/orion_dummy/hosts");
    });
  });
});
