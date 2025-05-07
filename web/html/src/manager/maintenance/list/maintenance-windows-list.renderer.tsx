import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr";

import { MaintenanceWindowsList } from "./maintenance-windows-list";

type Props = {
  type?: any;
  isAdmin?: any;
};

export const renderer = (id: string, props: Props) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <MaintenanceWindowsList type={props.type} />
    </>,
    document.getElementById(id)
  );
};
