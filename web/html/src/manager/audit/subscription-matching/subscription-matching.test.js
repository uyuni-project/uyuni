/**
 * Test for the subscription-matching component.
 */

import React from 'react';
import { render, unmountComponentAtNode } from 'react-dom';
import { act } from 'react-dom/test-utils';

import { SubscriptionMatching } from './subscription-matching.js';

let container = null;

beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
});

afterEach(() => {
  unmountComponentAtNode(container);
  container.remove();
  container = null;
});

// testing response from the server
const responseWithNoData = {
  'matcherDataAvailable': false
};
const responseWithData = {
  'matcherDataAvailable': true,
  'latestStart': '2020-06-26T07:58:02.294Z',
  'latestEnd': '2020-06-26T07:58:29.214Z',
  'subscriptions': {
    '123': {
      'id': 123,
      'partNumber': '123455',
      'description': 'A Subscription',
      'policy': 'one_two',
      'totalQuantity': 1,
      'matchedQuantity': 1,
      'startDate': '2017-08-07T00:00:00.000Z',
      'endDate': '2018-08-07T00:00:00.000Z'
    }
  },
  'messages': [
    {
      'type': 'guest_with_unknown_host',
      'data': {
        'id': '1000010000'
      }
    },
    {
      'type': 'unknown_cpu_count',
      'data': {
        'id': '1000010000'
      }
    }
  ],
  'products': {
    '832': {
      'id': 832,
      'productName': 'SUSE Linux Enterprise Server 10 SP4 x86_64',
      'unmatchedSystemCount': 0,
      'unmatchedSystemIds': []
    },
    '-18': {
      'id': -18,
      'productName': 'Ubuntu 20.04',
      'unmatchedSystemCount': 1,
      'unmatchedSystemIds': [1000010001]
    }
  },
  'unmatchedProductIds': [
    -18
  ],
  'pinnedMatches': [],
  'systems': {
    '1000010000': {
      'id': 1000010000,
      'name': 'suma-refhead-min-centos7.mgr.suse.de',
      'cpuCount': null,
      'productIds': [832],
      'type': 'virtualGuest',
      'possibleSubscriptionIds': []
    },
    '1000010001': {
      'id': 1000010001,
      'name': 'suma-refhead-min-ubuntu1804.mgr.suse.de',
      'cpuCount': null,
      'productIds': [-18],
      'type': 'virtualGuest',
      'possibleSubscriptionIds': []
    }
  }
};

// The goal here is to write a thin test for a nontrivial react
// component (mocking API etc.)  without using too many libraries like
// react-test-library.

it('renders a todo', () => {
  // todo start here: fake the server response using the data above (with and without ^^^)
  act(() => {
    render(<SubscriptionMatching />, container);
  });
  expect(container.textContent).toBe('my-label-1:');

});

