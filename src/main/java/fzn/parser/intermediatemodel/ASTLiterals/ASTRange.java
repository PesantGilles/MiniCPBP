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

//import oscar.flatzinc.model.FzDomainRange;

/**
 * @author Gustav Bjordal
 */
public class ASTRange extends ASTDomain {
    private ASTInt lb, ub;

    public ASTInt getLb() {
        return lb;
    }

    public void setLb(ASTInt lb) {
        this.lb = lb;
    }

    public ASTInt getUb() {
        return ub;
    }

    public void setUb(ASTInt ub) {
        this.ub = ub;
    }

    public ASTRange(ASTInt lb, ASTInt ub) {
        this.lb = lb;
        this.ub = ub;
    }

    public int getSize(){
        return ub.getValue() - lb.getValue()+1;
    }

    @Override
    public String toString() {
        return lb + ".."+ub;
    }

    @Override
    public Object getOldDomainObj() {
        return null;
    }
}
