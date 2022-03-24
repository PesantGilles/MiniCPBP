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

package fzn.parser.intermediatemodel.ASTLiterals;

import java.util.ArrayList;

/**
 * @author Gustav Bjordal
 */
public class ASTAnnotation extends ASTLit {
    private ASTId id;
    private ArrayList<ASTLit> args;

    public ASTId getId() {
        return id;
    }

    public void setId(ASTId id) {
        this.id = id;
    }

    public ArrayList<ASTLit> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<ASTLit> args) {
        this.args = args;
    }

    public ASTAnnotation(ASTId id, ArrayList<ASTLit> args) {
        this.id = id;
        this.args = args;
    }

    @Override
    public String toString() {
        String argsString = "";

        for(ASTLit a: args){
            argsString += a +", ";
        }

        return id + "(" +argsString +")";
    }
}
