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
import minicpbp.engine.constraints.Profile.Rectangle;
import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;

import java.util.ArrayList;

import static minicpbp.cp.Factory.minus;
import static minicpbp.cp.Factory.plus;

/**
 * Cumulative constraint with time-table filtering
 */
public class Cumulative extends AbstractConstraint {

    private final IntVar[] start;
    private final int[] duration;
    private final IntVar[] end;
    private final int[] demand;
    private final int capa;
    private final boolean postMirror;


    /**
     * Creates a cumulative constraint with a time-table filtering.
     * At any time-point t, the sum of the demands
     * of the activities overlapping t do not overlap the capacity.
     *
     * @param start    the start time of each activities
     * @param duration the duration of each activities (non negative)
     * @param demand   the demand of each activities, non negative
     * @param capa     the capacity of the constraint
     */
    public Cumulative(IntVar[] start, int[] duration, int[] demand, int capa) {
        this(start, duration, demand, capa, true);
    }

    private Cumulative(IntVar[] start, int[] duration, int[] demand, int capa, boolean postMirror) {
        super(start[0].getSolver(), start);
        setName("Cumulative");
        this.start = start;
        this.duration = duration;
        this.end = Factory.makeIntVarArray(start.length, i -> plus(start[i], duration[i]));
        this.demand = demand;
        this.capa = capa;
        this.postMirror = postMirror;
    }


    @Override
    public void post() {
        for (int i = 0; i < start.length; i++) {
            start[i].propagateOnBoundChange(this);
        }

        if (postMirror) {
            IntVar[] startMirror = Factory.makeIntVarArray(start.length, i -> minus(end[i]));
            getSolver().post(new Cumulative(startMirror, duration, demand, capa, false), false);
        }

        propagate();
    }

    @Override
    public void propagate() {
        Profile profile = buildProfile();
        for (int i = 0; i < profile.size(); i++) {
            if (profile.get(i).height() > capa) {
                throw InconsistencyException.INCONSISTENCY;
            }
        }

        for (int i = 0; i < start.length; i++) {
            if (!start[i].isBound()) {
                // j is the index of the profile rectangle overlapping t
                int j = profile.rectangleIndex(start[i].min());
                int t = start[i].min();
                while (j < profile.size()
                        && profile.get(j).start() < Math.min(t + duration[i], start[i].max())) {
                    if (capa - demand[i]
                            <  profile.get(j).height()) {
                        t = Math.min(profile.get(j).end(), start[i].max());
                    }
                    j++;
                }
                start[i].removeBelow(t);
            }
        }
    }

    public Profile buildProfile() {
        ArrayList<Rectangle> mandatoryParts = new ArrayList<Rectangle>();
        for (int i = 0; i < start.length; i++) {
            if (end[i].min() > start[i].max()) {
                int s = start[i].max();
                int e = end[i].min();
                int d = demand[i];
                mandatoryParts.add(new Rectangle(s, e, d));
            }
        }
        return new Profile(mandatoryParts.toArray(new Profile.Rectangle[0]));
    }

}
