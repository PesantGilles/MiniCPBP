package model;

public enum ModelGoal {
    MAX,
    MIN,
    SAT;

    /**
     *
     * @return true if the problem is a COP, false if the problem is a CSP
     */
    public boolean isCOP() {
        return this == MAX || this == MIN;
    }
}
