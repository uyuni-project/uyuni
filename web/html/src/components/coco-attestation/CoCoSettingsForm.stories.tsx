import type { Meta, StoryObj } from "@storybook/react-webpack5";
import { action } from "storybook/actions";

import { CoCoSettingsForm } from "./CoCoSettingsForm";
import { type Settings, HOST_KEY_DOCUMENT_FIELD, SECURE_EXECUTION_HEADER_FIELD } from "./Utils";

const initialData: Settings = {
  enabled: true,
  environmentType: "KVM_IBM_Z",
  attestOnBoot: true,
  attestOnSchedule: true,
  inputData: {
    [SECURE_EXECUTION_HEADER_FIELD]: "U3Rvcnlib29rIHNlY3VyZSBoZWFkZXI=",
    [HOST_KEY_DOCUMENT_FIELD]: [
      "-----BEGIN CERTIFICATE-----",
      "U3Rvcnlib29rIGhvc3Qga2V5IGNlcnRpZmljYXRl",
      "-----END CERTIFICATE-----",
    ].join("\n"),
  },
};

const meta = {
  title: "Compositions/Confidential Computing/CoCoSettingsForm",
  component: CoCoSettingsForm,
  parameters: {
    docs: {
      description: {
        component:
          "Configures confidential-computing attestation. The fixture starts with IBM Z inputs and scheduled execution enabled so all conditional sections are visible.",
      },
    },
  },
  args: {
    initialData,
    availableEnvironmentTypes: {
      KVM_AMD: "KVM on AMD",
      KVM_INTEL: "KVM on Intel",
      KVM_IBM_Z: "KVM on IBM Z",
    },
    showOnScheduleOption: true,
    saveHandler: async (settingsPromise) => {
      action("settings saved")(await settingsPromise);
    },
  },
  argTypes: {
    initialData: {
      control: "object",
      description: "Persisted settings used to initialize and reset the form.",
    },
    availableEnvironmentTypes: {
      control: "object",
      description: "Environment types offered by the backend.",
    },
    showOnScheduleOption: {
      control: "boolean",
      description: "Shows scheduling controls when scheduled attestation is supported.",
    },
    saveHandler: {
      control: false,
      description: "Receives a promise resolving to the normalized settings payload.",
    },
  },
  render: (args) => (
    <div style={{ maxWidth: "1200px", minHeight: "850px" }}>
      <CoCoSettingsForm {...args} />
    </div>
  ),
} satisfies Meta<typeof CoCoSettingsForm>;

export default meta;

type Story = StoryObj<typeof meta>;

export const Playground: Story = {};
