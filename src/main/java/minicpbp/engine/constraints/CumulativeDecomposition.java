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


package minicpbp.engine.constraints;

import minicpbp.cp.Factory;
import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.IntVar;
import minicpbp.util.exception.NotImplementedException;

import java.util.Arrays;

import static minicpbp.cp.Factory.*;

/**
 * Cumulative constraint with sum decomposition (very slow).
 */
public class CumulativeDecomposition extends AbstractConstraint {

    private final IntVar[] start;
    private final int[] duration;
    private final IntVar[] end;
    private final int[] demand;
    private final int capa;

    /**
     * Creates a cumulative constraint with a decomposition into sum constraint.
     * At any time-point t, the sum of the demands
     * of the activities overlapping t do not overlap the capacity.
     *
     * @param start    the start time of each activities
     * @param duration the duration of each activities (non negative)
     * @param demand   the demand of each activities, non negative
     * @param capa     the capacity of the constraint
     */
    public CumulativeDecomposition(IntVar[] start, int[] duration, int[] demand, int capa) {
        super(start[0].getSolver(), start);
        setName("CumulativeDecomposition");
        this.start = start;
        this.duration = duration;
        this.end = Factory.makeIntVarArray(start.length, i -> plus(start[i], duration[i]));
        this.demand = demand;
        this.capa = capa;
    }

    @Override
    public void post() {

        int min = Arrays.stream(start).map(s -> s.min()).min(Integer::compare).get();
        int max = Arrays.stream(end).map(e -> e.max()).max(Integer::compare).get();

        for (int t = min; t < max; t++) {

            BoolVar[] overlaps = new BoolVar[start.length];
            for (int i = 0; i < start.length; i++) {
                overlaps[i] = makeBoolVar(getSolver());
                BoolVar startsBefore = makeBoolVar(getSolver());
                BoolVar endsAfter = makeBoolVar(getSolver());
                getSolver().post(new IsLessOrEqual(startsBefore, start[i], t));
                getSolver().post(new IsLessOrEqual(endsAfter, minus(plus(start[i], duration[i] - 1)), -t));
                final int capa = -2;
                // overlaps = endsAfter & startsBefore
                getSolver().post(new IsLessOrEqual(overlaps[i], minus(sum(new IntVar[]{startsBefore, endsAfter})), capa));
            }

            IntVar[] overlapHeights = Factory.makeIntVarArray(start.length, i -> mul(overlaps[i], demand[i]));
            IntVar cumHeight = sum(overlapHeights);
            cumHeight.removeAbove(capa);

        }

    }

}
