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
import minicpbp.engine.core.Constraint;
import minicpbp.engine.core.Solver;
import minicpbp.engine.core.IntVar;
import minicpbp.state.StateManager;
import minicpbp.state.StateInt;
import minicpbp.state.StateBool;
import minicpbp.util.exception.InconsistencyException;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;
import java.security.InvalidParameterException;

/**
 * System of linear inequalities in modular arithmetic: Ax <= b (mod p)
 * Requires p to be prime, thus defining a finite field F_p
 */
public class LinIneqSystemModP extends AbstractConstraint {
    private int[][] A;
    private IntVar[] x;
    private int p; // the prime modulus
    private int m, n, nNonparam, nDisjuncts;
    private int[] inverse; // multiplicative inverse (reciprocal) of each nonzero element of F_p
    private int[] unBounds;
	private int[] colIdx;
    private StateInt nUnBounds;
    private static final int maxNbTuples = 100000; // size of allocated data structures for table constraint
    private static final double likelihoodThreshold = 0.5; // apply DC when likelihood of non-parametric variables supporting combination of parametric values < threshold
    private int nTuplesThreshold = 100; // apply DC when parametric vars search space <= threshold
    private StateBool tableFiltering; // true iff we now apply DC (once the threshold has been reached)
    private int[] tuple; // to build tuples during the tuple enumeration
    private int[][] Tuples; // to accumulate tuples
    private int nTuples; // number of tuples
    //
    // data structures borrowed from TableCT implementation
    //
    private int[] ofs; //offsets for each variable's domain
    //supports[i][v] is the set of tuples supported by x[i]=v
    private BitSet[][] supports;
    //supportedTuples is the set of tuples supported by the current domains of the variables
    private BitSet supportedTuples;
    private BitSet supporti;

    /**
     * Creates a "system of linear inequalities modulo p" constraint, with p a prime number.
     * <p> This constraint holds iff
     * {@code Ax <= b (mod p)}.
     *
     * @param A the mxn matrix of coefficients
     * @param x the column vector of n variables
     * @param b the column vector of m rhs values
     * @param p the prime modulus
     */
    public LinIneqSystemModP(int[][] A, IntVar[] x, int[] b, int p) {
        super(x[0].getSolver(), x);
	setName("LinIneqSystemModP");
	assert( nTuplesThreshold <= maxNbTuples );
	n = x.length;
	m = b.length;
        this.x = x;
	this.p = p;
	assert( p > 1 );
	assert( A.length == m );
	StateManager sm = getSolver().getStateManager();
        tableFiltering = sm.makeStateBool(false);

	// precompute the reciprocals used in the Extended Gauss-Jordan Elimination algorithm (also allows us to verify that p is prime)
	inverse = new int[p];
	inverse[1] = 1;
	inverse[p-1] = p-1;
	for (int i=2; i<p-1; i++)
	    inverse[i] = -1;
	for (int i=2; i<p-1; i++)
	    if (inverse[i] == -1) {
		inverse[i] = reciprocal(i); // every element of the field (except 0) has a multiplicative inverse
		inverse[inverse[i]] = i;
	    }

	Solver cp = getSolver();
        unBounds = IntStream.range(0, n).toArray();

        // Collect bound vars
        int nU = n;
        for (int i = nU - 1; i >= 0; i--) {
            int idx = unBounds[i];
			if (x[idx].isBound()) {
               unBounds[i] = unBounds[nU - 1]; // Swap the variables
                unBounds[nU - 1] = idx;
                nU--;
            }
        }
		// initialize column indices for matrix A, which will be synchronized with changes to unBounds
		colIdx = IntStream.range(0, nU).toArray();

		nUnBounds = cp.getStateManager().makeStateInt(nU);

	// map all constants to their canonical representative (from the set {0,1,...,p-1}) in the congruence relation's equivalence class of finite field F_p, while taking into account bound variables
	// Whereas a system of equalities has one column as rhs, our system will have \Pi_{0 \leq i < m}(b[i]+1) columns, each representing one of a disjunction of systems of linear equalities
	nDisjuncts = 1;
	for (int i=0; i<m; i++) {
	    nDisjuncts *= Math.floorMod(b[i],p)+1;
	}
// 	System.out.println("nb of disjuncts: "+nDisjuncts);
	nTuplesThreshold = Math.max(nTuplesThreshold,nDisjuncts); // to ensure that a table constraint is eventually posted
	this.A = new int[m][nU+nDisjuncts]; // augmented coefficient matrix
	int nConsec = nDisjuncts; // to fill disjuncts on rhs
	int period = 1; // to fill disjuncts on rhs
	for (int i=0; i<m; i++) {
	    assert( A[i].length == n );
	    // fill rhs
	    nConsec /= b[i]+1;
	    for (int l=0; l<period; l++) {
		for (int k=0; k<=b[i]; k++) {
		    for (int j=0; j<nConsec; j++) {
			this.A[i][nU+l*nConsec*(b[i]+1)+k*nConsec+j] = k;
		    }
		}
	    }
	    period *= b[i]+1;
 	    for (int j=0; j<nU; j++) {
		this.A[i][j] = Math.floorMod(A[i][unBounds[j]],p);
	    }
	    for (int j=nU; j<n; j++) { // subtract terms of bound variables from rhs
		for (int k=0; k<nDisjuncts; k++) {
		    this.A[i][nU+k] -= A[i][unBounds[j]]*x[unBounds[j]].min();
		}
	    }
	    for (int k=0; k<nDisjuncts; k++) {
		this.A[i][nU+k] = Math.floorMod(this.A[i][nU+k],p);
	    }
	}

	/*
   	System.out.println("before GJ Elim");
  	for (int i=0; i<m; i++) {
  	    System.out.println(Arrays.toString(this.A[i]));
  	}
	*/

	// put A in reduced row echelon form
	GaussJordanElimination(m,nU,nDisjuncts);

	/*
   	System.out.println("after GJ Elim");
  	for (int i=0; i<m; i++) {
  	    System.out.println(Arrays.toString(this.A[i]));
  	}
	*/

	// consider potential rows with all zero coefficients
	// TODO remove disjuncts with nonzero rhs
	/*
	for (int i=nNonparam; i<m; i++)
	    if (this.A[i][nU] != 0) // rhs must be zero as well in order for the system to be satisfiable
		throw new InconsistencyException();
	*/

	tuple = new int[nU];
	Tuples = new int[maxNbTuples][nU];
	ofs = new int[nU];
	supportedTuples = new BitSet(maxNbTuples);
	supporti = new BitSet(maxNbTuples);
        // Allocate supportedByVarVal
        supports = new BitSet[nU][];
	int maxDsize = 0;
        for (int i = 0; i < nU; i++) {
	    if (x[unBounds[i]].max() - x[unBounds[i]].min() + 1 > maxDsize)
		maxDsize = x[unBounds[i]].max() - x[unBounds[i]].min() + 1;
	}
        for (int i = 0; i < nU; i++) {
	    supports[i] = new BitSet[maxDsize];
            for (int j = 0; j < supports[i].length; j++)
                supports[i][j] = new BitSet();
        }

/*
 	System.out.print("\n nonparam vars: ");
	for (int j=0; j<nNonparam; j++) {
	    System.out.print(x[unBounds[j]].getName()+" ");
	}
	System.out.println();
*/

//    	setExactWCounting(true);
    }

