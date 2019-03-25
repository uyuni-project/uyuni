//@flow
import React from 'react';
import {Text} from "components/input/Text";
import {Select} from "components/input/Select";
import {Form} from "components/input/Form";

import type {ProjectEnvironmentType} from '../../../type/project.type.js';

type Props = {
  environment: ProjectEnvironmentType,
  environments: Array<ProjectEnvironmentType>,
  onChange: Function,
  editing?: boolean
}

const EnvironmentForm = (props: Props) =>
  <Form
    model={props.environment}
    onChange={model => {
      props.onChange(model);
    }}
  >
    <React.Fragment>
      <div className="row">
        <Text
          name="name"
          // ref={nameInputRef}
          label={t("Name")}
          labelClass="col-md-3"
          divClass="col-md-8"/>
      </div>
      <div className="row">
        <Text
          name="description"
          label={t("Description")}
          labelClass="col-md-3"
          divClass="col-md-8"/>
      </div>
      {
        !props.editing &&
        <div className="row">
          <Select
            name="predecessorLabel"
            label={t("Insert before")}
            labelClass="col-md-3"
            divClass="col-md-8">
            <option
              key={"predecessorLabelEmpty"}/>
            {props.environments && props.environments.map(env =>
              <option
                key={env.label}
                value={env.label}>
                {env.name}
              </option>
            )}
          </Select>
        </div>
      }
    </React.Fragment>
  </Form>

export default EnvironmentForm;
