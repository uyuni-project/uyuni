import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { localizedMoment } from "utils";
import { Utils } from "utils/functions";
import Network from "utils/network";

import { ScheduleScapScanForm } from "./schedule-scap-scan-form";

type ScheduleScapScanFormProps = React.ComponentProps<typeof ScheduleScapScanForm>;

const mockNetworkGet = ((url: string) => {
  if (url.includes("/policy/view/")) {
    return Utils.cancelable(
      Promise.resolve({
        scapContentId: 101,
        xccdfProfileId: "xccdf_org.ssgproject.content_profile_cis",
        xccdfProfileTitle: "CIS Server Level 1",
        tailoringFileId: 201,
        tailoringProfileId: "xccdf_org.example_profile_web_server",
        tailoringProfileTitle: "Web server adjustments",
        ovalFiles: "/usr/share/xml/scap/suse-sles15-cve.xml",
        advancedArgs: "--remediate",
        fetchRemoteResources: false,
      })
    );
  }

  if (url.includes("/tailoringFile/")) {
    return Utils.cancelable(
      Promise.resolve([
        { id: "xccdf_org.example_profile_web_server", title: "Web server adjustments" },
        { id: "xccdf_org.example_profile_database", title: "Database server adjustments" },
      ])
    );
  }

  return Utils.cancelable(
    Promise.resolve([
      { id: "xccdf_org.ssgproject.content_profile_cis", title: "CIS Server Level 1" },
      { id: "xccdf_org.ssgproject.content_profile_standard", title: "Standard System Security Profile" },
    ])
  );
}) as typeof Network.get;

const mockNetworkPost = (() =>
  Utils.cancelable(
    Promise.resolve({
      data: {
        maintenanceWindowsMultiSchedules: false,
        maintenanceWindows: null,
      },
    })
  )) as typeof Network.post;

const ScheduleScapScanFormStory = (props: ScheduleScapScanFormProps) => {
  return (
    <div style={{ maxWidth: "1200px", minHeight: "900px" }}>
      <ScheduleScapScanForm {...props} />
    </div>
  );
};

const meta = {
  title: "Compositions/Compliance/ScheduleScapScanForm",
  component: ScheduleScapScanForm,
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
    docs: {
      description: {
        component:
          "Configures and schedules an XCCDF scan. Policy, profile, tailoring-file, and maintenance-window requests are handled by local fixtures.",
      },
    },
  },
  args: {
    scapContentList: [
      { id: 101, name: "SUSE Linux Enterprise 15 Security Guide" },
      { id: 102, name: "SUSE Linux Enterprise 16 Security Guide" },
    ],
    tailoringFiles: [
      { id: 201, name: "Production tailoring file" },
      { id: 202, name: "Development tailoring file" },
    ],
    scapPolicies: [
      { id: 301, policyName: "CIS Server Level 1" },
      { id: 302, policyName: "PCI DSS" },
    ],
    earliest: localizedMoment("2026-09-24T09:00:00Z"),
    minions: [{ id: 1000010001 }],
    createRecurringLink: "/rhn/manager/recurring/recurring-actions",
    onSubmit: async (model) => {
      action("scan scheduled")(model);
      return 731;
    },
  },
  argTypes: {
    scapContentList: {
      control: false,
      description: "Available SCAP data streams.",
    },
    tailoringFiles: {
      control: false,
      description: "Available tailoring files.",
    },
    scapPolicies: {
      control: false,
      description: "Saved policies that can prefill and lock the form.",
    },
    earliest: {
      control: false,
      description: "Initial earliest execution time.",
    },
    minions: {
      control: false,
      description: "Target systems used when checking maintenance windows.",
    },
    createRecurringLink: {
      control: "text",
      description: "Optional destination for creating a recurring scan.",
    },
    checkMemory: {
      control: false,
      description: "Legacy compatibility property; currently unused by the component.",
    },
    onSubmit: {
      control: false,
      description: "Schedules the scan and resolves with the created action ID.",
    },
  },
  render: (args) => <ScheduleScapScanFormStory {...args} />,
} satisfies Meta<typeof ScheduleScapScanForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
