import { Notification, Severity } from "./types";

// Comparators used for column sorting
export class NotificationComparators {
  private static readonly severityValues: Record<Severity, number>;

  private static readonly statusValues = { true: 0, false: 1 };

  private constructor() {
    // Prevent instantiation
  }

  public static sortBySeverity(n1: Notification, n2: Notification, _key: string, direction: number): number {
    const a = NotificationComparators.severityValues[n1.severity];
    const b = NotificationComparators.severityValues[n2.severity];

    return Math.sign(a - b) * direction;
  }

  public static sortByStatus(n1: Notification, n2: Notification, _key: string, direction: number): number {
    const a = NotificationComparators.statusValues[n1.read.toString()];
    const b = NotificationComparators.statusValues[n2.read.toString()];

    return Math.sign(a - b) * direction;
  }

  public static sortByType(typeMap: Map<string, string>) {
    return (n1: Notification, n2: Notification, _key: string, direction: number): number => {
      const a = typeMap.get(n1.type)?.toLowerCase() ?? "";
      const b = typeMap.get(n2.type)?.toLowerCase() ?? "";

      return a.localeCompare(b) * direction;
    };
  }
}
