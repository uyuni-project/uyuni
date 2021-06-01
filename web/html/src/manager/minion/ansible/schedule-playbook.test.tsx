import { render, server, screen } from "utils/test-utils";
import { rest } from 'msw';
import selectEvent from 'react-select-event';
import { JsonResult } from "utils/network";
import { AnsiblePath } from "./ansible-path-type";
import SchedulePlaybook from "./schedule-playbook";
import { PlaybookDetails } from "./accordion-path-content";

const API_INVENTORIES = "/rhn/manager/api/systems/details/ansible/paths/inventory/1000";
const API_PLAYBOOK_CONTENT = "/rhn/manager/api/systems/details/ansible/paths/playbook-contents";

// TODO: Handle "ReferenceError: handleSst is not defined"
window.handleSst = undefined;

describe("Ansible playbooks", () => {
  test("Render playbook content", async () => {
    // Mock inventory list API endpoint
    const response: JsonResult<AnsiblePath[]> = {
      success: true,
      messages: [],
      data: [
        {
          id: 1,
          minionServerId: 1000,
          type: "inventory",
          path: "/my/inventory"
        },
        {
          id: 2,
          minionServerId: 1000,
          type: "inventory",
          path: "/my/second/inventory"
        },
      ]
    };
    server.mockGetJson(API_INVENTORIES, response);

    // Mock playbook content API endpoint
    server.use(
      rest.post(API_PLAYBOOK_CONTENT, (req, res, ctx) =>
        res(ctx.json({
          success: true,
          messages: [],
          data: "My playbook content string"
        }))
      )
    );

    const playbook: PlaybookDetails = {
      path: {
        id: 2,
        minionServerId: 1000,
        type: "playbook",
        path: "my/playbook"
      },
      fullPath: "/playbooks/my/playbook",
      customInventory: "/playbooks/my/inventory",
      name: "my/playbook"
    }

    render(<SchedulePlaybook playbook={playbook} onBack={() => { }} />);
    // TODO: Change after t() mockup is fixed
    await screen.findByText("Playbook '{0}'");

    // Inventory combobox
    const inventorySelect = screen.getByLabelText("Inventory Path:");
    selectEvent.openMenu(inventorySelect);
    screen.getByText("/my/inventory");
    screen.getByText("/my/second/inventory");
    screen.getByText("/playbooks/my/inventory");

    // Playbook contents (mocked up by AceEditor)
    screen.getByText("My AceEditor mockup")
  });
});
