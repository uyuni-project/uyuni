import * as React from "react";

import { AsyncButton } from "components/buttons";
import { MessageType } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";

import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

import { ContainerConfigMessages } from "./container-config-messages";

enum SSLMode {
  UseSSL = "use-ssl",
  CreateSSL = "create-ssl",
}

type Props = {};

type State = {
  caCertificate: string;
  caKey: string;
  caPassword: string;
  country: string;
  state: string;
  city: string;
  org: string;
  orgUnit: string;
  sslEmail: string;
  rootCA: string;
  intermediateCAs: string[];
  proxyCertificate: string;
  proxyKey: string;
  email: string;
  sslMode: SSLMode;
  loading: boolean;
  maxSquidCacheSize: string;
  messages: MessageType[];
  proxyFQDN: string;
  success?: any;
};

export class ProxyConfig extends React.Component<Props, State> {
  initState: State;

  constructor(props: Props) {
    super(props);

    this.initState = {
      caCertificate: "",
      caKey: "",
      caPassword: "",
      country: "",
      state: "",
      city: "",
      org: "",
      orgUnit: "",
      sslEmail: "",
      rootCA: "",
      intermediateCAs: [],
      proxyCertificate: "",
      proxyKey: "",
      email: "",
      sslMode: SSLMode.CreateSSL,
      loading: false,
      maxSquidCacheSize: "",
      messages: [],
      proxyFQDN: "",
    };

    this.state = this.initState;
  }

  onSubmit = () => {
    this.setState({ messages: [], loading: true });
    var formData: any = {};
    formData["proxyFQDN"] = this.state.proxyFQDN;
    formData["maxSquidCacheSize"] = this.state.maxSquidCacheSize;
    formData["email"] = this.state.email;
    formData["sslMode"] = this.state.sslMode;

    if (this.state.sslMode === SSLMode.CreateSSL) {
      formData["caCertificate"] = this.state.caCertificate;
      formData["caKey"] = this.state.caKey;
      formData["caPassword"] = this.state.caPassword;
      formData["country"] = this.state.country;
      formData["state"] = this.state.state;
      formData["city"] = this.state.city;
      formData["org"] = this.state.org;
      formData["orgUnit"] = this.state.orgUnit;
      formData["sslEmail"] = this.state.sslEmail;
    } else {
      formData["rootCA"] = this.state.rootCA;
      formData["intermediateCAs"] = this.state.intermediateCAs;
      formData["proxyCertificate"] = this.state.proxyCertificate;
      formData["proxyKey"] = this.state.proxyKey;
    }
    const request = Network.post("/rhn/manager/api/proxy/container-config", formData).then(
      (data) => {
        this.setState({
          success: data.success,
          messages: data.messages,
          loading: false,
        });
      },
      (xhr) => {
        try {
          this.setState({
            success: false,
            messages: [JSON.parse(xhr.responseText)],
            loading: false,
          });
        } catch (err) {
          var errMessages = DEPRECATED_unsafeEquals(xhr.status, 0)
            ? MessagesUtils.error(
                t(
                  "Request interrupted or invalid response received from the server. Please check if the config archive was generated correctly."
                )
              )
            : MessagesUtils.error(Network.errorMessageByStatus(xhr.status)[0]);
          this.setState({
            success: false,
            messages: errMessages,
            loading: false,
          });
        }
      }
    );
    return request;
  };

  clearFields = () => {
    this.setState(this.initState);
  };

  proxyFQDNChanged = (event) => {
    this.setState({
      proxyFQDN: event.target.value,
    });
  };

  maxSquidCacheSizeChanged = (event) => {
    this.setState({
      maxSquidCacheSize: event.target.value,
    });
  };

  emailChanged = (event) => {
    this.setState({
      email: event.target.value,
    });
  };

  sslModeChanged = (event) => {
    this.setState({
      sslMode: event.target.value,
    });
  };

  replaceCRLF = (fileString) => {
    return fileString.replace(/\r\n/g, "\n");
  };

