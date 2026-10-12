import type { Meta, StoryObj } from "@storybook/react-webpack5";

import { localizedMoment } from "utils";

import CoCoReport from "./CoCoReport";
import type { AttestationReport } from "./Utils";

const report: AttestationReport = {
  id: 42,
  systemId: 1000010001,
  systemName: "sles15-secure.example.com",
  environmentType: "KVM_IBM_Z",
  environmentTypeLabel: "KVM on IBM Z",
  environmentTypeDescription: "KVM guest using IBM Secure Execution",
  status: "FAILED",
  statusDescription: "Attestation completed with failures",
  creationTime: localizedMoment("2026-09-23T08:30:00Z").toDate(),
  modificationTime: localizedMoment("2026-09-23T08:32:00Z").toDate(),
  attestationTime: localizedMoment("2026-09-23T08:31:30Z").toDate(),
  actionId: 731,
  actionName: "Confidential computing attestation",
  actionScheduledBy: "admin",
  results: [
    {
      id: 421,
      resultType: "HOST_KEY",
      resultTypeLabel: "Host key",
      status: "SUCCEEDED",
      statusDescription: "The host key is trusted",
      description: "Validates the host key document against the expected certificate chain.",
      details: "Certificate chain verified successfully.",
      processOutput: "Verification completed with exit code 0.",
      attestationTime: localizedMoment("2026-09-23T08:31:10Z").toDate(),
    },
    {
      id: 422,
      resultType: "SECURE_EXECUTION",
      resultTypeLabel: "Secure execution",
      status: "FAILED",
      statusDescription: "The secure execution measurement does not match",
      description: "Compares the measured guest image with the expected secure execution header.",
      details: "Expected measurement 4f2a…, received measurement 8c19….",
      processOutput: "Measurement verification failed with exit code 2.",
      attestationTime: localizedMoment("2026-09-23T08:31:30Z").toDate(),
    },
  ],
};

const meta = {
  title: "Compositions/Confidential Computing/CoCoReport",
  component: CoCoReport,
  parameters: {
    docs: {
      description: {
        component:
          "Displays a confidential-computing attestation overview and one tab per result. The representative report includes both successful and failed checks.",
      },
    },
  },
  args: {
    report,
    activeTab: "overview",
  },
  argTypes: {
    report: {
      control: false,
      description: "Attestation report and its result details.",
    },
    activeTab: {
      control: "select",
      options: ["overview", "host-key", "secure-execution"],
      description: "Result tab selected when the report is mounted.",
    },
  },
  render: (args) => (
    <div style={{ maxWidth: "1100px", minHeight: "420px" }}>
      <CoCoReport key={`${args.report.id}-${args.activeTab}`} {...args} />
    </div>
  ),
} satisfies Meta<typeof CoCoReport>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
