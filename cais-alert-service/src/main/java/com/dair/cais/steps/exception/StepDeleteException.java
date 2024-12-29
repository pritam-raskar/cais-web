package com.dair.cais.steps.exception;

/**
 * Exception thrown when a step cannot be deleted due to existing dependencies or constraints.
 */
public class StepDeleteException extends RuntimeException {
    private final int associatedWorkflowCount;

    /**
     * Constructs a new StepDeleteException with a specific error message and workflow count.
     *
     * @param message Detailed description of why the step deletion failed
     * @param associatedWorkflowCount Number of workflows associated with the step
     */
    public StepDeleteException(String message, int associatedWorkflowCount) {
        super(message);
        this.associatedWorkflowCount = associatedWorkflowCount;
    }

    /**
     * Constructs a new StepDeleteException with a specific error message, cause, and workflow count.
     *
     * @param message Detailed description of why the step deletion failed
     * @param cause The underlying cause of the deletion failure
     * @param associatedWorkflowCount Number of workflows associated with the step
     */
    public StepDeleteException(String message, Throwable cause, int associatedWorkflowCount) {
        super(message, cause);
        this.associatedWorkflowCount = associatedWorkflowCount;
    }

    /**
     * Get the number of workflows associated with the step.
     *
     * @return Number of associated workflows
     */
    public int getAssociatedWorkflowCount() {
        return associatedWorkflowCount;
    }
}