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

package fzn.parser.intermediatemodel.ASTDecls;

import fzn.parser.intermediatemodel.ASTLet;
import fzn.parser.intermediatemodel.ASTLiterals.ASTId;
import fzn.parser.intermediatemodel.ASTNode;

import java.util.List;

/**
 * @author Gustav Bjordal
 */
public class ASTFuncDecl extends ASTNode {
    private ASTId id;
    private List<ASTVarDecl> params;
    private ASTLet body;

    public ASTFuncDecl(ASTId id, List<ASTVarDecl> params, ASTLet body) {
        this.id = id;
        this.params = params;
        this.body = body;
    }

    public ASTId getId() {
        return id;
    }

    public List<ASTVarDecl> getParams() {
        return params;
    }

    public ASTLet getBody() {
        return body;
    }

    @Override
    public String toString() {
        String parStr ="";
        if(!params.isEmpty()){
            parStr += "(";
            for(ASTVarDecl a: params){
                parStr += a +", ";
            }
            parStr += ")";
        }
        return "function ann: " +id + parStr+ " = \n" + body +";";
    }
}
