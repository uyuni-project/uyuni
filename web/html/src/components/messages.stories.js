import React from 'react';
import { storiesOf } from '@storybook/react';
import { Messages } from './messages';


storiesOf('Messages', module)
  .add('with errors, warnings, success and info messages', () => (
    <Messages items={[
      { severity: "error", text: "error message"},
      { severity: "warning", text: "warning message"},
      { severity: "success", text: "success message"},
      { severity: "info", text: "info message"}
    ]} />
  ))