    /**
     * Computes the multiplicative inverse of nonzero element e in finite field F_p (extended GCD algorithm)
     *
     * @param e the element
     * @return the multiplicative inverse of e
     */
    private int reciprocal(int e) {
	assert( e>0 && e<p );
	int r = p;
	int newr = e;
	int t = 0;
	int newt = 1;
	while (newr != 0) {
	    int tmp1 = r % newr;
	    int tmp2 = t - r / newr * newt;
	    r = newr;
	    newr = tmp1;
	    t = newt;
	    newt = tmp2;
	}
	if (r == 1)
	    return (t<0 ? t+p : t);
	else
	    throw new InvalidParameterException("Modulus p="+p+" is not prime");
    }

    /**
     * Performs Gauss-Jordan Elimination on the disjunction of d mxn systems of linear equations in order to simplify it into reduced row echelon form.
     * Note: all elements of A are assumed to lie in the range 0..p-1
     *
     */
    private void GaussJordanElimination(int m, int n, int d) {
	int h = 0; // pivot row 
	int k = 0; // pivot column
	while ( (h < m) && (k < n) ) {
	    // Find the k-th pivot
	    int pivotCol = k;
	    while (pivotCol < n) {
		int pivotRow = h;
		while ( (pivotRow < m) && (A[pivotRow][pivotCol] == 0) )
		    pivotRow++;
		if (pivotRow < m) {
		    if (pivotRow > h)
			swapRows(h,pivotRow);
		    break;
		} else
		    pivotCol++; // no pivot in this column, go to next one
	    }
	    if (pivotCol == n)
		break; // end of procedure
	    if (pivotCol > k)
		swapCols(k,pivotCol);
	    // transform pivot row to lead with a unit coefficient
	    int inv = inverse[A[h][k]];
	    A[h][k] = 1;
	    for (int j=k+1; j<n+d; j++) {
		A[h][j] = Math.floorMod( A[h][j] * inv, p );
	    }
	    for (int i=h+1; i<m; i++) { // for all rows below pivot
		int f = A[i][k];
		A[i][k] = 0; // element in same column as pivot is set to 0
		for (int j=k+1; j<n+d; j++) { // transform the other elements of that row
		    A[i][j] = Math.floorMod( A[i][j] - A[h][j] * f, p );
		}
	    }
	    for (int i=0; i<h; i++) { // and for all rows above pivot
		int f = A[i][k];
		A[i][k] = 0; // element in same column as pivot is set to 0
		for (int j=k+1; j<n+d; j++) { // transform the other elements of that row
		    A[i][j] = Math.floorMod( A[i][j] - A[h][j] * f, p );
		}
	    }
	    h++;
	    k++;
	}
	nNonparam = k;
    }

