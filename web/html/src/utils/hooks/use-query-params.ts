import { useEffect, useState } from "react";

export const useQueryParams = () => {
  const getParams = () => {
    const params: Partial<Record<string, string>> = {};
    const urlParams = new URLSearchParams(window.location.search);

    for (const [key, value] of urlParams.entries()) {
      params[key] = value;
    }

    return params;
  };

  const [queryParams, setQueryParams] = useState(getParams());

  useEffect(() => {
    const onPopState = () => setQueryParams(getParams());

    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  return queryParams;
};
