//@flow

import React, { useEffect, useRef } from 'react';
import { hot } from 'react-hot-loader';
import { useInputValue } from 'components/hooks/forms/useInputValue';
import { Messages } from '../../components/messages';
import { AsyncButton } from '../../components/buttons';
import useLoginApi from './use-login-api';
import styles from './login.css';
import withPageWrapper from "components/general/with-page-wrapper";

import LoginHeader from './login-header';
import LoginFooter from './login-footer';

const products = {
  suma: {
    key: 'SUSE Manager',
    headerTitle:
      <React.Fragment>
        <span>SUSE</span>
        <i className="fa fa-registered" />
        <span>Manager</span>
      </React.Fragment>,
    bodyTitle: <span>SUSE<br />{' Manager'}</span>,
    url: 'http://www.suse.com/products/suse-manager/',
    title: 'SUSE Manager login page',
  },
  uyuni: {
    key: 'Uyuni',
    headerTitle: 'Uyuni',
    bodyTitle: 'Uyuni',
    url: 'http://www.uyuni-project.org/',
    title: 'Uyuni login page',
  },
};

const getGlobalMessages = (validationErrors, schemaUpgradeRequired) => {
  let messages = [];

  if (validationErrors && validationErrors.length > 0) {
    messages = messages.concat(validationErrors.map(msg => ({ severity: 'error', text: msg })));
  }

  if (schemaUpgradeRequired) {
    const schemaUpgradeError = t('A schema upgrade is required. Please upgrade your schema at your earliest convenience to receive latest bug fixes and avoid potential problems.');
    messages = messages.concat({ severity: 'error', text: schemaUpgradeError });
  }

  return messages;
};

const getFormMessages = (success, messages) => {
  if (success) {
    return [{severity: "success", text: <p>{t('Signing in ...')}.</p>}];
  }

  if (messages.length > 0) {
    return messages.map(msg => ({ severity: 'error', text: msg }));
  }

  return [];
};

type Props = {
  isUyuni: boolean,
  bounce: string,
  validationErrors: Array<string>,
  schemaUpgradeRequired: boolean,
  webVersion: string,
  productName: string,
  customHeader: string,
  customFooter: string,
  legalNote: string,
  loginLength: string,
  passwordLength: string,
}

const Login = (props: Props) => {
  const loginInput = useInputValue('');
  const passwordInput = useInputValue('');
  const { onLogin, success, messages } = useLoginApi();
  const loginInputRef = useRef();

  useEffect(() => {
    loginInputRef && loginInputRef.current && loginInputRef.current.focus();
  }, []);

  const product = props.isUyuni ? products.uyuni : products.suma;

  return (
    <React.Fragment>

      <LoginHeader
        title={product.title}
        text={product.headerTitle}
        customHeader={props.customHeader}
      />

      <div className={`spacewalk-main-column-layout ${styles.fixed_content}`}>
        <section id="spacewalk-content">
          <div className="wrap">
            <div name="container">
              <Messages items={getGlobalMessages(props.validationErrors, props.schemaUpgradeRequired)} />
              <React.Fragment>
                <div className="col-sm-6">
                  <h1>{product.bodyTitle}</h1>
                  <p className="gray-text margins-updown">
                    Discover a new way of managing your servers, packages, patches and more via one interface.
                  </p>
                  <p className="gray-text">
                    Learn more about
                    {' '}
                    {product.key}
                    :
                    <a href={product.url} className="btn-dark" target="_blank" rel="noopener noreferrer"> View website</a>
                  </p>
                </div>
                <div className="col-sm-5 col-sm-offset-1">
                  <Messages items={getFormMessages(success, messages)} />
                  <h2 className="gray-text">{t('Sign In')}</h2>
                  <form
                    onSubmit={(event) => event.preventDefault()}
                    name="loginForm">
                    <div className="margins-updown">
                      <input
                        id="username-field"
                        name="username"
                        className="form-control"
                        type="text"
                        placeholder={t('Login')}
                        tabindex="1"
                        maxlength={props.loginLength}
                        ref={loginInputRef}
                        {...loginInput}
                      />
                      <input
                        id="password-field"
                        name="password"
                        className="form-control"
                        type="password"
                        placeholder={t('Password')}
                        tabindex="2"
                        maxlength={props.passwordLength}
                        {...passwordInput}
                      />
                      <AsyncButton
                        id="login-btn"
                        className="btn-block"
                        defaultType="btn-success"
                        text={t('Sign In')}
                        action={() =>
                          onLogin({login: loginInput.value, password: passwordInput.value})
                            .then(success => success && window.location.replace(props.bounce))
                        }
                      />
                    </div>
                  </form>
                  <hr />
                  <p className="gray-text small-text">
                    {props.legalNote}
                  </p>
                </div>
              </React.Fragment>
            </div>
          </div>
        </section>
      </div>

      <LoginFooter
        productName={props.productName}
        customFooter={props.customFooter}
        webVersion={props.webVersion}
      />

    </React.Fragment>
  );
};

export default hot(module)(withPageWrapper<Props>(Login));
