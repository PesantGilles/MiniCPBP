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
 *
 * mini-cpbp, replacing classic propagation by belief propagation
 * Copyright (c)  2019. by Gilles Pesant
 */

package minicpbp.engine.constraints;

import minicpbp.engine.core.AbstractConstraint;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.Solver;
import minicpbp.util.exception.InconsistencyException;
import minicpbp.util.exception.NotImplementedException;

import minicpbp.util.binpacking.densities.LoadSolutionDensitiesSolver;
import minicpbp.util.binpacking.network.Network;
import minicpbp.util.binpacking.packing.PackingCounter;
import minicpbp.util.binpacking.packing.LayerProfile;

import static minicpbp.cp.Factory.sum;
import static minicpbp.cp.Factory.isEqual;

/**
 * Bin Packing constraint with basic filtering (rewritten as sums)
 */
public class BinPacking extends AbstractConstraint {

    private final IntVar[] b;
    private final int[] size;
    private final IntVar[] l;
    private final int capa;
    private final int n;
    private final int m;
    private Solver cp;

    /**
     * Creates a bin packing constraint rewritten as sum constraints.
     * But includes a dedicated weighted counting algorithm for updateBelief()
     *
     * @param b    the bin into which each item is put
     * @param size the size of each item
     * @param l    the load of each bin
     * @param capa the capacity of the bins
     * @param vars = b followed by l, variables for which we compute beliefs (passed to AbstractConstraint)
     */

    public BinPacking(IntVar[] b, int[] size, IntVar[] l, int capa, IntVar[] vars) {
        super(b[0].getSolver(), vars);
        setName("BinPacking");
        this.b = b;
        this.size = size;
        this.l = l;
        this.capa = capa;
	    n = b.length;
	    m = l.length;
	    cp = b[0].getSolver();
	    assert (size.length == n);
        setExactWCounting(false);
    }

    @Override
    public void post() {
	    // restrict the capacity of bins
        for (int j = 0; j < m; j++) {
            l[j].removeAbove(capa);
        }
	    // equate the load of each bin to the sum of the sizes of the items in it
        for (int j = 0; j < m; j++) {
	        BoolVar[] inBin = new BoolVar[n];
	        for (int i = 0; i < n; i++) {
		        inBin[i] = isEqual(b[i],j);
	        }
	        cp.post(sum(size,inBin,l[j]));
	    }
	    // redundant constraint: sum of loads = sum of sizes
	    int total = 0;
        for (int i = 0; i < size.length; i++) {
	        total += size[i];
	    }
	    cp.post(sum(l,total));
    }

    @Override
    public void updateBelief() {
        // TODO exclude items whose bin assignment var is bound and restrict final layer profiles accordingly

        // Build flow network
        Network network = new Network(size, capa);
//         System.out.println(network);

        // Generate layer profiles to count packings
        PackingCounter packingCounter = new PackingCounter(network, m);

        System.out.println("Number of packings: " + packingCounter.getPackingCount());

	/*
        System.out.println("Flow profiles and multiplicities (number of packings for each):");
        for (Map.Entry<LayerProfile, Long> entry : packingCounter.getFlowProfiles().entrySet()) {
            System.out.println("    " + entry.getKey() + " (x" + entry.getValue() + ")");
        }

        // Generate solution densities for load variables
        List<Set<Integer>> loadVariableDomains = new ArrayList<>() {{
            add(new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7)));
            add(new HashSet<>(Arrays.asList(5, 6, 7)));
            add(new HashSet<>(Arrays.asList(7)));
        }};
        LoadSolutionDensitiesSolver loadSolutionDensitiesSolver = new LoadSolutionDensitiesSolver(
            packingCounter.getFlowProfiles(),
            network.getLayers().get(network.getLayers().size() - 1),
            loadVariableDomains
        );
        List<Map<Integer, Float>> loadSolutionDensities = loadSolutionDensitiesSolver.getSolutionDensities();
        System.out.println("Load variable solution densities: " + loadSolutionDensities);
        System.out.println("Number of bin assignment solutions: " + loadSolutionDensitiesSolver.getSolutionCount());
	*/
    }

}
