import { IconTag } from "components/icontag";

export const DownloadCSVButton = ({ url, search }: { url: string; search?: { field?: string; criteria?: string } }) => {
  const finalUrl = search?.field && search?.criteria
    ? `${url}${url.includes("?") ? "&" : "?"}qc=${search.field}&q=${encodeURIComponent(search.criteria)}`
    : url;
  return (
    <a role="button" title={t("Download CSV")} href={finalUrl} className="btn btn-default" data-senna-off="true">
      <IconTag type="item-download-csv" />
      {t("Download CSV")}
    </a>
  );
};
