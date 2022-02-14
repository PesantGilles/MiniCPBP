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

package fzn.parser.intermediatemodel.ASTTypes;

import fzn.parser.intermediatemodel.ASTLiterals.ASTRange;

/**
 * @author Gustav Bjordal
 */

public class ASTArrayType extends ASTType {
    private ASTRange indexSet;

    private ASTType type;

    public ASTArrayType(ASTRange indexSet, ASTType type) {
        if(indexSet.getLb().getValue() != 1){throw new RuntimeException("Ranges of array must start at 1");};
        this.indexSet = indexSet;
        this.type = type;
    }

    public ASTRange getIndexSet() {
        return indexSet;
    }

    public void setIndexSet(ASTRange indexSet) {
        this.indexSet = indexSet;
    }

    public ASTType getType() {
        return type;
    }

    public void setType(ASTType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "array [" + indexSet + "] of " +type;
    }

    @Override
    public ASTType getDataType() {
        return type.getDataType();
    }
}