    private void swapRows(int i, int j) {
	int[] tmp = A[j];
	A[j] = A[i];
	A[i] = tmp;
    }
    
    private void swapCols(int i, int j) {
	for (int k=0; k<m; k++) {
	    int tmp = A[k][j];
	    A[k][j] = A[k][i];
	    A[k][i] = tmp;
	}
	int tmp = unBounds[j];
	unBounds[j] = unBounds[i];
	unBounds[i] = tmp;
		tmp = colIdx[j];
		colIdx[j] = colIdx[i];
		colIdx[i] = tmp;
    }

    @Override
    public void post() {
	switch(getSolver().getMode()) {
	case BP:
	    break;
	case SP:
	case SBP:
	    // Since all variables are used to branch on (not just parametric ones), track all of them
 	    for (int j = 0; j < nUnBounds.value(); j++) {
   		x[unBounds[j]].propagateOnBind(this);
	    }
	}
  	propagate();
    }

    @Override
    public void propagate() {
	if (!tableFiltering.value()) { 
	    // so far table filtering has not been activated
	    int nU = nUnBounds.value();

	    // compute the size of the Cartesian product of the non-parametric vars' domains
	    int nonparamSpace = 1;
	    for (int i = 0; i < nNonparam; i++) { 
		nonparamSpace *= x[unBounds[i]].size();
	    }

	    // compute the likelihood that the non-parametric variables support a given combination of parametric values
	    double likelihood = ((double) nDisjuncts * nonparamSpace) / Math.pow(p,nNonparam);
//    	    System.out.println("likelihood = "+likelihood);

 	    double nTuplesUB = Math.min(nDisjuncts,nonparamSpace); // upper bound on nb combinations of values for parametric vars
	    for (int i = nU - 1; i >= nNonparam; i--) { // restrict to parametric vars
		int idx = unBounds[i];
		if (x[idx].isBound()) {
		    unBounds[i] = unBounds[nU - 1]; // Swap the variables
		    unBounds[nU - 1] = idx;
			int tmp = colIdx[i];
			colIdx[i] = colIdx[nU - 1]; // swap column indices (keep synchronized)
			colIdx[nU - 1] = tmp;
		    nU--;
		}
		else {
		    nTuplesUB *= x[idx].size();
		}
	    }
	    nUnBounds.setValue(nU); // Warning: this count ignores bound _non_parametric variables
	    // don't risk exceeding the allocated space
 	    if (nTuplesUB > maxNbTuples)
		return;

	    // decide whether or not we should post a table constraint
 	    if ((likelihood > likelihoodThreshold) && (nTuplesUB > nTuplesThreshold))
		return;

	    // otherwise it is now time to set up table filtering
	    tableFiltering.setValue(true);
     /*
	    // From now on also track bound non-parametric vars
	    for (int j = 0; j < nNonparam; j++) {
  		x[unBounds[j]].propagateOnBind(this);
	    }
     */
	    // TODO? switch to domain events for all vars
	    // TODO: map domain values to their canonical rep

 //	    System.out.println("\n enumerating tuples:");
 //	    System.out.println("posting Table with "+nU+" unbounds");
	    // enumerate tuples over parametric variables and accumulate them in Tuples
	    nTuples = 0;
	    paramEnum(1);
//     	    System.out.println(nTuples+" tuples whereas the upper bound is "+nTuplesUB+"; "+nU+" unbound vars");
	    if (nTuples==0)
		throw new InconsistencyException();	    
	    for (int i = 0; i < nU; i++) {
		ofs[i] = x[unBounds[i]].min(); // offsets map the variables' domain to start at 0 for supports[][]
	    }
	    // Clear supports
	    for (int j = 0; j < nU; j++) {
		for (int k = 0; k < supports[j].length; k++) {
		    supports[j][k].clear();
		}
	    }
	    // Set values in supportedByVarVal, which contains all the tuples supported by each var-val pair
	    for (int i = 0; i < nTuples; i++) { //i is the index of the tuple (in table)
		for (int j = 0; j < nU; j++) { //j is the index of the current variable (in x)
		    if (x[unBounds[j]].contains(Tuples[i][j])) {
			supports[j][Tuples[i][j] - ofs[j]].set(i);
		    }
		}
	    }
	}
	// perform table filtering (borrowed from TableCT.propagate())
// 	System.out.println("filtering...");
	supportedTuples.set(0, nTuples); // set them all to true
	if (supportedTuples.length() > nTuples)
	    supportedTuples.clear(nTuples, supportedTuples.length()); // disregard tuples from former table
	int nU = nUnBounds.value(); // WARNING!! assumes no updates to unBounds have been done since tableFiltering was set
	for (int i = 0; i < nU; i++) {
	    supporti.clear(); // set them all to false
	    int s = x[unBounds[i]].fillArray(domainValues);
// 	    System.out.println("building supporti for "+x[unBounds[i]].getName());
	    for (int j = 0; j < s; j++) {
// 		System.out.println("val "+domainValues[j]+": "+Arrays.toString(supports[i][domainValues[j]-ofs[i]].stream().toArray()));
		supporti.or(supports[i][domainValues[j]-ofs[i]]);
	    }
	    supportedTuples.and(supporti);
	}
// 	System.out.println("currently supported tuples are: "+Arrays.toString(supportedTuples.stream().toArray()));
	if (supportedTuples.isEmpty())
	    throw new InconsistencyException();	    
	for (int i = 0; i < nU; i++) {
	    int s = x[unBounds[i]].fillArray(domainValues);
// 	    System.out.println("considering "+x[unBounds[i]].getName()+x[unBounds[i]].toString());
	    for (int j = 0; j < s; j++) {
		// The condition for removing the setValue v from x[i] is to check if
		// there is no intersection between supportedTuples and the support[i][v]
		int v = domainValues[j];
		if (!supports[i][v-ofs[i]].intersects(supportedTuples)) {
//  		    System.out.println("removing "+v+" for "+x[unBounds[i]].getName()+x[unBounds[i]].toString());
		    x[unBounds[i]].remove(v);
		}
	    }
	}
    }

