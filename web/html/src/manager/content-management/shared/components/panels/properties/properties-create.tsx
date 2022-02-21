import * as React from "react";

import { Panel } from "components/panels/Panel";

import { ProjectPropertiesType } from "../../../type/project.type";
import PropertiesForm from "./properties-form";

type Props = {
  properties: ProjectPropertiesType;
  errors: any;
  onChange: Function;
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
