import * as React from "react";
import { Panel } from "components/panels/Panel";
import PropertiesForm from "./properties-form";

import { ProjectPropertiesType } from "../../../type/project.type";

type Props = {
  properties: ProjectPropertiesType;
  errors: any;
  onChange: (...args: any[]) => any;
};

const Properties = (props: Props) => {
  return (
    <Panel headingLevel="h2" title={t("Project Properties")}>
      <div className="col-md-8">
        <PropertiesForm properties={props.properties} errors={props.errors} onChange={props.onChange} />
      </div>
    </Panel>
  );
};

export default Properties;
