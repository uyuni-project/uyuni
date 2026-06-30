import { StoryRow, StorySection, StripedStorySection } from "manager/storybook/layout";

import { Badge } from "components/badge/Badge";
import { CounterBadge } from "components/badge/CounterBadge";
import { Button } from "components/buttons";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
export default () => {
  return (
    <>
      <h4>Status Badge</h4>
      <Badge text="Default" />
      <Badge text="Success" icon="fa-check" color="success" />
      <Badge text="Warning" icon="fa-exclamation-triangle" color="warning" />
      <Badge text="Error" icon="fa-ban" color="error" />
      <Badge text="Info" icon="fa-info-circle" color="info" />
      <Badge text="Running" icon="fa-spinner fa-spin" color="running" />

      <h4>Special Badge</h4>
      <Badge text="Default" variant="special" />
      <Badge text="New" color="green" variant="special" />
      <Badge text="Removed" color="red" variant="special" />
      <Badge text="Beta" color="yellow" variant="special" />
      <Badge text="Internal" color="blue" variant="special" />

      <h4 className="mt-5">Counter Badge</h4>
      <CounterBadge text="1" />
      <CounterBadge text="999+" status="highlight" />

      <h4 className="mt-5">Badge in Panel:</h4>
      <BootstrapPanel title="Panel">
        <Badge text="Default" />
        <Badge text="Success" icon="fa-check" color="success" />
        <Badge text="Warning" icon="fa-exclamation-triangle" color="warning" />
        <Badge text="Error" icon="fa-ban" color="error" />
        <Badge text="Info" icon="fa-info-circle" color="info" />
        <Badge text="Running" icon="fa-spinner fa-spin" color="running" />
        <h4 className="mt-5">Counter Badge</h4>
        <CounterBadge text="1" />
        <CounterBadge text="999+" status="highlight" />
        <h4 className="mt-5">Special Badge</h4>
        <Badge text="Default" variant="special" />
        <Badge text="New" color="green" variant="special" />
        <Badge text="Removed" color="red" variant="special" />
        <Badge text="Beta" color="yellow" variant="special" />
        <Badge text="Internal" color="blue" variant="special" />
      </BootstrapPanel>
    </>
  );
};