  caCertificateChanged = (event) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.setState({
        // replace CRLF from Windows
        caCertificate: this.replaceCRLF(e.target?.result),
      });
    };
    reader.readAsText(event.target.files[0]);
  };

  caKeyChanged = (event) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.setState({
        // replace CRLF from Windows
        caKey: this.replaceCRLF(e.target?.result),
      });
    };
    reader.readAsText(event.target.files[0]);
  };

  caPasswordChanged = (event) => {
    this.setState({
      caPassword: event.target.value,
    });
  };

  countryChanged = (event) => {
    this.setState({
      country: event.target.value,
    });
  };

  stateChanged = (event) => {
    this.setState({
      state: event.target.value,
    });
  };

  cityChanged = (event) => {
    this.setState({
      city: event.target.value,
    });
  };

  orgChanged = (event) => {
    this.setState({
      org: event.target.value,
    });
  };

  orgUnitChanged = (event) => {
    this.setState({
      orgUnit: event.target.value,
    });
  };

  sslEmailChanged = (event) => {
    this.setState({
      sslEmail: event.target.value,
    });
  };

  rootCAChanged = (event) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.setState({
        // replace CRLF from Windows
        rootCA: this.replaceCRLF(e.target?.result),
      });
    };
    reader.readAsText(event.target.files[0]);
  };

  // intermediateCAAdd= (event) => {
  //   this.setState({
  //     intermediateCAs: this.state.intermediateCAs.push(event.target.files[0]),
  //   });
  // };

  // intermediateCARemove = (event) => {
  //   this.setState({
  //     intermediateCAs: this.state.intermediateCAs.filter((c) => c.name === event.target.files[0]),
  //   });
  // };

  intermediateCAsChanged = (event) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.setState({
        // replace CRLF from Windows
        intermediateCAs: this.replaceCRLF(e.target?.result),
      });
    };
    reader.readAsText(event.target.files[0]);
  };

  proxyCertificateChanged = (event) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.setState({
        // replace CRLF from Windows
        proxyCertificate: this.replaceCRLF(e.target?.result),
      });
    };
    reader.readAsText(event.target.files[0]);
  };

  proxyKeyChanged = (event) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.setState({
        // replace CRLF from Windows
        proxyKey: this.replaceCRLF(e.target?.result),
      });
    };
    reader.readAsText(event.target.files[0]);
  };

  render() {
    var buttons = [
      <AsyncButton
        id="generate-btn"
        defaultType="btn-success"
        icon="fa-plus"
        text={t("Generate")}
        // disabled={}
        action={this.onSubmit}
      />,
      <AsyncButton
        id="clear-btn"
        defaultType="btn-default pull-right"
        icon="fa-eraser"
        text={t("Clear fields")}
        action={this.clearFields}
      />,
    ];

    var createSSLFields = [
      <div className="form-group">
        <label className="col-md-3 control-label">
          {t("CA certificate to use to sign the SSL certificate in PEM format")}:
        </label>
        <div className="col-md-6">
          <input
            name="caCertificate"
            className="form-control"
            type="file"
            value={undefined}
            onChange={this.caCertificateChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">
          {t("CA private key to use to sign the SSL certificate in PEM format")}:
        </label>
        <div className="col-md-6">
          <input name="caKey" className="form-control" type="file" value={undefined} onChange={this.caKeyChanged} />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The CA private key password")}:</label>
        <div className="col-md-6">
          <input
            name="caPassword"
            className="form-control"
            type="password"
            value={this.state.caPassword}
            onChange={this.caPasswordChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">
          <h2>{t("SSL certificate")}</h2>
        </label>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The 2-letter country code")}:</label>
        <div className="col-md-1">
          <input
            name="country"
            className="form-control"
            type="text"
            maxLength={2}
            value={this.state.country}
            onChange={this.countryChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The state")}:</label>
        <div className="col-md-3">
          <input
            name="state"
            className="form-control"
            type="text"
            value={this.state.state}
            onChange={this.stateChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The city")}:</label>
        <div className="col-md-3">
          <input name="city" className="form-control" type="text" value={this.state.city} onChange={this.cityChanged} />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The organization")}:</label>
        <div className="col-md-6">
          <input name="org" className="form-control" type="text" value={this.state.org} onChange={this.orgChanged} />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The organization unit")}:</label>
        <div className="col-md-6">
          <input
            name="orgUnit"
            className="form-control"
            type="text"
            value={this.state.orgUnit}
            onChange={this.orgUnitChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("The email")}:</label>
        <div className="col-md-6">
          <input
            name="sslEmail"
            className="form-control"
            type="text"
            placeholder={t("e.g., proxy.admin@mycompany.com")}
            value={this.state.sslEmail}
            onChange={this.sslEmailChanged}
          />
        </div>
      </div>,
    ];
    var useSSLFields = [
      <div className="form-group">
        <label className="col-md-3 control-label">
          {t("The root CA used to sign the SSL certificate in PEM format")}:
        </label>
        <div className="col-md-6">
          <input name="rootCA" className="form-control" type="file" value={undefined} onChange={this.rootCAChanged} />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">
          {t("intermediate CA used to sign the SSL certificate in PEM format")}:
        </label>
        <div className="col-md-6">
          <input
            name="intermediateCAs"
            className="form-control"
            type="file"
            value={undefined}
            onChange={this.intermediateCAsChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("Proxy CRT content in PEM format")}:</label>
        <div className="col-md-6">
          <input
            name="proxyCertificate"
            className="form-control"
            type="file"
            value={undefined}
            onChange={this.proxyCertificateChanged}
          />
        </div>
      </div>,
      <div className="form-group">
        <label className="col-md-3 control-label">{t("Proxy SSL private key in PEM format")}:</label>
        <div className="col-md-6">
          <input
            name="proxyKey"
            className="form-control"
            type="file"
            value={undefined}
            onChange={this.proxyKeyChanged}
          />
        </div>
      </div>,
    ];

    return (
      <TopPanel
        title={t("Container Based Proxy Configuration")}
        icon="fa fa-cogs"
        helpUrl="reference/proxy/container-based-config.html"
      >
        <p>{t("TODO: some info text message about this page")}</p>
        {ContainerConfigMessages(this.state)}
        <div className="form-horizontal">
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Proxy FQDN")}:</label>
            <div className="col-md-6">
              <input
                name="proxyFQDN"
                className="form-control"
                type="text"
                placeholder={t("e.g., proxy.domain.com")}
                value={this.state.proxyFQDN}
                onChange={this.proxyFQDNChanged}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Max Squid cache size (MB)")}:</label>
            <div className="col-md-3">
              <input
                name="maxSquidCacheSize"
                className="form-control numeric"
                type="text"
                placeholder={t("e.g., 2048")}
                value={this.state.maxSquidCacheSize}
                onChange={this.maxSquidCacheSizeChanged}
                onKeyPress={window.numericValidate}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("Proxy administrator email")}:</label>
            <div className="col-md-6">
              <input
                name="proxyAdminEmail"
                className="form-control"
                type="text"
                placeholder={t("e.g., proxy.admin@mycompany.com")}
                value={this.state.email}
                onChange={this.emailChanged}
              />
            </div>
          </div>
          <div className="form-group">
            <label className="col-md-3 control-label">{t("SSL certificate")}:</label>
            <div className="col-md-6">
              <div className="radio col-md-3">
                <input
                  type="radio"
                  name="ssl-mode"
                  value="create-ssl"
                  id="create-ssl"
                  onChange={this.sslModeChanged}
                  checked={this.state.sslMode === SSLMode.CreateSSL}
                />
                <label htmlFor="create-ssl" className="control-label">
                  {t("Create")}
                </label>
              </div>
              <div className="radio col-md-3">
                <input
                  type="radio"
                  name="ssl-mode"
                  value="use-ssl"
                  id="use-ssl"
                  onChange={this.sslModeChanged}
                  checked={this.state.sslMode === SSLMode.UseSSL}
                />
                <label htmlFor="use-ssl" className="control-label">
                  {t("Use existing")}
                </label>
              </div>
            </div>
          </div>
          {this.state.sslMode === SSLMode.CreateSSL ? createSSLFields : useSSLFields}
          <div className="form-group">
            <br />
            <div className="col-md-offset-3 col-md-6">{buttons}</div>
          </div>
        </div>
      </TopPanel>
    );
  }
}
