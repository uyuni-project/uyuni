let timerId = 0;

/**
 * Utility class for debugging unit test performance issues.
 * Create an instance with `const timer = new Timer(isEnabled = true)` and then instert
 * measurement points with `timer.now(description)`.
 */
export class Timer {
  id: number;
  isEnabled: boolean;
  start: Date;
  prev: Date;

  constructor(isEnabled = true) {
    this.id = timerId++;
    this.isEnabled = isEnabled;
    this.start = new Date();
    this.prev = this.start;
  }

  now(description) {
    if (!this.isEnabled) {
      return;
    }
    const now = new Date();
    console.log(
      `[timer ${this.id}] ${description}: +${now.getTime() - this.prev.getTime()}ms (total: ${
        now.getTime() - this.start.getTime()
      }ms)`
    );
    this.prev = now;
  }
}
