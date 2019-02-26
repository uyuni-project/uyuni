// @flow
import React from 'react';
import {Panel} from "../../../../../../components/panels/Panel";
import PropertiesForm from "./properties-form";

import type {projectPropertiesType} from '../../../type/project.type.js';

type Props = {
  properties: projectPropertiesType,
  onChange: Function,
};

const Properties = (props: Props) => {
  return (
    <Panel
      headingLevel="h2"
      title={t('Project Properties')}>
      <div className="col-md-8">
        <PropertiesForm
          properties={props.properties}
          onChange={props.onChange}
        />
      </div>
    </Panel>
  );
};

export default Properties;
