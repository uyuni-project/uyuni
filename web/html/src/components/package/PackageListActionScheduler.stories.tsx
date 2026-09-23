import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { Column } from "components/table/Column";

import { Utils } from "utils/functions";
import Network from "utils/network";

import { PackageListActionScheduler } from "./PackageListActionScheduler";

type PackageFixture = {
  idCombo: string;
  nvre: string;
  nvrea: string;
  packageId: number | null;
  selectable: boolean;
  summary: string;
  arch: string;
};

const packages: PackageFixture[] = [
  {
    idCombo: "101|201|301",
    nvre: "ptf-18102-1-1",
    nvrea: "ptf-18102-1-1.x86_64",
    packageId: 501,
    selectable: true,
    summary: "Fixes a kernel live-patching issue",
    arch: "x86_64",
  },
  {
    idCombo: "102|202|302",
    nvre: "ptf-18134-2-1",
    nvrea: "ptf-18134-2-1.x86_64",
    packageId: 502,
    selectable: true,
    summary: "Corrects package dependency resolution",
    arch: "x86_64",
  },
  {
    idCombo: "103|203|303",
    nvre: "ptf-18201-1-2",
    nvrea: "ptf-18201-1-2.aarch64",
    packageId: null,
    selectable: false,
    summary: "Requires a newer product service pack",
    arch: "aarch64",
  },
];

const selectionKey = (item: PackageFixture) => `${item.idCombo}~*~${item.nvrea}`;

const mockNetworkGet = ((url: string) => {
  const criteria = new URL(url, window.location.origin).searchParams.get("q")?.toLocaleLowerCase() ?? "";
  const items = packages
    .filter((item) => item.nvre.toLocaleLowerCase().includes(criteria))
    .map((item) => ({ ...item }));

  return Utils.cancelable(
    Promise.resolve({
      items,
      total: items.length,
      selectedIds: [selectionKey(packages[0])],
    })
  );
}) as typeof Network.get;

const mockNetworkPost = ((url: string) => {
  if (url.endsWith("/maintenance/upcoming-windows")) {
    return Utils.cancelable(
      Promise.resolve({
        data: {
          maintenanceWindowsMultiSchedules: false,
          maintenanceWindows: null,
        },
      })
    );
  }

  if (url.endsWith("/scheduleAction")) {
    return Utils.cancelable(Promise.resolve(731));
  }

  return Utils.cancelable(Promise.resolve({}));
}) as typeof Network.post;

const packageColumns = [
  <Column key="summary" columnKey="summary" header="Summary" cell={(item: PackageFixture) => item.summary} />,
  <Column key="arch" columnKey="arch" header="Architecture" cell={(item: PackageFixture) => item.arch} />,
];

const meta = {
  title: "Compositions/Package Management/PackageListActionScheduler",
  component: PackageListActionScheduler,
  beforeEach: () => {
    const originalNetworkGet = Network.get;
    const originalNetworkPost = Network.post;
    Network.get = mockNetworkGet;
    Network.post = mockNetworkPost;

    return () => {
      if (Network.get === mockNetworkGet) {
        Network.get = originalNetworkGet;
      }
      if (Network.post === mockNetworkPost) {
        Network.post = originalNetworkPost;
      }
    };
  },
  parameters: {
    layout: "fullscreen",
    docs: {
      description: {
        component:
          "Selects packages and schedules their action immediately or through an action chain. One representative PTF is preselected so the confirmation scheduler can be reached directly; all list, selection, maintenance-window, and scheduling requests stay local to Storybook.",
      },
    },
  },
  args: {
    serverId: 1000010001,
    selectionSet: "storybook_ptf_install",
    actionChains: [
      { id: 12, text: "Monthly maintenance" },
      { id: 18, text: "Urgent production fixes" },
    ],
    icon: "fa-fire-extinguisher",
    listDataAPI: "/storybook/packages",
    scheduleActionAPI: "/storybook/packages/scheduleAction",
    actionType: "packages.update",
    listTitle: "Install Program Temporary Fixes (PTFs)",
    listSummary:
      "Select the support-provided PTF packages that should be installed on this system, then review the schedule.",
    listEmptyText: "No Program Temporary Fixes (PTFs) available.",
    listActionLabel: "Install PTFs",
    listColumns: packageColumns,
    confirmTitle: "Confirm Program Temporary Fixes (PTFs) Installation",
  },
  argTypes: {
    serverId: {
      control: "number",
      description: "Target system identifier used for maintenance-window checks.",
    },
    selectionSet: {
      control: false,
      description: "Server-side selection-set label; requests are intercepted by this story.",
    },
    actionChains: {
      control: false,
      description: "Existing action chains offered by the confirmation scheduler.",
    },
    icon: {
      control: "text",
      description: "Icon shown in the package-list panel heading.",
    },
    listDataAPI: {
      control: false,
      description: "Paged package endpoint; served by local fixtures in this story.",
    },
    scheduleActionAPI: {
      control: false,
      description: "Scheduling endpoint; served locally in this story.",
    },
    actionType: {
      control: false,
      description: "Action type used when checking maintenance windows and scheduling.",
    },
    listTitle: {
      control: "text",
      description: "Title of the package-selection step.",
    },
    listSummary: {
      control: "text",
      description: "Guidance shown above the available packages.",
    },
    listEmptyText: {
      control: "text",
      description: "Message shown when the package endpoint is empty.",
    },
    listActionLabel: {
      control: "text",
      description: "Label of the button that advances to confirmation.",
    },
    listColumns: {
      control: false,
      description: "Additional columns rendered beside the package name.",
    },
    confirmTitle: {
      control: "text",
      description: "Title of the scheduling confirmation step.",
    },
  },
  render: (args) => (
    <div style={{ width: "100%", maxWidth: "1400px", minHeight: "850px", padding: "24px" }}>
      <PackageListActionScheduler key={`${args.serverId}-${args.actionType}`} {...args} />
    </div>
  ),
} satisfies Meta<typeof PackageListActionScheduler>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
