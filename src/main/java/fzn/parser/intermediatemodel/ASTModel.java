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

import fzn.parser.intermediatemodel.ASTDecls.ASTFuncDecl;
import fzn.parser.intermediatemodel.ASTDecls.ASTParamDecl;
import fzn.parser.intermediatemodel.ASTDecls.ASTVarDecl;
import fzn.parser.intermediatemodel.ASTLiterals.ASTId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gustav Bjordal
 */
public class ASTModel extends ASTNode{
    private List<ASTParamDecl> paramDecls;
    private List<ASTVarDecl> varDecls;
    private List<ASTConstraint> constraints;
    private ASTSolve solve;
    private List<ASTFuncDecl> funcDecls;

    public ASTModel() {
        this.paramDecls = new ArrayList<>();
        this.varDecls = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.funcDecls = new ArrayList<>();
        this.solve = null;
    }

    public void print(){
        for(ASTConstraint p : constraints){
            if(p.getId().getValue().equals("bool2int")){
                ((ASTId)p.getArgs().get(0)).setValue(((ASTId)p.getArgs().get(1)).getValue());
            }
        }
        for(ASTParamDecl p : paramDecls){
            System.out.println(p);
        }
        for(ASTVarDecl p : varDecls){
            System.out.println(p);
        }
        for(ASTConstraint p : constraints){
            System.out.println(p);
        }
        for(ASTFuncDecl p : funcDecls){
            System.out.println(p);
        }
        System.out.println(solve);

    }

    public void addParamDecl(ASTParamDecl d){
        paramDecls.add(d);
    }
    public void addVarDecl(ASTVarDecl d){
        varDecls.add(d);
    }
    public void addConstraint(ASTConstraint d){
        constraints.add(d);
    }
    public void addFuncDecl(ASTFuncDecl d){
        funcDecls.add(d);
    }
    public void setSolve(ASTSolve s){
        solve = s;
    }

    public List<ASTParamDecl> getParamDecls() {
        return paramDecls;
    }

    public List<ASTVarDecl> getVarDecls() {
        return varDecls;
    }

    public List<ASTConstraint> getConstraints() {
        return constraints;
    }

    public ASTSolve getSolve() {
        return solve;
    }

    public List<ASTFuncDecl> getFuncDecls() {
        return funcDecls;
    }
}
