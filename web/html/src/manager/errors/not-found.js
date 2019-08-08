/* eslint-disable */
"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const SpaRenderer  = require("core/spa/spa-renderer").default;

const NotFound = ({currentUrl}) =>
    <body>
        <h1>
            {t('Page Not Found')}
        </h1>
        <p>{t('The page you requested, {0}, was not found.', currentUrl)}</p>
        <ol>
            <li>
                {t('If you typed this address into your browser, double-check the spelling and capitalization.' +
                    ' The address you are copying may also be outdated.')}
            </li>
            <li>
                {t('If you followed a link, the link may be incorrect or outdated. Click on the back button in your ' +
                    'browser to try a different link, or use the top (or left) navigation bars to find the page you are ' +
                    'looking for.')}
            </li>
            <li>
                {t('If you used a bookmark, the page may have moved to a different location since the bookmark was ' +
                    'created. Use the top (or left) navigation bars to find the page you are looking for, and if you ' +
                    'find the document be sure to update your bookmark.')}
            </li>
            <li>
                {t('If you feel you\'ve reached this page in error, please contact us with details of how you received ' +
                    'this message.')}
            </li>
        </ol>
    </body>;

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.errors = window.pageRenderers.errors || {};

window.pageRenderers.errors.NotFoundRenderer = (id, {currentUrl}) => SpaRenderer.renderNavigationReact(
    <NotFound
        currentUrl={currentUrl}
    />,
    document.getElementById(id)
);
