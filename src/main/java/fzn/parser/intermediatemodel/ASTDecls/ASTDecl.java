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

import fzn.parser.intermediatemodel.ASTLiterals.ASTAnnotation;
import fzn.parser.intermediatemodel.ASTLiterals.ASTId;
import fzn.parser.intermediatemodel.ASTLiterals.ASTLit;
import fzn.parser.intermediatemodel.ASTNode;
import fzn.parser.intermediatemodel.ASTTypes.ASTType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Gustav Bjordal
 */
public class ASTDecl extends ASTNode {
    private ASTId id;
    private ASTType type;
    private List<ASTLit> anns;
    private ASTLit expr;

    public ASTDecl(ASTId id, ASTType type, ASTLit expr, List<ASTLit> anns) {
        this.id = id;
        this.type = type;
        this.anns = anns;
        this.expr = expr;
    }

    public ASTDecl(ASTId id, ASTType type) {
        this.id = id;
        this.type = type;
    }

    public ASTDecl(ASTId id, ASTType type, ASTLit expr) {
        this.id = id;
        this.type = type;
        this.expr = expr;
    }

    public ASTDecl(ASTId id, ASTType type, List<ASTLit> anns) {
        this.id = id;
        this.type = type;
        this.anns = anns;
    }

    public ASTDecl(ASTId id) {
        this.id = id;
    }

    public ASTId getId() {
        return id;
    }

    public void setId(ASTId id) {
        this.id = id;
    }

    public List<ASTLit> getAnns() {
        return anns;
    }

    public void setAnns(ArrayList<ASTLit> anns) {
        this.anns = anns;
    }

    public ASTLit getExpr() {
        return expr;
    }

    public ASTType getType() {
        return type;
    }

    public String getName() {
        return id.getValue();
    }

    public void setExpr(ASTLit expr) {
        this.expr = expr;
    }

    public boolean hasExpr(){
        return expr != null;
    }
    public  boolean hasAnnotation(String id){
        for(ASTLit a: anns){
            if(a instanceof ASTId && ((ASTId) a).getValue().equals(id)){
                return true;
            }else if(a instanceof ASTAnnotation && ((ASTAnnotation) a).getId().getValue().equals(id)){
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return type + ": " + id + " " + (anns == null || anns.isEmpty()? "" : " :: " + Arrays.toString(anns.toArray()))  + (expr == null ? ";" : " = " + expr + ";");
    }
}
