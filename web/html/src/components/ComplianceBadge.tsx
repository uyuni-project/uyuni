import { Badge } from "./badge/Badge";
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
    return <Badge text={t("No Scans")} />;
  }

  const getColorClass = () => {
    if (percentage >= 80) return "success";
    if (percentage >= 50) return "warning";
    return "error";
  };

  const badgeText = `${compliant}/${total} (${percentage.toFixed(0)}%)`;
  return <Badge text={badgeText} color={getColorClass()} />;
};
