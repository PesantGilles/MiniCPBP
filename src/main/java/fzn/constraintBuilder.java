package fzn;

import minicpbp.cp.Factory;
import minicpbp.engine.core.Constraint;
import minicpbp.engine.core.BoolVar;
import minicpbp.engine.core.IntVar;
import minicpbp.util.exception.NotImplementedException;


import static minicpbp.cp.Factory.*;

public class constraintBuilder {
    public static void makeIntLinNe(int[] as, IntVar[] bs, int c) {
        Factory.sum(as, bs).remove(c);
    }

    public static void makeIntLinNeReif(int[] as, IntVar[] bs, int c, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeIntLinEq(int[] as, IntVar[] bs, int c) {
        Factory.sum(as, bs).assign(c);
    }

    public static void makeIntLinEqReif(int[] as, IntVar[] bs, int c, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeIntLinLe(int[] as, IntVar[] bs, int c) {
        Factory.sum(as,bs).removeAbove(c);
    }

    public static void makeIntLinLeReif(int[] as, IntVar[] bs, int c, BoolVar r) {
        throw new NotImplementedException();
    }

    public static Constraint makeIntEq(IntVar a, IntVar b) {
        return Factory.equal(a, b);
    }

    public static void makeIntEqReif(IntVar a, IntVar b, BoolVar r) {
        throw new NotImplementedException();
    }

    public static Constraint makeIntNe(IntVar a, IntVar b) {
        return Factory.notEqual(a, b);
    }

    public static void makeIntNeReif(IntVar a, IntVar b, BoolVar r) {
        throw new NotImplementedException();
    }

    public static Constraint makeIntLe(IntVar a, IntVar b) {
        return Factory.lessOrEqual(a, b);
    }

    public static void makeIntLeReif(IntVar a, IntVar b, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeIntLt(IntVar a, IntVar b) {
        throw new NotImplementedException();
    }

    public static void makeIntLtReif(IntVar a, IntVar b, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeBoolEq(BoolVar a, BoolVar b) {
        throw new NotImplementedException();
    }

    public static void makeBoolLt(BoolVar a, BoolVar b) {
        throw new NotImplementedException();
    }

    public static void makeBoolLe(BoolVar a, BoolVar b) {
        throw new NotImplementedException();
    }

    public static void makeBoolNot(BoolVar a, BoolVar b) {
        throw new NotImplementedException();
    }    

    public static void makeBoolXor(BoolVar a, BoolVar b) {
        throw new NotImplementedException();
    }

    public static void makeArrayBoolOr(BoolVar[] as, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeArrayBoolXor(BoolVar[] as, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeArrayBoolAnd(BoolVar[] as, BoolVar r) {
        throw new NotImplementedException();
    }

    public static void makeBoolClause(BoolVar[] as, BoolVar[] bs) {
        throw new NotImplementedException();
    }

    public static void makeBool2Int(BoolVar a, IntVar b) {
        throw new NotImplementedException();
    }

    public static Constraint makeArrayIntElement(IntVar b, int[] as, IntVar c) {
        return Factory.equal(Factory.element(as, b), c);
    }

    public static void makeArrayVarIntElement(IntVar b, IntVar[] as, IntVar c) {
        throw new NotImplementedException();
    }

    public static void makeArrayBoolElement(IntVar b, boolean[] as, BoolVar c) {
        throw new NotImplementedException();
    }

    public static void makeArrayBoolElement(IntVar b, BoolVar[] as, BoolVar c) {
        throw new NotImplementedException();
    }

    public static void makeCount() {
        throw new NotImplementedException();
    }

    public static Constraint makeAllDifferentInt(IntVar[] a) {
        return Factory.allDifferent(a);
    }

    public static Constraint makeIntPlus(IntVar a, IntVar b, IntVar c) {
        return Factory.equal(Factory.sum(a,b), c);
    }

    public static Constraint makeIntMax(IntVar a, IntVar b, IntVar c) {
        return Factory.equal(maximum(a,b), c);
    }
    public static Constraint makeIntMin(IntVar a, IntVar b, IntVar c) {
        return Factory.equal(minimum(a,b), c);
    }

    public static Constraint makeIntAbs(IntVar a, IntVar b) {
        return Factory.equal(Factory.abs(a), b);
    }

    public static Constraint makeMaximumInt(IntVar m, IntVar[] x) {
        return Factory.equal(m, Factory.maximum(x));
    }
    public static Constraint makeMinimumInt(IntVar m, IntVar[] x) {
        return Factory.equal(m, Factory.minimum(x));
    }

    public static Constraint makeExactlyInt(int n, IntVar[] x, int v) {
        return Factory.exactly(x, v, n);
    }

    public static Constraint makeAtLeastInt(int n, IntVar[] x, int v) {
        return Factory.atleast(x, v, n);
    }

    public static Constraint makeAtMostInt(int n, IntVar[] x, int v) {
        return Factory.atmost(x, v, n);
    }

    public static Constraint makeGlobalCardinality(IntVar[] x, int[] cover, IntVar[] counts) {
        return Factory.cardinality(x, cover, counts);
    }

    public static Constraint makeGlobalCardinalityLowUp(IntVar[] x, int[] cover, int[] lbound, int[] ubound) {
        return Factory.cardinality(x, cover, lbound, ubound);
    }

}
