type Props = {
  percentage: number;
  compliant: number;
  total: number;
};

/**
 * Badge component to display compliance status with color coding
 * Green: >= 80%, Yellow: 50-79%, Red: < 50%
 */
export const ComplianceBadge = ({ percentage, compliant, total }: Props) => {
  if (total === 0) {
    return <span className="badge badge-default">{t("No Scans")}</span>;
  }

  const getColorClass = () => {
    if (percentage >= 80) return "badge-success";
    if (percentage >= 50) return "badge-warning";
    return "badge-danger";
  };

  return (
    <span className={`badge ${getColorClass()}`}>
      {compliant}/{total} ({percentage.toFixed(0)}%)
    </span>
  );
};
