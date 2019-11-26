import * as React from 'react';
import { Form } from './Form';
import { Text } from './Text';
import { FormMultiInput } from './FormMultiInput';
import { SubmitButton } from 'components/buttons';
import { action } from '@storybook/addon-actions';

export default {
  component: FormMultiInput,
  title: 'Forms/FormMultiInput'
};

export const Example = () => {
  const [model, setModel] = React.useState({});
  return (
    <Form
      model={model}
      onChange={setModel}
      onSubmit={action('Submit clicked')}
      onSubmitInvalid={action('Submit clicked when invalid')}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <FormMultiInput
        id="users"
        title={t('Users')}
        prefix="user"
        onAdd={(index) => {
          const newModel = Object.assign({}, model, {
            [`user${index}_firstname`]: '',
            [`user${index}_lastname`]: '',
          });
          setModel(newModel);
          action('add clicked')(index);
        }}
        onRemove={index => {
          const newModel = Object.entries(model).reduce((res, entry) => {
            const property = !entry[0].startsWith(`user${index}_`) ? { [entry[0]]: entry[1] } : undefined;
            return Object.assign(res, property);
          }, {});
          setModel(newModel);
          action('remove clicked')(index);
        }}
        disabled={false}
        panelTitle={index => model[`user${index}_lastname`] || 'New user'}
      >
        {
          (index: number) => (
            <>
              <Text
                name={`user${index}_firstname`}
                label={t('First Name')}
                required
                invalidHint={t('Minimum 2 characters')}
                labelClass="col-md-3"
                divClass="col-md-6"
                validators={[(value => (value.length > 2))]}
              />
              <Text
                name={`user${index}_lastname`}
                label={t('Last Name')}
                required
                invalidHint={t('Minimum 2 characters')}
                labelClass="col-md-3"
                divClass="col-md-6"
                validators={[(value => (value.length > 2))]}
              />
            </>
          )
        }
      </FormMultiInput>
      <SubmitButton
        id="submit-btn"
        className="btn-success"
        text={t("Submit")}
      />
    </Form>
  )
};
