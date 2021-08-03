import * as React from 'react';
import FormikForm from './FormikForm';
import { Text } from './FormikText';
import { SubmitButton } from 'components/buttons';

export default {
  component: FormikForm,
  title: 'Forms/FormikForm'
};

let model = {
  firstname: 'John',
};

export const Example = () => (
  <FormikForm
    model={model}
    onSubmit={(foo) => console.log(foo)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Text
      name="firstname"
      label={t('First Name')}
      required
      labelClass="col-md-3"
      disabled
      divClass="col-md-6"
      validators={[(value => (value.length > 2))]}
      invalidHint={t('Minimum 2 characters')}
    />
    <SubmitButton
      id="submit-btn"
      className="btn-success"
      text={t("Submit")}
    />
  </FormikForm>
)
