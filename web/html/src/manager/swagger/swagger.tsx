import "swagger-ui-react/swagger-ui.css";

import { useEffect, useState } from "react";

import SwaggerUI from "swagger-ui-react";

import SpaRenderer from "core/spa/spa-renderer";

import Network from "../../utils/network";

const Swagger = (props) => {
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
      .catch((error) => console.error("Error fetching namespaces:", error));
  }, []);

  useEffect(() => {
    if (selectedNamespace) {
      Network.get(`/rhn/manager/api/openapi/${selectedNamespace}`)
        .then((data) => setSpec(data))
        .catch((error) => console.error("Error fetching OpenAPI spec:", error));
    }
  }, [selectedNamespace]);

  return (
    <div>
      <h1>API Documentation</h1>
      <div>
        <label htmlFor="namespace-select">Select Namespace:</label>
        <select id="namespace-select" onChange={(e) => setSelectedNamespace(e.target.value)} value={selectedNamespace}>
          {namespaces.map((ns) => (
            <option key={ns} value={ns}>
              {ns}
            </option>
          ))}
        </select>
      </div>
      {spec ? <SwaggerUI spec={spec} /> : <div>Loading API documentation...</div>}
    </div>
  );
};

export const renderer = (id: string, props) =>
  SpaRenderer.renderNavigationReact(<Swagger props={props} />, document.getElementById(id));
