import { IconTag } from "components/icontag";

import { ActionStatus } from "./ActionStatus";
import styles from "../../manager/content-management/shared/components/panels/sources/channels/channels-selection.module.scss";

export default () => {
  const system = {
    reason: "failed",
    details: null,
  };

  const isOpen = false;
   let iconClass, iconTitle, iconStyle;
   iconClass = "spacewalk-icon-salt-add";
    iconTitle = t("Internal State");
    iconStyle = "";

  return (
    <>
      <ActionStatus serverId="server123" actionId="456" status="Queued" />,
      <ActionStatus serverId="server123" actionId="456" status="Picked Up" />,
      <ActionStatus serverId="server123" actionId="456" status="Failed" />,
      <ActionStatus serverId="server123" actionId="456" status="Completed" />
     <hr />
      <IconTag type="experimental" size="sm" title="Small" />
      <hr />
      <IconTag type="experimental" title="Default" /> Default 14px
      <hr />
      <IconTag type="experimental" title="Medium" className="spacewalk-help-link"/> | <IconTag type="experimental" title="md" size="md"/>spacewalk-help-link | md 16px
      <hr />
      <IconTag type="experimental" title="Large" className="fa-1-5x"/> | <IconTag type="experimental" title="lg" size="lg"/>fa-1-5x 18.2 | lg 18px
      <hr />
      <IconTag type="experimental" title="xl" className="fa-2x" /> | <IconTag type="experimental" title="xl" size="xl"/>fa-2x 28px | xl 24px
       <hr />
      <IconTag type="experimental" className="xxl" /> | <IconTag type="experimental" className="icon-size-2xl" />fa-3x | icon-size-2xl 36px
    

      <hr></hr>
      <IconTag type="header-mgr-server"  className="fa-lg" title="fa-small" />
    </>

  );
};
