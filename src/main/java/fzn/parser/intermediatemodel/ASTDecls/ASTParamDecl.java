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

import fzn.parser.intermediatemodel.ASTLiterals.ASTId;
import fzn.parser.intermediatemodel.ASTLiterals.ASTLit;
import fzn.parser.intermediatemodel.ASTTypes.ASTType;

import java.util.List;

/**
 * @author Gustav Bjordal
 */
public class ASTParamDecl extends ASTDecl {
    public ASTParamDecl(ASTId id, ASTType type, ASTLit expr, List<ASTLit> anns) {
        super(id, type, expr, anns);
    }

    public ASTParamDecl(ASTId id, ASTType type) {
        super(id, type);
    }

    public ASTParamDecl(ASTId id, ASTType type, ASTLit expr) {
        super(id, type, expr);
    }

    public ASTParamDecl(ASTId id, ASTType type, List<ASTLit> anns) {
        super(id, type, anns);
    }

    public ASTParamDecl(ASTId id) {
        super(id);
    }
}