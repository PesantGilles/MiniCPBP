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

import java.util.List;

/**
 * @author Gustav Bjordal
 */
public class ASTLet extends ASTNode{

    private List<ASTNode> body;
    private ASTLit returnValue;

    public ASTLet(List<ASTNode> body, ASTLit returnValue) {
        this.body = body;
        this.returnValue = returnValue;
    }

    public List<ASTNode> getBody() {
        return body;
    }

    public ASTLit getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        String r ="";
        for(ASTNode node: body) {
            r += "\t"+node + ",\n";
        }
        return "let {\n"+r+"} in " + returnValue;
    }
}
