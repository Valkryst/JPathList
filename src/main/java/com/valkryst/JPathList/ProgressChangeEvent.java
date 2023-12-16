package com.valkryst.JPathList;

import javax.swing.event.ChangeEvent;
import java.util.Objects;

public class ProgressChangeEvent extends ChangeEvent {
    /** Progress of the current operation. Value is between 0.0 and 100.0. */
    private final double progress;

    /**
     * Constructs a new {@code ProgressChangeEvent}.
     *
     * @param source Source of the event.
     * @param progress Progress of the current operation.
     *
     * @throws NullPointerException If {@code source} is {@code null}.
     * @throws IllegalArgumentException If {@code progress} is less than 0.0 or greater than 100.0.
     */
    public ProgressChangeEvent(final Object source, final double progress) {
        super(source);

        Objects.requireNonNull(source);

        if (progress < 0.0 || progress > 100.0) {
            throw new IllegalArgumentException("The progress value must be between 0.0 and 100.0.");
        }

        this.progress = progress;
    }

    /**
     * Retrieves the progress of the current operation.
     *
     * @return Progress of the current operation.
     */
    public double getProgress() {
        return progress;
    }
}
