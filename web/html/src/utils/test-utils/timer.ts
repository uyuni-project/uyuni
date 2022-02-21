import { localizedMoment } from "utils/datetime";

let timerId = 0;

/**
 * Utility class for debugging unit test performance issues.
 * Create an instance with `const timer = new Timer(isEnabled = true)` and then instert
 * measurement points with `timer.now(description)`.
 */
export class Timer {
  id: number;
  isEnabled: boolean;
  start: moment.Moment;
  prev: moment.Moment;

  constructor(isEnabled = true) {
    this.id = timerId++;
    this.isEnabled = isEnabled;
    this.start = localizedMoment();
    this.prev = this.start;
  }

  now(description) {
    if (!this.isEnabled) {
      return;
    }
    const now = localizedMoment();
    console.log(
      `[timer ${this.id}] ${description}: +${now.valueOf() - this.prev.valueOf()}ms (total: ${
        now.valueOf() - this.start.valueOf()
      }ms)`
    );
    this.prev = now;
  }
}
