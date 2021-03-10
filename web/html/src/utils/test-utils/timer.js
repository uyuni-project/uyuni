let timerId = 0;

/**
 * Utility class for debugging unit test performance issues.
 * Create an instance with `const timer = new Timer(isEnabled = true)` and then instert
 * measurement points with `timer.now(description)`.
 */
export class Timer {
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
        console.log(`[timer ${this.id}] ${description}: +${now - this.prev}ms (total: ${now - this.start}ms)`);
        this.prev = now;
    }
}
