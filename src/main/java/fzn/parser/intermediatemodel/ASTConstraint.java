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

import fzn.parser.intermediatemodel.ASTLiterals.ASTId;
import fzn.parser.intermediatemodel.ASTLiterals.ASTLit;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Gustav Bjordal
 */
public class ASTConstraint extends ASTNode{
    private ASTId id;
    private ArrayList<ASTLit> args;
    private ArrayList<ASTLit> anns;


    public ASTConstraint(ASTId id, ArrayList<ASTLit> args, ArrayList<ASTLit> anns) {
        this.id = id;
        this.args = args;
        this.anns = anns;
    }

    public ASTId getId() {

        return id;
    }

    public ArrayList<ASTLit> getArgs() {
        return args;
    }

    public ArrayList<ASTLit> getAnns() {
        return anns;
    }

    @Override
    public String toString() {
        String returnValue = "constraint "+ id + "(";

        String argsString = "";

        for(ASTLit a: args){
            argsString += a +", ";
        }

        returnValue = returnValue + argsString +")" + (anns.isEmpty()? "" : " :: " + Arrays.toString(anns.toArray()));
        return returnValue;
    }
}
