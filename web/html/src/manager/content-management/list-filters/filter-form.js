//@flow
import React from 'react';
import {Text} from "components/input/Text";
import {Select} from "components/input/Select";
import {Form} from "components/input/Form";
import {Check} from "components/input/Check";

import type {FilterType} from '../shared/type/filter.type.js';

type Props = {
  filter: FilterType,
  onChange: Function,
  editing?: boolean
}

const FilterForm = (props: Props) =>
  <Form
    model={{...props.filter}}
    onChange={model => {
      props.onChange(model);
    }}
  >
    <React.Fragment>
      <div className="row">
        <Text
          name="name"
          label={t("Name")}
          labelClass="col-md-3"
          divClass="col-md-8"
          disabled={props.editing}
        />
      </div>
      <div className="row">
        <Select
          name="type"
          label={t("Filter Type")}
          labelClass="col-md-3"
          divClass="col-md-8">
          <option key={'package'} value={'package'} selected>{t('package')}</option>
          <option key={'patch'} value={'patch'} disabled>{t('patch (To be implemented for RC)')}</option>
        </Select>
      </div>
      <div className="row">
        <Text
          name="criteria"
          label={t("Name criteria")}
          labelClass="col-md-3"
          divClass="col-md-8"/>
      </div>
        <Check
          name="deny"
          disabled
          label={t("deny")}
          divClass="col-md-8 col-md-offset-3"/>
    </React.Fragment>
  </Form>


export default FilterForm;
