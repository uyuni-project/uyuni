import { useEffect, useState } from "react";

import { PackageLink } from "components/links";

import Network from "../../utils/network";

interface Package {
  id: number;
  nevra: string;
}

export const AppStreamPackages = ({ stream, channelId }) => {
  const [packages, setPackages] = useState<Package[]>([]);
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
    return <p>{t("Loading...")}</p>;
  }

  return hasError ? (
    <p>{t("An error occurred while displaying the list of packages.")}</p>
  ) : (
    <>
      <h6>{t("AppStream {stream} has {count} packages:", { stream: stream, count: packages.length })}</h6>
      <div style={{ maxHeight: "calc(100vh - 200px)" }}>
        {packages.map((pkg, index) => (
          <li key={`pkg-${pkg.id}`}>
            <PackageLink id={pkg.id} newWindow={true}>
              {pkg.nevra}
            </PackageLink>
          </li>
        ))}
      </div>
    </>
  );
};
