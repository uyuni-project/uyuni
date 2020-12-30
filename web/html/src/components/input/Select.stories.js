import * as React from 'react';
import { Form } from './Form';
import { Select } from './Select';
import { SubmitButton } from 'components/buttons';

export default {
  component: Select,
  title: 'Forms/Select'
};

let model = {
  level: 'beginner',
};

export const Example = () => (
  <Form
    model={model}
    onChange={newModel => {model['level'] = newModel['level']}}
    onSubmit={() => alert(`Level: ${model['level']}`)}
    onSubmitInvalid={(data, evt) => alert("Submit clicked, but form invalid")}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Select
      name="level"
      label={t('Level')}
      required
      labelClass="col-md-3"
      divClass="col-md-6"
      options={[
        {value: "beginner", label: "Beginner"},
        {value: "normal", label: "Normal"},
        {value: "expert", label: "Expert"},
      ]}
    />
    <SubmitButton
      id="submit-btn"
      className="btn-success"
      text={t("Submit")}
    />
  </Form>
)

