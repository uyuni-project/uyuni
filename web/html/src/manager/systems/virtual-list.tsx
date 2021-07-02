import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import {IconTag} from "components/icontag";

// See java/code/src/com/suse/manager/webui/templates/systems/virtual-list.jade
type Props = {
  /** Locale of the help links */
  docsLocale: string,
};

function VirtualSystems(props: Props) {
  return (
    <>
      <h1>
        <IconTag type="header-system"/>
        {t("Virtual Systems")}
        <a href={`/docs/${props.docsLocale}/reference/systems/systems-list.html`} target="_blank">
          <IconTag type="header-help"/>
        </a>
      </h1>
      <p>TODO Implement me!</p>
    </>
  );
}

export const renderer = (id: string, docsLocale: string) =>
  SpaRenderer.renderNavigationReact(
    <VirtualSystems docsLocale={docsLocale}/>,
    document.getElementById(id)
  );