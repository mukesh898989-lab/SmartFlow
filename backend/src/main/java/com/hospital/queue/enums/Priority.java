package com.hospital.queue.enums;

/**
 * Priority levels for queue ordering.
 * Lower order value = higher priority in queue.
 */
public enum Priority {
    EMERGENCY(1),
    URGENT(2),
    NORMAL(3);

    private final int order;

    Priority(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
