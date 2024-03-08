import { useEffect, useState } from "react";

import Network from "../../utils/network";

export const ModulePackages = ({ nsvca, channelId }) => {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    if (channelId && nsvca) {
      Network.get(`/rhn/manager/api/appstreams/${channelId}/${nsvca}/packages`)
        .then((data) => {
          setPackages(data.data.packages);
          setLoading(false);
        })
        .catch((error) => {
          setLoading(false);
          setHasError(true);
        });
    }
  }, [channelId, nsvca]);

  if (loading) {
    return <p>Loading...</p>;
  }

  return hasError ? (
    <p>Sorry, error loading packages</p>
  ) : (
    <>
      <h6>
        Module {nsvca} has {packages.length} packages:
      </h6>
      <div style={{ maxHeight: "calc(100vh - 200px)" }}>
        {packages.map((packageName, index) => (
          <li key={index}>{packageName}</li>
        ))}
      </div>
    </>
  );
};
