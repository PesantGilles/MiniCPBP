/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package minicpbp.state;

/**
 * Object that wraps an integer value
 * that can be saved and restored through
 * the {@link StateManager#saveState()} / {@link StateManager#restoreState()}
 * methods.
 *
 * @see StateManager#makeStateInt(int) for the creation.
 */
public interface StateInt {

    /**
     * Set the value
     * @param v the value to set
     * @return the new value that was set
     */
    int setValue(int v);

    /**
     * Retrieves the value
     * @return the value
     */
    int value();

    /**
     * Increments the value
     * @return the new value
     */
    int increment();

    /**
     * Decrements the value
     * @return the new value
     */
    int decrement();

    @Override
    String toString();
}
