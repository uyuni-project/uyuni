/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

/**
 * result of a module.run in a state file
 * @param <R> inner changes type
 */
public class ModuleRun<R> {

    private final R changes;
    private final String comment;
    private final boolean result;

    /**
     * constructor
     *
     * @param changesIn changes
     * @param commentIn comment
     * @param resultIn result
     */
    public ModuleRun(R changesIn, String commentIn, boolean resultIn) {
        this.changes = changesIn;
        this.comment = commentIn;
        this.result = resultIn;
    }

    /**
     * get changes
     * @return changes
     */
    public R getChanges() {
        return changes;
    }

    /**
     * get comment
     * @return comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * get result
     * @return result
     */
    public boolean isResult() {
        return result;
    }
}
