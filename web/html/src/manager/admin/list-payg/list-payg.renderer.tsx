import { RolesProvider } from "core/auth/roles-context";
import SpaRenderer from "core/spa/spa-renderer";

import { ServerMessageType } from "components/messages/messages";
import { MessagesContainer } from "components/toastr/toastr";

import ListPayg from "./list-payg";

type RendererProps = {
  payg_instances?: string;
  flashMessage?: ServerMessageType;
  isIssPeripheral?: boolean;
};

export const renderer = (id: string, { payg_instances, flashMessage, isIssPeripheral }: RendererProps = {}) => {
  let projectsJson: any[] = [];
  try {
    projectsJson = JSON.parse(payg_instances || "");
  } catch (error) {
    Loggerhead.error(error);
  }

  SpaRenderer.renderNavigationReact(
    <RolesProvider>
      <MessagesContainer />
      <ListPayg payg_instances={projectsJson} flashMessage={flashMessage} isIssPeripheral={isIssPeripheral} />
    </RolesProvider>,
    document.getElementById(id)
  );
};
