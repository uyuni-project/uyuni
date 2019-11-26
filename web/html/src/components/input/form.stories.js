import * as React from 'react';
import { storiesOf } from '@storybook/react';
import { Form } from './Form';
import { Text } from './Text';
import { Password } from './Password';
import { Check } from './Check';
import { DateTime } from './DateTime';
import { Radio } from './Radio';
import { Select } from './Select';
import { SubmitButton } from 'components/buttons';

storiesOf('Forms', module)
  .add('text input', () => {
    let model = {
      firstname: 'John',
    };

    return (
      <Form
        model={model}
        onChange={newModel => {model['firstname'] = newModel['firstname']}}
        onSubmit={() => alert(`Hello ${model['firstname']}`)}
        onSubmitInvalid={(data, evt) => alert("Submit clicked, but form invalid")}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Text
          name="firstname"
          label={t('First Name')}
          required
          invalidHint={t('Minimum 2 characters')}
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value => (value.length > 2))]}
        />
        <SubmitButton
          id="submit-btn"
          className="btn-success"
          text={t("Submit")}
        />
      </Form>
    );
  })
  .add('password input', () => {
    let model = {
      password: 'secret',
    };

    return (
      <Form
        model={model}
        onChange={newModel => {model['password'] = newModel['password']}}
        onSubmit={() => alert(`Secret revealed: ${model['password']}`)}
        onSubmitInvalid={(data, evt) => alert("Submit clicked, but form invalid")}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Password
          name="password"
          label={t('Password')}
          required
          invalidHint={t('Minimum 4 characters')}
          labelClass="col-md-3"
          divClass="col-md-6"
          validators={[(value => (value.length > 4))]}
        />
        <SubmitButton
          id="submit-btn"
          className="btn-success"
          text={t("Submit")}
        />
      </Form>
    );
  })
  .add('check box', () => {
    let model = {
      force: false,
    };

    return (
      <Form
        model={model}
        onSubmit={() => alert(`May${model['force'] ? '' : ' NOT'} the force be with you`)}
        divClass="col-md-12"
        formDirection="form-horizontal"
        onChange={newModel => {model['force'] = newModel['force']}}
      >
        <Check
          name="force"
          label="Force action"
          divClass="col-md-6 col-md-offset-3"
        />
        <SubmitButton
          id="submit-btn"
          className="btn-success"
          text={t("Submit")}
        />
      </Form>
    )
  })
  .add('date time input', () => {
    let model = {
      time: new Date(),
    };

    return (
      <Form
        model={model}
        onChange={newModel => {model['time'] = newModel['time']}}
        onSubmit={() => alert(`Set time: ${model['time'].toISOString()}`)}
        onSubmitInvalid={(data, evt) => alert("Submit clicked, but form invalid")}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <DateTime
          name="time"
          timezone="CEST"
          label={t('Time')}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
        />
        <SubmitButton
          id="submit-btn"
          className="btn-success"
          text={t("Submit")}
        />
      </Form>
    );
  })
  .add('radio button input', () => {
    let model = {
      level: 'beginner',
    };

    return (
      <Form
        model={model}
        onChange={newModel => {model['level'] = newModel['level']}}
        onSubmit={() => alert(`Level: ${model['level']}`)}
        onSubmitInvalid={(data, evt) => alert("Submit clicked, but form invalid")}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Radio
          name="level"
          label={t('Level')}
          required
          openOption={true}
          labelClass="col-md-3"
          divClass="col-md-6"
          items={[
            {label: t('Beginner'), value: 'beginner'},
            {label: t('Normal'), value: 'normal'},
            {label: t('Expert'), value: 'expert'}
          ]}
        />
        <SubmitButton
          id="submit-btn"
          className="btn-success"
          text={t("Submit")}
        />
      </Form>
    );
  })
  .add('drop down list input', () => {
    let model = {
      level: 'beginner',
    };

    return (
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
        >
          <option key="beginner" value="beginner">Beginner</option>
          <option key="normal" value="normal">Normal</option>
          <option key="expert" value="expert">Expert</option>
        </Select>
        <SubmitButton
          id="submit-btn"
          className="btn-success"
          text={t("Submit")}
        />
      </Form>
    );
  })
