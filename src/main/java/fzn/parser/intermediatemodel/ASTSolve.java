/*
 * *****************************************************************************
 * OscaR is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * OscaR is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with OscaR.
 * If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 * ****************************************************************************
 */

package fzn.parser.intermediatemodel;

import fzn.parser.intermediatemodel.ASTLiterals.ASTLit;

import java.util.ArrayList;

/**
 * @author Gustav Bjordal
 */
public class ASTSolve extends ASTNode{

    public static final int SAT = 0;
    public static final int MIN = 1;
    public static final int MAX = 2;

    private int type;
    private ASTLit expr;
    private ArrayList<ASTLit> anns;

    public ASTSolve(int type, ASTLit expr, ArrayList<ASTLit> anns) {
        this.type = type;
        this.expr = expr;
        this.anns = anns;
    }

    public ArrayList<ASTLit> getAnns() {
        return anns;
    }

    public int getType() {
        return type;
    }

    public ASTLit getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        String solveType ="";
        switch (type){
            case SAT:
                solveType = "satisfy";
                break;
            case MIN:
                solveType = "minimize";
                break;
            case MAX:
                solveType = "maximize";
                break;
        }
        String annotations = "";

        for(ASTLit a: anns){
            annotations += " :: " +a;
        }

        return "solve" + annotations + " " + solveType + " " + (type==SAT ? ";": expr + ";") ;
    }
}
