// Generated from /Users/gustavbjordal/OscaR/oscar/oscar-fzn/src/main/java/oscar/flatzinc/parser/Flatzinc.g4 by ANTLR 4.7
package fzn.parser;

/*******************************************************************************
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
  ******************************************************************************/


import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FlatzincParser}.
 */
public interface FlatzincListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#flatzinc_model}.
	 * @param ctx the parse tree
	 */
	void enterFlatzinc_model(FlatzincParser.Flatzinc_modelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#flatzinc_model}.
	 * @param ctx the parse tree
	 */
	void exitFlatzinc_model(FlatzincParser.Flatzinc_modelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#pred_decl}.
	 * @param ctx the parse tree
	 */
	void enterPred_decl(FlatzincParser.Pred_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#pred_decl}.
	 * @param ctx the parse tree
	 */
	void exitPred_decl(FlatzincParser.Pred_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#pred_param}.
	 * @param ctx the parse tree
	 */
	void enterPred_param(FlatzincParser.Pred_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#pred_param}.
	 * @param ctx the parse tree
	 */
	void exitPred_param(FlatzincParser.Pred_paramContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#param_decl}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl(FlatzincParser.Param_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#param_decl}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl(FlatzincParser.Param_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#func_decl}.
	 * @param ctx the parse tree
	 */
	void enterFunc_decl(FlatzincParser.Func_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#func_decl}.
	 * @param ctx the parse tree
	 */
	void exitFunc_decl(FlatzincParser.Func_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#let_expr}.
	 * @param ctx the parse tree
	 */
	void enterLet_expr(FlatzincParser.Let_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#let_expr}.
	 * @param ctx the parse tree
	 */
	void exitLet_expr(FlatzincParser.Let_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#let_body}.
	 * @param ctx the parse tree
	 */
	void enterLet_body(FlatzincParser.Let_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#let_body}.
	 * @param ctx the parse tree
	 */
	void exitLet_body(FlatzincParser.Let_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl(FlatzincParser.Var_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl(FlatzincParser.Var_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#constraint}.
	 * @param ctx the parse tree
	 */
	void enterConstraint(FlatzincParser.ConstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#constraint}.
	 * @param ctx the parse tree
	 */
	void exitConstraint(FlatzincParser.ConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#solve_goal}.
	 * @param ctx the parse tree
	 */
	void enterSolve_goal(FlatzincParser.Solve_goalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#solve_goal}.
	 * @param ctx the parse tree
	 */
	void exitSolve_goal(FlatzincParser.Solve_goalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#basic_par_type}.
	 * @param ctx the parse tree
	 */
	void enterBasic_par_type(FlatzincParser.Basic_par_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#basic_par_type}.
	 * @param ctx the parse tree
	 */
	void exitBasic_par_type(FlatzincParser.Basic_par_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#basic_var_type}.
	 * @param ctx the parse tree
	 */
	void enterBasic_var_type(FlatzincParser.Basic_var_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#basic_var_type}.
	 * @param ctx the parse tree
	 */
	void exitBasic_var_type(FlatzincParser.Basic_var_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#par_type}.
	 * @param ctx the parse tree
	 */
	void enterPar_type(FlatzincParser.Par_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#par_type}.
	 * @param ctx the parse tree
	 */
	void exitPar_type(FlatzincParser.Par_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#var_type}.
	 * @param ctx the parse tree
	 */
	void enterVar_type(FlatzincParser.Var_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#var_type}.
	 * @param ctx the parse tree
	 */
	void exitVar_type(FlatzincParser.Var_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#array_type}.
	 * @param ctx the parse tree
	 */
	void enterArray_type(FlatzincParser.Array_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#array_type}.
	 * @param ctx the parse tree
	 */
	void exitArray_type(FlatzincParser.Array_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#par_array_type}.
	 * @param ctx the parse tree
	 */
	void enterPar_array_type(FlatzincParser.Par_array_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#par_array_type}.
	 * @param ctx the parse tree
	 */
	void exitPar_array_type(FlatzincParser.Par_array_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#pred_param_type}.
	 * @param ctx the parse tree
	 */
	void enterPred_param_type(FlatzincParser.Pred_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#pred_param_type}.
	 * @param ctx the parse tree
	 */
	void exitPred_param_type(FlatzincParser.Pred_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#basic_pred_param_type}.
	 * @param ctx the parse tree
	 */
	void enterBasic_pred_param_type(FlatzincParser.Basic_pred_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#basic_pred_param_type}.
	 * @param ctx the parse tree
	 */
	void exitBasic_pred_param_type(FlatzincParser.Basic_pred_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#pred_array_type}.
	 * @param ctx the parse tree
	 */
	void enterPred_array_type(FlatzincParser.Pred_array_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#pred_array_type}.
	 * @param ctx the parse tree
	 */
	void exitPred_array_type(FlatzincParser.Pred_array_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(FlatzincParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(FlatzincParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#lit_expr}.
	 * @param ctx the parse tree
	 */
	void enterLit_expr(FlatzincParser.Lit_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#lit_expr}.
	 * @param ctx the parse tree
	 */
	void exitLit_expr(FlatzincParser.Lit_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#const_set}.
	 * @param ctx the parse tree
	 */
	void enterConst_set(FlatzincParser.Const_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#const_set}.
	 * @param ctx the parse tree
	 */
	void exitConst_set(FlatzincParser.Const_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#const_range}.
	 * @param ctx the parse tree
	 */
	void enterConst_range(FlatzincParser.Const_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#const_range}.
	 * @param ctx the parse tree
	 */
	void exitConst_range(FlatzincParser.Const_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#const_float_range}.
	 * @param ctx the parse tree
	 */
	void enterConst_float_range(FlatzincParser.Const_float_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#const_float_range}.
	 * @param ctx the parse tree
	 */
	void exitConst_float_range(FlatzincParser.Const_float_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#array_expr}.
	 * @param ctx the parse tree
	 */
	void enterArray_expr(FlatzincParser.Array_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#array_expr}.
	 * @param ctx the parse tree
	 */
	void exitArray_expr(FlatzincParser.Array_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#annotations}.
	 * @param ctx the parse tree
	 */
	void enterAnnotations(FlatzincParser.AnnotationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#annotations}.
	 * @param ctx the parse tree
	 */
	void exitAnnotations(FlatzincParser.AnnotationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(FlatzincParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(FlatzincParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#pred_ann_id}.
	 * @param ctx the parse tree
	 */
	void enterPred_ann_id(FlatzincParser.Pred_ann_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#pred_ann_id}.
	 * @param ctx the parse tree
	 */
	void exitPred_ann_id(FlatzincParser.Pred_ann_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#bool_const}.
	 * @param ctx the parse tree
	 */
	void enterBool_const(FlatzincParser.Bool_constContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#bool_const}.
	 * @param ctx the parse tree
	 */
	void exitBool_const(FlatzincParser.Bool_constContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#float_const}.
	 * @param ctx the parse tree
	 */
	void enterFloat_const(FlatzincParser.Float_constContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#float_const}.
	 * @param ctx the parse tree
	 */
	void exitFloat_const(FlatzincParser.Float_constContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#int_const}.
	 * @param ctx the parse tree
	 */
	void enterInt_const(FlatzincParser.Int_constContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#int_const}.
	 * @param ctx the parse tree
	 */
	void exitInt_const(FlatzincParser.Int_constContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#string_constant}.
	 * @param ctx the parse tree
	 */
	void enterString_constant(FlatzincParser.String_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#string_constant}.
	 * @param ctx the parse tree
	 */
	void exitString_constant(FlatzincParser.String_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatzincParser#var_par_id}.
	 * @param ctx the parse tree
	 */
	void enterVar_par_id(FlatzincParser.Var_par_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatzincParser#var_par_id}.
	 * @param ctx the parse tree
	 */
	void exitVar_par_id(FlatzincParser.Var_par_idContext ctx);
}