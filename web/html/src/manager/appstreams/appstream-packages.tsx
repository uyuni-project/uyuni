import { useEffect, useState } from "react";

import Network from "../../utils/network";

export const AppStreamPackages = ({ stream, channelId }) => {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    if (channelId && stream) {
      Network.get(`/rhn/manager/api/appstreams/${channelId}/${stream}/packages`)
        .then((data) => {
          setPackages(data.data.packages);
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
          setHasError(true);
        });
    }
  }, [channelId, stream]);

  if (loading) {
    return <p>{t("Loading...</p>")}</p>;
  }

  return hasError ? (
    <p>{t("Sorry, error loading packages")}</p>
  ) : (
    <>
      <h6>{t("AppStream {stream} has {packages.length} packages:")}</h6>
      <div style={{ maxHeight: "calc(100vh - 200px)" }}>
        {packages.map((packageName, index) => (
          <li key={index}>{packageName}</li>
        ))}
      </div>
    </>
  );
};
