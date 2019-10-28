import React from 'react';
import { storiesOf } from '@storybook/react';
import { Loading } from './loading';

storiesOf('Loading', module)
  .add('normal', () => (
    <Loading />
  ))
  .add('with text', () => (
    <Loading text="loading text" />
  ))
  .add('with border', () => (
    <Loading text="loading text" withBorders/>
  ))
