/**
 * Test for the Label component.
 * (todo javadoc notation ok?)
 */

// todo ' or "? do we have a convention, eslint?
import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';

import { Label } from './Label';

let container = null;

// ok, writing this boilerplate all the time is annoying
beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
});

afterEach(() => {
  unmountComponentAtNode(container);
  container.remove();
  container = null;
});

// The goal here is to write a thin test for a react component,
// without using too many libraries like react-test-library.

it('renders a label', () => {
  act(() => {
    render(<Label name='my-label-1' />, container);
  });
  expect(container.textContent).toBe('my-label-1:');

  act(() => {
    render(<Label name='<inside />' />, container);
  });
  expect(container.textContent).toBe('<inside />:');

});

it('renders a label with required flag', () => {
  act(() => {
    render(<Label name='required' required />, container);
  });

  expect(container.textContent).toBe('required *:');
});

it('renders a label with or without class', () => {
  act(() => {
    render(<Label name='my-label-implicit-class' />, container); 
  });

  expect(container.childNodes[0].className).toBe('control-label');

  act(() => {
    render(<Label name='my-label-explicit-class' className='extra' />, container); 
  });

  expect(container.childNodes[0].className).toBe('control-label extra');
});
