import "swagger-ui-react/swagger-ui.css";
import "./swagger.scss";

import { useEffect, useState } from "react";

import SwaggerUI from "swagger-ui-react";

import SpaRenderer from "core/spa/spa-renderer";

import Network from "../../utils/network";

const Swagger = () => {
  const [namespaces, setNamespaces] = useState([]);
  const [selectedNamespace, setSelectedNamespace] = useState("");
  const [spec, setSpec] = useState(null);

  useEffect(() => {
    Network.get("/rhn/manager/api/openapi/namespaces")
      .then((data) => {
        setNamespaces(data);
        if (data.length > 0) {
          setSelectedNamespace(data[0]);
        }
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedNamespace) {
      Network.get(`/rhn/manager/api/openapi/${selectedNamespace}`)
        .then((data) => setSpec(data))
        .catch(() => {});
    }
  }, [selectedNamespace]);

  return (
    <div className="swaggerWrapper">
      <h1>{t("API Documentation")}</h1>

      <div className="namespaceSelector">
        <label className="control-label" htmlFor="namespace-select">
          {t("Select Namespace:")}
        </label>
        <select
          id="namespace-select"
          className="form-control"
          onChange={(e) => setSelectedNamespace(e.target.value)}
          value={selectedNamespace}
        >
          {namespaces.map((ns) => (
            <option key={ns} value={ns}>
              {ns}
            </option>
          ))}
        </select>
      </div>

      {spec ? <SwaggerUI spec={spec} /> : <div>{t("Loading API documentation...")}</div>}
    </div>
  );
};

export const renderer = (id: string) => SpaRenderer.renderNavigationReact(<Swagger />, document.getElementById(id));
