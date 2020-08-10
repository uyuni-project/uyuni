/**
 * Test for the Label component - this time using the React Testing Library
 */

// todo this file to something else

// import dependencies
import React from 'react'

// import API mocking utilities from Mock Service Worker
import { rest } from 'msw'
import { setupServer } from 'msw/node'

// import react-testing methods
import { render, fireEvent, waitFor, screen } from '@testing-library/react'

// add custom jest matchers from jest-dom
import '@testing-library/jest-dom/extend-expect'

import AppStreams from './appstreams.js'

// we need to import bloody jquery, so that we can mock Network with msw in a comfortable way
import jQuery from 'jquery';

jest.mock('components/toastr/toastr', () => ({
    showErrorToastr: jest.fn()
}));

const server = setupServer();

beforeAll(() => {
    global.jQuery = jQuery;
    server.listen();
});
afterEach(() => server.resetHandlers());
afterAll(() => {
    global.jQuery = undefined;
    server.close();
});

test('initial component state: a link is displayed', async() => {
    render(<AppStreams />);
    // not really a meaningful test
    expect(screen.getByText('Browse available modules')).toHaveTextContent('Browse available modules');
});

test('component is loading channels', async () => {
    render(<AppStreams />);

    server.use(
        rest.get('/rhn/manager/api/channels/modular', (req, res, ctx) => {
            // let's add some delay to the loading since we want to check
            // the 'loading' state
            return res(ctx.json({'data': []}), ctx.delay(200));
        })
    );

    const contents = 'Browse available modules';

    // firstly a search icon is displayed
    await waitFor(() => 
        expect(screen.getByText(contents).querySelector('.fa-search')).not.toBeNull());

    fireEvent.click(screen.getByText(contents));
    // after clicking, we make spinner go brrrrr
    await waitFor(() => 
        expect(screen.getByText(contents).querySelector('.fa-spin')).not.toBeNull());

    // finally, after channel list is fetched, we show the channel selector
    const selectorContents = 'Select a channel to browse available modules';
    await waitFor(() => 
        expect(screen.getByText(selectorContents)).not.toBeNull());
});

test('component is displaying channels', async () => {
    render(<AppStreams />);

    const channel1Name = 'testing channel 1';
    const channel2Name = 'testing channel 2';
    const channels = [
        {'id': 1, 'name': channel1Name},
        {'id': 2, 'name': channel2Name},
    ];

    server.use(
        rest.get('/rhn/manager/api/channels/modular', (req, res, ctx) => {
            return res(ctx.json({'data': channels}));
        })
    );

    // let's click the element
    const contents = 'Browse available modules';
    fireEvent.click(screen.getByText(contents));

    // and check that the channel list will get eventually loaded
    await waitFor(() => {
        expect(screen.getByText(channel1Name)).not.toBeNull();
        expect(screen.getByText(channel2Name)).not.toBeNull();
    });

});