    /**
     * Recursive algorithm enumerating tuples over unbound parametric variables
     *
     * @param r the rank of the next parametric variable to enumerate over
     */
    private void paramEnum(int r) {
	if (r <= nUnBounds.value()-nNonparam) {
	    int idx = nNonparam+r-1;
	    for (int v = 0; v < p; v++) 
		if (x[unBounds[idx]].contains(v)) {
		    tuple[idx] = v;
		    paramEnum(r+1);
		}
	}
	else { // complete tuple: check consistency using nonparametric variables
	    for (int k = 0; k < nDisjuncts; k++) {
		int i;
		for (i = 0; i < nNonparam; i++) {
		    int sum = A[i][A[i].length-nDisjuncts+k]; // rhs
 // 		    System.out.print("for nonparam "+x[unBounds[i]].getName()+": "+sum);
		    for (int j = nNonparam; j < nUnBounds.value(); j++) { // unbound parametric vars
				sum -= A[i][colIdx[j]]*tuple[j];
 //		        System.out.print("-"+A[i][colIdx[j]]+"*"+tuple[j]+"("+x[unBounds[j]].getName()+")");
		    }
		    for (int j = nUnBounds.value(); j < A[i].length-nDisjuncts; j++) { // bound parametric vars
			assert( x[unBounds[j]].isBound() );
			sum -= A[i][colIdx[j]]*x[unBounds[j]].min();
 // 		        System.out.print("--"+A[i][colIdx[j]]+"*"+x[unBounds[j]].min()+"("+x[unBounds[j]].getName()+")");
		    }
		    sum = Math.floorMod( sum, p );
 // 		    System.out.println();
		    if (!x[unBounds[i]].contains(sum))
			break;
		    tuple[i] = sum;
		}
		if (i == nNonparam) { // tuple is consistent: add to Tuples
		    for (int j = 0; j < nUnBounds.value(); j++) {
			Tuples[nTuples][j] = tuple[j];
//    		        System.out.print(tuple[j]+" ");
		    }
//    		    System.out.println("   index:"+nTuples);
//     		    System.out.println();
		    nTuples++;
		}
	    }
	}
    }
}
