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
//package oscar.flatzinc.parser;
import fzn.parser.intermediatemodel.*;
import fzn.parser.intermediatemodel.ASTLiterals.*;
import fzn.parser.intermediatemodel.ASTDecls.*;
import fzn.parser.intermediatemodel.ASTTypes.*;
import fzn.Model;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FlatzincParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, Boolconst=31, 
		PREDANNID=32, VARPARID=33, Floatconst=34, INT=35, STRING=36, WS=37;
	public static final int
		RULE_flatzinc_model = 0, RULE_pred_decl = 1, RULE_pred_param = 2, RULE_param_decl = 3, 
		RULE_func_decl = 4, RULE_let_expr = 5, RULE_let_body = 6, RULE_var_decl = 7, 
		RULE_constraint = 8, RULE_solve_goal = 9, RULE_basic_par_type = 10, RULE_basic_var_type = 11, 
		RULE_par_type = 12, RULE_var_type = 13, RULE_array_type = 14, RULE_par_array_type = 15, 
		RULE_pred_param_type = 16, RULE_basic_pred_param_type = 17, RULE_pred_array_type = 18, 
		RULE_expr = 19, RULE_lit_expr = 20, RULE_const_set = 21, RULE_const_range = 22, 
		RULE_const_float_range = 23, RULE_array_expr = 24, RULE_annotations = 25, 
		RULE_annotation = 26, RULE_pred_ann_id = 27, RULE_bool_const = 28, RULE_float_const = 29, 
		RULE_int_const = 30, RULE_string_constant = 31, RULE_var_par_id = 32;
	public static final String[] ruleNames = {
		"flatzinc_model", "pred_decl", "pred_param", "param_decl", "func_decl", 
		"let_expr", "let_body", "var_decl", "constraint", "solve_goal", "basic_par_type", 
		"basic_var_type", "par_type", "var_type", "array_type", "par_array_type", 
		"pred_param_type", "basic_pred_param_type", "pred_array_type", "expr", 
		"lit_expr", "const_set", "const_range", "const_float_range", "array_expr", 
		"annotations", "annotation", "pred_ann_id", "bool_const", "float_const", 
		"int_const", "string_constant", "var_par_id"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'predicate'", "'('", "','", "')'", "':'", "'='", "'function'", 
		"'ann'", "'let'", "'{'", "'}'", "'in'", "'constraint'", "'solve'", "'satisfy'", 
		"'minimize'", "'maximize'", "'bool'", "'float'", "'int'", "'set'", "'of'", 
		"'var'", "'array'", "'['", "']'", "'..'", "'::'", "'()'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, "Boolconst", "PREDANNID", "VARPARID", 
		"Floatconst", "INT", "STRING", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Flatzinc.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


		private Model m;
		private ASTModel astM;
		private IDContainer idC;
		public FlatzincParser(TokenStream input, Model m){
			this(input);
			this.m = m;
			this.astM = new ASTModel();
			idC = new IDContainer();
		}

	public FlatzincParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class Flatzinc_modelContext extends ParserRuleContext {
		public Param_declContext param_decl;
		public Var_declContext var_decl;
		public ConstraintContext constraint;
		public Solve_goalContext solve_goal;
		public Func_declContext func_decl;
		public List<Pred_declContext> pred_decl() {
			return getRuleContexts(Pred_declContext.class);
		}
		public Pred_declContext pred_decl(int i) {
			return getRuleContext(Pred_declContext.class,i);
		}
		public List<Param_declContext> param_decl() {
			return getRuleContexts(Param_declContext.class);
		}
		public Param_declContext param_decl(int i) {
			return getRuleContext(Param_declContext.class,i);
		}
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public List<Solve_goalContext> solve_goal() {
			return getRuleContexts(Solve_goalContext.class);
		}
		public Solve_goalContext solve_goal(int i) {
			return getRuleContext(Solve_goalContext.class,i);
		}
		public List<Func_declContext> func_decl() {
			return getRuleContexts(Func_declContext.class);
		}
		public Func_declContext func_decl(int i) {
			return getRuleContext(Func_declContext.class,i);
		}
		public Flatzinc_modelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flatzinc_model; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterFlatzinc_model(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitFlatzinc_model(this);
		}
	}

	public final Flatzinc_modelContext flatzinc_model() throws RecognitionException {
		Flatzinc_modelContext _localctx = new Flatzinc_modelContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_flatzinc_model);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__7) | (1L << T__13) | (1L << T__14) | (1L << T__18) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__23) | (1L << T__24))) != 0)) {
				{
				setState(87);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(66);
					pred_decl();
					}
					break;
				case 2:
					{
					setState(67);
					((Flatzinc_modelContext)_localctx).param_decl = param_decl();
					setState(68);
					match(T__0);
					astM.addParamDecl(((Flatzinc_modelContext)_localctx).param_decl.d);
					}
					break;
				case 3:
					{
					setState(71);
					((Flatzinc_modelContext)_localctx).var_decl = var_decl();
					setState(72);
					match(T__0);
					astM.addVarDecl(((Flatzinc_modelContext)_localctx).var_decl.d);
					}
					break;
				case 4:
					{
					setState(75);
					((Flatzinc_modelContext)_localctx).constraint = constraint();
					setState(76);
					match(T__0);
					astM.addConstraint(((Flatzinc_modelContext)_localctx).constraint.c);
					}
					break;
				case 5:
					{
					setState(79);
					((Flatzinc_modelContext)_localctx).solve_goal = solve_goal();
					setState(80);
					match(T__0);
					astM.setSolve(((Flatzinc_modelContext)_localctx).solve_goal.s);
					}
					break;
				case 6:
					{
					setState(83);
					((Flatzinc_modelContext)_localctx).func_decl = func_decl();
					setState(84);
					match(T__0);
					astM.addFuncDecl(((Flatzinc_modelContext)_localctx).func_decl.f);
					}
					break;
				}
				}
				setState(91);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			m.addASTModel(astM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_declContext extends ParserRuleContext {
		public TerminalNode PREDANNID() { return getToken(FlatzincParser.PREDANNID, 0); }
		public List<Pred_paramContext> pred_param() {
			return getRuleContexts(Pred_paramContext.class);
		}
		public Pred_paramContext pred_param(int i) {
			return getRuleContext(Pred_paramContext.class,i);
		}
		public Pred_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPred_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPred_decl(this);
		}
	}

	public final Pred_declContext pred_decl() throws RecognitionException {
		Pred_declContext _localctx = new Pred_declContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_pred_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			match(T__1);
			setState(95);
			match(PREDANNID);
			setState(96);
			match(T__2);
			setState(97);
			pred_param();
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(98);
				match(T__3);
				setState(99);
				pred_param();
				}
				}
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(105);
			match(T__4);
			setState(106);
			match(T__0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_paramContext extends ParserRuleContext {
		public Pred_param_typeContext pred_param_type() {
			return getRuleContext(Pred_param_typeContext.class,0);
		}
		public Pred_ann_idContext pred_ann_id() {
			return getRuleContext(Pred_ann_idContext.class,0);
		}
		public Pred_paramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_param; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPred_param(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPred_param(this);
		}
	}

	public final Pred_paramContext pred_param() throws RecognitionException {
		Pred_paramContext _localctx = new Pred_paramContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_pred_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			pred_param_type();
			setState(109);
			match(T__5);
			setState(110);
			pred_ann_id();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Param_declContext extends ParserRuleContext {
		public ASTParamDecl d;
		public Par_typeContext par_type;
		public Var_par_idContext var_par_id;
		public ExprContext expr;
		public Par_typeContext par_type() {
			return getRuleContext(Par_typeContext.class,0);
		}
		public Var_par_idContext var_par_id() {
			return getRuleContext(Var_par_idContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Param_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterParam_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitParam_decl(this);
		}
	}

	public final Param_declContext param_decl() throws RecognitionException {
		Param_declContext _localctx = new Param_declContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_param_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			((Param_declContext)_localctx).par_type = par_type();
			setState(113);
			match(T__5);
			setState(114);
			((Param_declContext)_localctx).var_par_id = var_par_id();
			setState(115);
			match(T__6);
			setState(116);
			((Param_declContext)_localctx).expr = expr();
			((Param_declContext)_localctx).d =  new ASTParamDecl(((Param_declContext)_localctx).var_par_id.text, ((Param_declContext)_localctx).par_type.t, ((Param_declContext)_localctx).expr.e);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Func_declContext extends ParserRuleContext {
		public ASTFuncDecl f;
		public ArrayList<ASTVarDecl> params = new ArrayList<ASTVarDecl>();;
		public Pred_ann_idContext pred_ann_id;
		public Var_declContext var_decl;
		public Let_exprContext let_expr;
		public Pred_ann_idContext pred_ann_id() {
			return getRuleContext(Pred_ann_idContext.class,0);
		}
		public Let_exprContext let_expr() {
			return getRuleContext(Let_exprContext.class,0);
		}
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public Func_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterFunc_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitFunc_decl(this);
		}
	}

	public final Func_declContext func_decl() throws RecognitionException {
		Func_declContext _localctx = new Func_declContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_func_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(T__7);
			setState(120);
			match(T__8);
			setState(121);
			match(T__5);
			setState(122);
			((Func_declContext)_localctx).pred_ann_id = pred_ann_id();
			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(123);
				match(T__2);
				setState(135);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__23 || _la==T__24) {
					{
					setState(124);
					((Func_declContext)_localctx).var_decl = var_decl();
					_localctx.params.add(((Func_declContext)_localctx).var_decl.d);
					setState(132);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(126);
						match(T__3);
						setState(127);
						((Func_declContext)_localctx).var_decl = var_decl();
						_localctx.params.add(((Func_declContext)_localctx).var_decl.d);
						}
						}
						setState(134);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(137);
				match(T__4);
				}
			}

			setState(140);
			match(T__6);
			setState(141);
			((Func_declContext)_localctx).let_expr = let_expr();
			((Func_declContext)_localctx).f =  new ASTFuncDecl(((Func_declContext)_localctx).pred_ann_id.text, _localctx.params, ((Func_declContext)_localctx).let_expr.l);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Let_exprContext extends ParserRuleContext {
		public ASTLet l;
		public ArrayList<ASTNode> body;
		public Let_bodyContext let_body;
		public AnnotationContext annotation;
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public List<Let_bodyContext> let_body() {
			return getRuleContexts(Let_bodyContext.class);
		}
		public Let_bodyContext let_body(int i) {
			return getRuleContext(Let_bodyContext.class,i);
		}
		public Let_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_let_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterLet_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitLet_expr(this);
		}
	}

	public final Let_exprContext let_expr() throws RecognitionException {
		Let_exprContext _localctx = new Let_exprContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_let_expr);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			match(T__9);
			setState(145);
			match(T__10);
			((Let_exprContext)_localctx).body =  new ArrayList<ASTNode>();
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__13) | (1L << T__18) | (1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__23) | (1L << T__24))) != 0)) {
				{
				setState(147);
				((Let_exprContext)_localctx).let_body = let_body();
				_localctx.body.add(((Let_exprContext)_localctx).let_body.n);
				setState(155);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(149);
						match(T__3);
						setState(150);
						((Let_exprContext)_localctx).let_body = let_body();
						_localctx.body.add(((Let_exprContext)_localctx).let_body.n);
						}
						} 
					}
					setState(157);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
				}
				}
			}

			setState(161);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(160);
				match(T__3);
				}
			}

			setState(163);
			match(T__11);
			setState(164);
			match(T__12);
			setState(165);
			match(T__2);
			setState(166);
			((Let_exprContext)_localctx).annotation = annotation();
			setState(167);
			match(T__4);
			 ((Let_exprContext)_localctx).l =  new ASTLet(_localctx.body, ((Let_exprContext)_localctx).annotation.ann);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Let_bodyContext extends ParserRuleContext {
		public ASTNode n;
		public Var_declContext var_decl;
		public Param_declContext param_decl;
		public ConstraintContext constraint;
		public Var_declContext var_decl() {
			return getRuleContext(Var_declContext.class,0);
		}
		public Param_declContext param_decl() {
			return getRuleContext(Param_declContext.class,0);
		}
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public Let_bodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_let_body; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterLet_body(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitLet_body(this);
		}
	}

	public final Let_bodyContext let_body() throws RecognitionException {
		Let_bodyContext _localctx = new Let_bodyContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_let_body);
		try {
			setState(179);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(170);
				((Let_bodyContext)_localctx).var_decl = var_decl();
				((Let_bodyContext)_localctx).n =  ((Let_bodyContext)_localctx).var_decl.d;
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(173);
				((Let_bodyContext)_localctx).param_decl = param_decl();
				((Let_bodyContext)_localctx).n =  ((Let_bodyContext)_localctx).param_decl.d;
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(176);
				((Let_bodyContext)_localctx).constraint = constraint();
				((Let_bodyContext)_localctx).n =  ((Let_bodyContext)_localctx).constraint.c;
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_declContext extends ParserRuleContext {
		public ASTVarDecl d;
		public ASTLit e = null;;
		public Var_typeContext var_type;
		public Var_par_idContext var_par_id;
		public AnnotationsContext annotations;
		public ExprContext expr;
		public Var_typeContext var_type() {
			return getRuleContext(Var_typeContext.class,0);
		}
		public Var_par_idContext var_par_id() {
			return getRuleContext(Var_par_idContext.class,0);
		}
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Var_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterVar_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitVar_decl(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_var_decl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			((Var_declContext)_localctx).var_type = var_type();
			setState(182);
			match(T__5);
			setState(183);
			((Var_declContext)_localctx).var_par_id = var_par_id();
			setState(184);
			((Var_declContext)_localctx).annotations = annotations();
			setState(189);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__6) {
				{
				setState(185);
				match(T__6);
				setState(186);
				((Var_declContext)_localctx).expr = expr();
				((Var_declContext)_localctx).e =  ((Var_declContext)_localctx).expr.e;
				}
			}

			((Var_declContext)_localctx).d =  new ASTVarDecl(((Var_declContext)_localctx).var_par_id.text, ((Var_declContext)_localctx).var_type.t, ((Var_declContext)_localctx).annotations.anns, _localctx.e);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstraintContext extends ParserRuleContext {
		public ASTConstraint c;
		public ArrayList<ASTLit> args;
		public Pred_ann_idContext pred_ann_id;
		public ExprContext e;
		public ExprContext e1;
		public AnnotationsContext annotations;
		public Pred_ann_idContext pred_ann_id() {
			return getRuleContext(Pred_ann_idContext.class,0);
		}
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitConstraint(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_constraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(193);
			match(T__13);
			setState(194);
			((ConstraintContext)_localctx).pred_ann_id = pred_ann_id();
			setState(195);
			match(T__2);
			setState(196);
			((ConstraintContext)_localctx).e = expr();
			((ConstraintContext)_localctx).args =  new ArrayList<ASTLit>(); _localctx.args.add(((ConstraintContext)_localctx).e.e);
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(198);
				match(T__3);
				setState(199);
				((ConstraintContext)_localctx).e1 = expr();
				_localctx.args.add(((ConstraintContext)_localctx).e1.e);
				}
				}
				setState(206);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(207);
			match(T__4);
			setState(208);
			((ConstraintContext)_localctx).annotations = annotations();
			((ConstraintContext)_localctx).c =  new ASTConstraint(((ConstraintContext)_localctx).pred_ann_id.text, _localctx.args, ((ConstraintContext)_localctx).annotations.anns);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Solve_goalContext extends ParserRuleContext {
		public ASTSolve s;
		public ASTLit e;
		public int t;
		public AnnotationsContext annotations;
		public ExprContext expr;
		public AnnotationsContext annotations() {
			return getRuleContext(AnnotationsContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Solve_goalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_solve_goal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterSolve_goal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitSolve_goal(this);
		}
	}

	public final Solve_goalContext solve_goal() throws RecognitionException {
		Solve_goalContext _localctx = new Solve_goalContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_solve_goal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			match(T__14);
			setState(212);
			((Solve_goalContext)_localctx).annotations = annotations();
			setState(223);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__15:
				{
				setState(213);
				match(T__15);
				((Solve_goalContext)_localctx).e =  null; ((Solve_goalContext)_localctx).t =  ASTSolve.SAT;
				}
				break;
			case T__16:
				{
				setState(215);
				match(T__16);
				setState(216);
				((Solve_goalContext)_localctx).expr = expr();
				((Solve_goalContext)_localctx).e =  ((Solve_goalContext)_localctx).expr.e;((Solve_goalContext)_localctx).t =  ASTSolve.MIN;
				}
				break;
			case T__17:
				{
				setState(219);
				match(T__17);
				setState(220);
				((Solve_goalContext)_localctx).expr = expr();
				((Solve_goalContext)_localctx).e =  ((Solve_goalContext)_localctx).expr.e;((Solve_goalContext)_localctx).t =  ASTSolve.MAX;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			((Solve_goalContext)_localctx).s =  new ASTSolve(_localctx.t,_localctx.e,((Solve_goalContext)_localctx).annotations.anns);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Basic_par_typeContext extends ParserRuleContext {
		public ASTType t;
		public Basic_par_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basic_par_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterBasic_par_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitBasic_par_type(this);
		}
	}

	public final Basic_par_typeContext basic_par_type() throws RecognitionException {
		Basic_par_typeContext _localctx = new Basic_par_typeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_basic_par_type);
		try {
			setState(237);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__18:
				enterOuterAlt(_localctx, 1);
				{
				setState(227);
				match(T__18);
				((Basic_par_typeContext)_localctx).t =  ASTConstants.BOOL;
				}
				break;
			case T__19:
				enterOuterAlt(_localctx, 2);
				{
				setState(229);
				match(T__19);
				((Basic_par_typeContext)_localctx).t =  ASTConstants.FLOAT;
				}
				break;
			case T__20:
				enterOuterAlt(_localctx, 3);
				{
				setState(231);
				match(T__20);
				((Basic_par_typeContext)_localctx).t =  ASTConstants.INT;
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 4);
				{
				setState(233);
				match(T__21);
				setState(234);
				match(T__22);
				setState(235);
				match(T__20);
				((Basic_par_typeContext)_localctx).t =  ASTConstants.SET;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Basic_var_typeContext extends ParserRuleContext {
		public ASTVarType t;
		public Const_float_rangeContext const_float_range;
		public Const_rangeContext const_range;
		public Const_setContext const_set;
		public Const_float_rangeContext const_float_range() {
			return getRuleContext(Const_float_rangeContext.class,0);
		}
		public Const_rangeContext const_range() {
			return getRuleContext(Const_rangeContext.class,0);
		}
		public Const_setContext const_set() {
			return getRuleContext(Const_setContext.class,0);
		}
		public Basic_var_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basic_var_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterBasic_var_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitBasic_var_type(this);
		}
	}

	public final Basic_var_typeContext basic_var_type() throws RecognitionException {
		Basic_var_typeContext _localctx = new Basic_var_typeContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_basic_var_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			match(T__23);
			setState(265);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(240);
				match(T__18);
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.BOOL);
				}
				break;
			case 2:
				{
				setState(242);
				match(T__19);
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.FLOAT);
				}
				break;
			case 3:
				{
				setState(244);
				((Basic_var_typeContext)_localctx).const_float_range = const_float_range();
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.FLOAT,((Basic_var_typeContext)_localctx).const_float_range.r);
				}
				break;
			case 4:
				{
				setState(247);
				match(T__20);
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.INT);
				}
				break;
			case 5:
				{
				setState(249);
				((Basic_var_typeContext)_localctx).const_range = const_range();
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.INT, ((Basic_var_typeContext)_localctx).const_range.r);
				}
				break;
			case 6:
				{
				setState(252);
				((Basic_var_typeContext)_localctx).const_set = const_set();
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.INT, ((Basic_var_typeContext)_localctx).const_set.r);
				}
				break;
			case 7:
				{
				setState(255);
				match(T__21);
				setState(256);
				match(T__22);
				setState(257);
				((Basic_var_typeContext)_localctx).const_range = const_range();
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.SET, ((Basic_var_typeContext)_localctx).const_range.r);
				}
				break;
			case 8:
				{
				setState(260);
				match(T__21);
				setState(261);
				match(T__22);
				setState(262);
				((Basic_var_typeContext)_localctx).const_set = const_set();
				((Basic_var_typeContext)_localctx).t =  new ASTVarType(ASTConstants.SET, ((Basic_var_typeContext)_localctx).const_set.r);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Par_typeContext extends ParserRuleContext {
		public ASTType t;
		public Basic_par_typeContext basic_par_type;
		public Par_array_typeContext par_array_type;
		public Basic_par_typeContext basic_par_type() {
			return getRuleContext(Basic_par_typeContext.class,0);
		}
		public Par_array_typeContext par_array_type() {
			return getRuleContext(Par_array_typeContext.class,0);
		}
		public Par_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPar_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPar_type(this);
		}
	}

	public final Par_typeContext par_type() throws RecognitionException {
		Par_typeContext _localctx = new Par_typeContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_par_type);
		try {
			setState(273);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__18:
			case T__19:
			case T__20:
			case T__21:
				enterOuterAlt(_localctx, 1);
				{
				setState(267);
				((Par_typeContext)_localctx).basic_par_type = basic_par_type();
				((Par_typeContext)_localctx).t =  ((Par_typeContext)_localctx).basic_par_type.t;
				}
				break;
			case T__24:
				enterOuterAlt(_localctx, 2);
				{
				setState(270);
				((Par_typeContext)_localctx).par_array_type = par_array_type();
				((Par_typeContext)_localctx).t =  ((Par_typeContext)_localctx).par_array_type.t;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_typeContext extends ParserRuleContext {
		public ASTType t;
		public Basic_var_typeContext basic_var_type;
		public Array_typeContext array_type;
		public Basic_var_typeContext basic_var_type() {
			return getRuleContext(Basic_var_typeContext.class,0);
		}
		public Array_typeContext array_type() {
			return getRuleContext(Array_typeContext.class,0);
		}
		public Var_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterVar_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitVar_type(this);
		}
	}

	public final Var_typeContext var_type() throws RecognitionException {
		Var_typeContext _localctx = new Var_typeContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_var_type);
		try {
			setState(281);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__23:
				enterOuterAlt(_localctx, 1);
				{
				setState(275);
				((Var_typeContext)_localctx).basic_var_type = basic_var_type();
				((Var_typeContext)_localctx).t =  ((Var_typeContext)_localctx).basic_var_type.t;
				}
				break;
			case T__24:
				enterOuterAlt(_localctx, 2);
				{
				setState(278);
				((Var_typeContext)_localctx).array_type = array_type();
				((Var_typeContext)_localctx).t =  ((Var_typeContext)_localctx).array_type.t;
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Array_typeContext extends ParserRuleContext {
		public ASTArrayType t;
		public Const_rangeContext const_range;
		public Basic_var_typeContext basic_var_type;
		public Const_rangeContext const_range() {
			return getRuleContext(Const_rangeContext.class,0);
		}
		public Basic_var_typeContext basic_var_type() {
			return getRuleContext(Basic_var_typeContext.class,0);
		}
		public Array_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterArray_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitArray_type(this);
		}
	}

	public final Array_typeContext array_type() throws RecognitionException {
		Array_typeContext _localctx = new Array_typeContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_array_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(283);
			match(T__24);
			setState(284);
			match(T__25);
			setState(285);
			((Array_typeContext)_localctx).const_range = const_range();
			setState(286);
			match(T__26);
			setState(287);
			match(T__22);
			setState(288);
			((Array_typeContext)_localctx).basic_var_type = basic_var_type();
			((Array_typeContext)_localctx).t =  new ASTArrayType(((Array_typeContext)_localctx).const_range.r, ((Array_typeContext)_localctx).basic_var_type.t);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Par_array_typeContext extends ParserRuleContext {
		public ASTArrayType t;
		public Const_rangeContext const_range;
		public Basic_par_typeContext basic_par_type;
		public Const_rangeContext const_range() {
			return getRuleContext(Const_rangeContext.class,0);
		}
		public Basic_par_typeContext basic_par_type() {
			return getRuleContext(Basic_par_typeContext.class,0);
		}
		public Par_array_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_par_array_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPar_array_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPar_array_type(this);
		}
	}

	public final Par_array_typeContext par_array_type() throws RecognitionException {
		Par_array_typeContext _localctx = new Par_array_typeContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_par_array_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			match(T__24);
			setState(292);
			match(T__25);
			setState(293);
			((Par_array_typeContext)_localctx).const_range = const_range();
			setState(294);
			match(T__26);
			setState(295);
			match(T__22);
			setState(296);
			((Par_array_typeContext)_localctx).basic_par_type = basic_par_type();
			((Par_array_typeContext)_localctx).t =  new ASTArrayType(((Par_array_typeContext)_localctx).const_range.r, ((Par_array_typeContext)_localctx).basic_par_type.t);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_param_typeContext extends ParserRuleContext {
		public Basic_pred_param_typeContext basic_pred_param_type() {
			return getRuleContext(Basic_pred_param_typeContext.class,0);
		}
		public Pred_array_typeContext pred_array_type() {
			return getRuleContext(Pred_array_typeContext.class,0);
		}
		public Pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_param_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPred_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPred_param_type(this);
		}
	}

	public final Pred_param_typeContext pred_param_type() throws RecognitionException {
		Pred_param_typeContext _localctx = new Pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_pred_param_type);
		try {
			setState(301);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__10:
			case T__18:
			case T__19:
			case T__20:
			case T__21:
			case T__23:
			case Floatconst:
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(299);
				basic_pred_param_type();
				}
				break;
			case T__24:
				enterOuterAlt(_localctx, 2);
				{
				setState(300);
				pred_array_type();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Basic_pred_param_typeContext extends ParserRuleContext {
		public Basic_var_typeContext basic_var_type() {
			return getRuleContext(Basic_var_typeContext.class,0);
		}
		public Basic_par_typeContext basic_par_type() {
			return getRuleContext(Basic_par_typeContext.class,0);
		}
		public List<Float_constContext> float_const() {
			return getRuleContexts(Float_constContext.class);
		}
		public Float_constContext float_const(int i) {
			return getRuleContext(Float_constContext.class,i);
		}
		public Const_setContext const_set() {
			return getRuleContext(Const_setContext.class,0);
		}
		public Const_rangeContext const_range() {
			return getRuleContext(Const_rangeContext.class,0);
		}
		public Basic_pred_param_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_basic_pred_param_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterBasic_pred_param_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitBasic_pred_param_type(this);
		}
	}

	public final Basic_pred_param_typeContext basic_pred_param_type() throws RecognitionException {
		Basic_pred_param_typeContext _localctx = new Basic_pred_param_typeContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_basic_pred_param_type);
		try {
			setState(321);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(303);
				basic_var_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(304);
				basic_par_type();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(305);
				float_const();
				setState(306);
				match(T__27);
				setState(307);
				float_const();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(309);
				const_set();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(310);
				const_range();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(311);
				match(T__21);
				setState(312);
				match(T__22);
				setState(313);
				const_range();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(314);
				match(T__21);
				setState(315);
				match(T__22);
				setState(316);
				const_set();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(317);
				match(T__23);
				setState(318);
				match(T__21);
				setState(319);
				match(T__22);
				setState(320);
				match(T__20);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_array_typeContext extends ParserRuleContext {
		public Basic_pred_param_typeContext basic_pred_param_type() {
			return getRuleContext(Basic_pred_param_typeContext.class,0);
		}
		public Const_rangeContext const_range() {
			return getRuleContext(Const_rangeContext.class,0);
		}
		public Pred_array_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_array_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPred_array_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPred_array_type(this);
		}
	}

	public final Pred_array_typeContext pred_array_type() throws RecognitionException {
		Pred_array_typeContext _localctx = new Pred_array_typeContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_pred_array_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			match(T__24);
			setState(324);
			match(T__25);
			setState(330);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(325);
				const_range();
				}
				break;
			case 2:
				{
				setState(326);
				match(T__20);
				}
				break;
			case 3:
				{
				setState(327);
				match(T__20);
				{
				setState(328);
				match(T__3);
				setState(329);
				match(T__20);
				}
				}
				break;
			}
			setState(332);
			match(T__26);
			setState(333);
			match(T__22);
			setState(334);
			basic_pred_param_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public ASTLit e;
		public Lit_exprContext lit_expr;
		public Lit_exprContext lit_expr() {
			return getRuleContext(Lit_exprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
			((ExprContext)_localctx).lit_expr = lit_expr();
			((ExprContext)_localctx).e =  ((ExprContext)_localctx).lit_expr.e;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Lit_exprContext extends ParserRuleContext {
		public ASTLit e;
		public Bool_constContext bool_const;
		public Float_constContext float_const;
		public Int_constContext int_const;
		public Const_rangeContext const_range;
		public Const_setContext const_set;
		public AnnotationContext annotation;
		public Array_exprContext array_expr;
		public String_constantContext string_constant;
		public Bool_constContext bool_const() {
			return getRuleContext(Bool_constContext.class,0);
		}
		public Float_constContext float_const() {
			return getRuleContext(Float_constContext.class,0);
		}
		public Int_constContext int_const() {
			return getRuleContext(Int_constContext.class,0);
		}
		public Const_rangeContext const_range() {
			return getRuleContext(Const_rangeContext.class,0);
		}
		public Const_setContext const_set() {
			return getRuleContext(Const_setContext.class,0);
		}
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public Array_exprContext array_expr() {
			return getRuleContext(Array_exprContext.class,0);
		}
		public String_constantContext string_constant() {
			return getRuleContext(String_constantContext.class,0);
		}
		public Lit_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lit_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterLit_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitLit_expr(this);
		}
	}

	public final Lit_exprContext lit_expr() throws RecognitionException {
		Lit_exprContext _localctx = new Lit_exprContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_lit_expr);
		try {
			setState(363);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(339);
				((Lit_exprContext)_localctx).bool_const = bool_const();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).bool_const.b;
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(342);
				((Lit_exprContext)_localctx).float_const = float_const();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).float_const.f;
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(345);
				((Lit_exprContext)_localctx).int_const = int_const();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).int_const.i;
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(348);
				((Lit_exprContext)_localctx).const_range = const_range();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).const_range.r;
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(351);
				((Lit_exprContext)_localctx).const_set = const_set();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).const_set.r;
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(354);
				((Lit_exprContext)_localctx).annotation = annotation();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).annotation.ann;
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(357);
				((Lit_exprContext)_localctx).array_expr = array_expr();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).array_expr.a;
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(360);
				((Lit_exprContext)_localctx).string_constant = string_constant();
				((Lit_exprContext)_localctx).e =  ((Lit_exprContext)_localctx).string_constant.str;
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Const_setContext extends ParserRuleContext {
		public ASTSet r;
		public Set<ASTInt> s;
		public Int_constContext f;
		public Int_constContext n;
		public List<Int_constContext> int_const() {
			return getRuleContexts(Int_constContext.class);
		}
		public Int_constContext int_const(int i) {
			return getRuleContext(Int_constContext.class,i);
		}
		public Const_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterConst_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitConst_set(this);
		}
	}

	public final Const_setContext const_set() throws RecognitionException {
		Const_setContext _localctx = new Const_setContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_const_set);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(365);
			match(T__10);
			((Const_setContext)_localctx).s =  new HashSet<ASTInt>();
			setState(378);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INT) {
				{
				setState(367);
				((Const_setContext)_localctx).f = int_const();
				 _localctx.s.add(((Const_setContext)_localctx).f.i); 
				setState(375);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(369);
					match(T__3);
					setState(370);
					((Const_setContext)_localctx).n = int_const();
					 _localctx.s.add(((Const_setContext)_localctx).n.i);
					}
					}
					setState(377);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(380);
			match(T__11);
			((Const_setContext)_localctx).r =  new ASTSet(_localctx.s);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Const_rangeContext extends ParserRuleContext {
		public ASTRange r;
		public Int_constContext lb;
		public Int_constContext ub;
		public List<Int_constContext> int_const() {
			return getRuleContexts(Int_constContext.class);
		}
		public Int_constContext int_const(int i) {
			return getRuleContext(Int_constContext.class,i);
		}
		public Const_rangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterConst_range(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitConst_range(this);
		}
	}

	public final Const_rangeContext const_range() throws RecognitionException {
		Const_rangeContext _localctx = new Const_rangeContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_const_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(383);
			((Const_rangeContext)_localctx).lb = int_const();
			setState(384);
			match(T__27);
			setState(385);
			((Const_rangeContext)_localctx).ub = int_const();
			((Const_rangeContext)_localctx).r =  new ASTRange(((Const_rangeContext)_localctx).lb.i,((Const_rangeContext)_localctx).ub.i);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Const_float_rangeContext extends ParserRuleContext {
		public ASTFloatRange r;
		public Float_constContext lb;
		public Float_constContext ub;
		public List<Float_constContext> float_const() {
			return getRuleContexts(Float_constContext.class);
		}
		public Float_constContext float_const(int i) {
			return getRuleContext(Float_constContext.class,i);
		}
		public Const_float_rangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const_float_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterConst_float_range(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitConst_float_range(this);
		}
	}

	public final Const_float_rangeContext const_float_range() throws RecognitionException {
		Const_float_rangeContext _localctx = new Const_float_rangeContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_const_float_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			((Const_float_rangeContext)_localctx).lb = float_const();
			setState(389);
			match(T__27);
			setState(390);
			((Const_float_rangeContext)_localctx).ub = float_const();
			((Const_float_rangeContext)_localctx).r =  new ASTFloatRange(((Const_float_rangeContext)_localctx).lb.f,((Const_float_rangeContext)_localctx).ub.f);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Array_exprContext extends ParserRuleContext {
		public ASTArray a;
		public ArrayList<ASTLit> elems;
		public Lit_exprContext e;
		public List<Lit_exprContext> lit_expr() {
			return getRuleContexts(Lit_exprContext.class);
		}
		public Lit_exprContext lit_expr(int i) {
			return getRuleContext(Lit_exprContext.class,i);
		}
		public Array_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterArray_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitArray_expr(this);
		}
	}

	public final Array_exprContext array_expr() throws RecognitionException {
		Array_exprContext _localctx = new Array_exprContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_array_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			match(T__25);
			((Array_exprContext)_localctx).elems =  new ArrayList<ASTLit>();
			setState(406);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__10) | (1L << T__25) | (1L << Boolconst) | (1L << PREDANNID) | (1L << Floatconst) | (1L << INT) | (1L << STRING))) != 0)) {
				{
				setState(395);
				((Array_exprContext)_localctx).e = lit_expr();
				_localctx.elems.add(((Array_exprContext)_localctx).e.e);
				setState(403);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(397);
					match(T__3);
					setState(398);
					((Array_exprContext)_localctx).e = lit_expr();
					_localctx.elems.add(((Array_exprContext)_localctx).e.e);
					}
					}
					setState(405);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(408);
			match(T__26);
			((Array_exprContext)_localctx).a =  new ASTArray(_localctx.elems);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationsContext extends ParserRuleContext {
		public ArrayList<ASTLit> anns;
		public AnnotationContext annotation;
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public AnnotationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotations; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterAnnotations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitAnnotations(this);
		}
	}

	public final AnnotationsContext annotations() throws RecognitionException {
		AnnotationsContext _localctx = new AnnotationsContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_annotations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			((AnnotationsContext)_localctx).anns =  new ArrayList<ASTLit>();
			setState(418);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__28) {
				{
				{
				setState(412);
				match(T__28);
				setState(413);
				((AnnotationsContext)_localctx).annotation = annotation();
				_localctx.anns.add(((AnnotationsContext)_localctx).annotation.ann);
				}
				}
				setState(420);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnnotationContext extends ParserRuleContext {
		public ASTLit ann;
		public ArrayList<ASTLit> args;
		public Pred_ann_idContext pred_ann_id;
		public ExprContext expr;
		public Pred_ann_idContext pred_ann_id() {
			return getRuleContext(Pred_ann_idContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterAnnotation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitAnnotation(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_annotation);
		int _la;
		try {
			setState(445);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(421);
				((AnnotationContext)_localctx).pred_ann_id = pred_ann_id();
				((AnnotationContext)_localctx).ann =  ((AnnotationContext)_localctx).pred_ann_id.text;
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(424);
				((AnnotationContext)_localctx).pred_ann_id = pred_ann_id();
				((AnnotationContext)_localctx).args =  new ArrayList<ASTLit>();
				setState(441);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__29:
					{
					{
					setState(426);
					match(T__29);
					}
					}
					break;
				case T__2:
					{
					{
					setState(427);
					match(T__2);
					setState(428);
					((AnnotationContext)_localctx).expr = expr();
					_localctx.args.add(((AnnotationContext)_localctx).expr.e);
					setState(436);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__3) {
						{
						{
						setState(430);
						match(T__3);
						setState(431);
						((AnnotationContext)_localctx).expr = expr();
						_localctx.args.add(((AnnotationContext)_localctx).expr.e);
						}
						}
						setState(438);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(439);
					match(T__4);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				((AnnotationContext)_localctx).ann =  new ASTAnnotation(((AnnotationContext)_localctx).pred_ann_id.text, _localctx.args);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Pred_ann_idContext extends ParserRuleContext {
		public ASTId text;
		public Token PREDANNID;
		public TerminalNode PREDANNID() { return getToken(FlatzincParser.PREDANNID, 0); }
		public Pred_ann_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred_ann_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterPred_ann_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitPred_ann_id(this);
		}
	}

	public final Pred_ann_idContext pred_ann_id() throws RecognitionException {
		Pred_ann_idContext _localctx = new Pred_ann_idContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_pred_ann_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
			((Pred_ann_idContext)_localctx).PREDANNID = match(PREDANNID);
			((Pred_ann_idContext)_localctx).text =  idC.getId(((Pred_ann_idContext)_localctx).PREDANNID.getText());
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bool_constContext extends ParserRuleContext {
		public ASTBool b;
		public Token Boolconst;
		public TerminalNode Boolconst() { return getToken(FlatzincParser.Boolconst, 0); }
		public Bool_constContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool_const; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterBool_const(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitBool_const(this);
		}
	}

	public final Bool_constContext bool_const() throws RecognitionException {
		Bool_constContext _localctx = new Bool_constContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_bool_const);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(450);
			((Bool_constContext)_localctx).Boolconst = match(Boolconst);
			((Bool_constContext)_localctx).b =  idC.getBool(((Bool_constContext)_localctx).Boolconst.getText().equals("true"));
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Float_constContext extends ParserRuleContext {
		public ASTFloat f;
		public Token Floatconst;
		public TerminalNode Floatconst() { return getToken(FlatzincParser.Floatconst, 0); }
		public Float_constContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_float_const; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterFloat_const(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitFloat_const(this);
		}
	}

	public final Float_constContext float_const() throws RecognitionException {
		Float_constContext _localctx = new Float_constContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_float_const);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(453);
			((Float_constContext)_localctx).Floatconst = match(Floatconst);
			((Float_constContext)_localctx).f =  idC.getFloat(Float.parseFloat(((Float_constContext)_localctx).Floatconst.getText()));
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Int_constContext extends ParserRuleContext {
		public ASTInt i;
		public Token INT;
		public TerminalNode INT() { return getToken(FlatzincParser.INT, 0); }
		public Int_constContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_int_const; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterInt_const(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitInt_const(this);
		}
	}

	public final Int_constContext int_const() throws RecognitionException {
		Int_constContext _localctx = new Int_constContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_int_const);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(456);
			((Int_constContext)_localctx).INT = match(INT);
			((Int_constContext)_localctx).i =  idC.getInt(Integer.parseInt(((Int_constContext)_localctx).INT.getText()));
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_constantContext extends ParserRuleContext {
		public ASTString str;
		public Token STRING;
		public TerminalNode STRING() { return getToken(FlatzincParser.STRING, 0); }
		public String_constantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterString_constant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitString_constant(this);
		}
	}

	public final String_constantContext string_constant() throws RecognitionException {
		String_constantContext _localctx = new String_constantContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_string_constant);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(459);
			((String_constantContext)_localctx).STRING = match(STRING);
			((String_constantContext)_localctx).str =  idC.getString(((String_constantContext)_localctx).STRING.getText().substring(1,((String_constantContext)_localctx).STRING.getText().length()-1));
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Var_par_idContext extends ParserRuleContext {
		public ASTId text;
		public Pred_ann_idContext pred_ann_id;
		public Pred_ann_idContext pred_ann_id() {
			return getRuleContext(Pred_ann_idContext.class,0);
		}
		public Var_par_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_par_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).enterVar_par_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof FlatzincListener ) ((FlatzincListener)listener).exitVar_par_id(this);
		}
	}

	public final Var_par_idContext var_par_id() throws RecognitionException {
		Var_par_idContext _localctx = new Var_par_idContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_var_par_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(462);
			((Var_par_idContext)_localctx).pred_ann_id = pred_ann_id();
			((Var_par_idContext)_localctx).text =  ((Var_par_idContext)_localctx).pred_ann_id.text;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\'\u01d4\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\2\3\2\3\2\3\2\3\2\3\2\7\2Z\n\2\f\2\16\2]\13\2\3\2\3\2\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\7\3g\n\3\f\3\16\3j\13\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6"+
		"\u0085\n\6\f\6\16\6\u0088\13\6\5\6\u008a\n\6\3\6\5\6\u008d\n\6\3\6\3\6"+
		"\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\7\7\u009c\n\7\f\7\16\7\u009f"+
		"\13\7\5\7\u00a1\n\7\3\7\5\7\u00a4\n\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u00b6\n\b\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\5\t\u00c0\n\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\7\n"+
		"\u00cd\n\n\f\n\16\n\u00d0\13\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00e2\n\13\3\13\3\13\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u00f0\n\f\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\5\r\u010c\n\r\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0114\n\16"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u011c\n\17\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\5\22"+
		"\u0130\n\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u0144\n\23\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\5\24\u014d\n\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\26\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u016e\n\26\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\7\27\u0178\n\27\f\27\16\27\u017b\13\27\5\27"+
		"\u017d\n\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31"+
		"\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\7\32\u0194\n\32\f\32\16"+
		"\32\u0197\13\32\5\32\u0199\n\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33"+
		"\7\33\u01a3\n\33\f\33\16\33\u01a6\13\33\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\7\34\u01b5\n\34\f\34\16\34\u01b8\13"+
		"\34\3\34\3\34\5\34\u01bc\n\34\3\34\3\34\5\34\u01c0\n\34\3\35\3\35\3\35"+
		"\3\36\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3!\3!\3!\3\"\3\"\3\"\3\"\2\2#"+
		"\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@B\2\2"+
		"\2\u01ea\2[\3\2\2\2\4`\3\2\2\2\6n\3\2\2\2\br\3\2\2\2\ny\3\2\2\2\f\u0092"+
		"\3\2\2\2\16\u00b5\3\2\2\2\20\u00b7\3\2\2\2\22\u00c3\3\2\2\2\24\u00d5\3"+
		"\2\2\2\26\u00ef\3\2\2\2\30\u00f1\3\2\2\2\32\u0113\3\2\2\2\34\u011b\3\2"+
		"\2\2\36\u011d\3\2\2\2 \u0125\3\2\2\2\"\u012f\3\2\2\2$\u0143\3\2\2\2&\u0145"+
		"\3\2\2\2(\u0152\3\2\2\2*\u016d\3\2\2\2,\u016f\3\2\2\2.\u0181\3\2\2\2\60"+
		"\u0186\3\2\2\2\62\u018b\3\2\2\2\64\u019d\3\2\2\2\66\u01bf\3\2\2\28\u01c1"+
		"\3\2\2\2:\u01c4\3\2\2\2<\u01c7\3\2\2\2>\u01ca\3\2\2\2@\u01cd\3\2\2\2B"+
		"\u01d0\3\2\2\2DZ\5\4\3\2EF\5\b\5\2FG\7\3\2\2GH\b\2\1\2HZ\3\2\2\2IJ\5\20"+
		"\t\2JK\7\3\2\2KL\b\2\1\2LZ\3\2\2\2MN\5\22\n\2NO\7\3\2\2OP\b\2\1\2PZ\3"+
		"\2\2\2QR\5\24\13\2RS\7\3\2\2ST\b\2\1\2TZ\3\2\2\2UV\5\n\6\2VW\7\3\2\2W"+
		"X\b\2\1\2XZ\3\2\2\2YD\3\2\2\2YE\3\2\2\2YI\3\2\2\2YM\3\2\2\2YQ\3\2\2\2"+
		"YU\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\^\3\2\2\2][\3\2\2\2^_\b\2\1"+
		"\2_\3\3\2\2\2`a\7\4\2\2ab\7\"\2\2bc\7\5\2\2ch\5\6\4\2de\7\6\2\2eg\5\6"+
		"\4\2fd\3\2\2\2gj\3\2\2\2hf\3\2\2\2hi\3\2\2\2ik\3\2\2\2jh\3\2\2\2kl\7\7"+
		"\2\2lm\7\3\2\2m\5\3\2\2\2no\5\"\22\2op\7\b\2\2pq\58\35\2q\7\3\2\2\2rs"+
		"\5\32\16\2st\7\b\2\2tu\5B\"\2uv\7\t\2\2vw\5(\25\2wx\b\5\1\2x\t\3\2\2\2"+
		"yz\7\n\2\2z{\7\13\2\2{|\7\b\2\2|\u008c\58\35\2}\u0089\7\5\2\2~\177\5\20"+
		"\t\2\177\u0086\b\6\1\2\u0080\u0081\7\6\2\2\u0081\u0082\5\20\t\2\u0082"+
		"\u0083\b\6\1\2\u0083\u0085\3\2\2\2\u0084\u0080\3\2\2\2\u0085\u0088\3\2"+
		"\2\2\u0086\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u008a\3\2\2\2\u0088"+
		"\u0086\3\2\2\2\u0089~\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008b\3\2\2\2"+
		"\u008b\u008d\7\7\2\2\u008c}\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008e\3"+
		"\2\2\2\u008e\u008f\7\t\2\2\u008f\u0090\5\f\7\2\u0090\u0091\b\6\1\2\u0091"+
		"\13\3\2\2\2\u0092\u0093\7\f\2\2\u0093\u0094\7\r\2\2\u0094\u00a0\b\7\1"+
		"\2\u0095\u0096\5\16\b\2\u0096\u009d\b\7\1\2\u0097\u0098\7\6\2\2\u0098"+
		"\u0099\5\16\b\2\u0099\u009a\b\7\1\2\u009a\u009c\3\2\2\2\u009b\u0097\3"+
		"\2\2\2\u009c\u009f\3\2\2\2\u009d\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e"+
		"\u00a1\3\2\2\2\u009f\u009d\3\2\2\2\u00a0\u0095\3\2\2\2\u00a0\u00a1\3\2"+
		"\2\2\u00a1\u00a3\3\2\2\2\u00a2\u00a4\7\6\2\2\u00a3\u00a2\3\2\2\2\u00a3"+
		"\u00a4\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a6\7\16\2\2\u00a6\u00a7\7"+
		"\17\2\2\u00a7\u00a8\7\5\2\2\u00a8\u00a9\5\66\34\2\u00a9\u00aa\7\7\2\2"+
		"\u00aa\u00ab\b\7\1\2\u00ab\r\3\2\2\2\u00ac\u00ad\5\20\t\2\u00ad\u00ae"+
		"\b\b\1\2\u00ae\u00b6\3\2\2\2\u00af\u00b0\5\b\5\2\u00b0\u00b1\b\b\1\2\u00b1"+
		"\u00b6\3\2\2\2\u00b2\u00b3\5\22\n\2\u00b3\u00b4\b\b\1\2\u00b4\u00b6\3"+
		"\2\2\2\u00b5\u00ac\3\2\2\2\u00b5\u00af\3\2\2\2\u00b5\u00b2\3\2\2\2\u00b6"+
		"\17\3\2\2\2\u00b7\u00b8\5\34\17\2\u00b8\u00b9\7\b\2\2\u00b9\u00ba\5B\""+
		"\2\u00ba\u00bf\5\64\33\2\u00bb\u00bc\7\t\2\2\u00bc\u00bd\5(\25\2\u00bd"+
		"\u00be\b\t\1\2\u00be\u00c0\3\2\2\2\u00bf\u00bb\3\2\2\2\u00bf\u00c0\3\2"+
		"\2\2\u00c0\u00c1\3\2\2\2\u00c1\u00c2\b\t\1\2\u00c2\21\3\2\2\2\u00c3\u00c4"+
		"\7\20\2\2\u00c4\u00c5\58\35\2\u00c5\u00c6\7\5\2\2\u00c6\u00c7\5(\25\2"+
		"\u00c7\u00ce\b\n\1\2\u00c8\u00c9\7\6\2\2\u00c9\u00ca\5(\25\2\u00ca\u00cb"+
		"\b\n\1\2\u00cb\u00cd\3\2\2\2\u00cc\u00c8\3\2\2\2\u00cd\u00d0\3\2\2\2\u00ce"+
		"\u00cc\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00d1\3\2\2\2\u00d0\u00ce\3\2"+
		"\2\2\u00d1\u00d2\7\7\2\2\u00d2\u00d3\5\64\33\2\u00d3\u00d4\b\n\1\2\u00d4"+
		"\23\3\2\2\2\u00d5\u00d6\7\21\2\2\u00d6\u00e1\5\64\33\2\u00d7\u00d8\7\22"+
		"\2\2\u00d8\u00e2\b\13\1\2\u00d9\u00da\7\23\2\2\u00da\u00db\5(\25\2\u00db"+
		"\u00dc\b\13\1\2\u00dc\u00e2\3\2\2\2\u00dd\u00de\7\24\2\2\u00de\u00df\5"+
		"(\25\2\u00df\u00e0\b\13\1\2\u00e0\u00e2\3\2\2\2\u00e1\u00d7\3\2\2\2\u00e1"+
		"\u00d9\3\2\2\2\u00e1\u00dd\3\2\2\2\u00e2\u00e3\3\2\2\2\u00e3\u00e4\b\13"+
		"\1\2\u00e4\25\3\2\2\2\u00e5\u00e6\7\25\2\2\u00e6\u00f0\b\f\1\2\u00e7\u00e8"+
		"\7\26\2\2\u00e8\u00f0\b\f\1\2\u00e9\u00ea\7\27\2\2\u00ea\u00f0\b\f\1\2"+
		"\u00eb\u00ec\7\30\2\2\u00ec\u00ed\7\31\2\2\u00ed\u00ee\7\27\2\2\u00ee"+
		"\u00f0\b\f\1\2\u00ef\u00e5\3\2\2\2\u00ef\u00e7\3\2\2\2\u00ef\u00e9\3\2"+
		"\2\2\u00ef\u00eb\3\2\2\2\u00f0\27\3\2\2\2\u00f1\u010b\7\32\2\2\u00f2\u00f3"+
		"\7\25\2\2\u00f3\u010c\b\r\1\2\u00f4\u00f5\7\26\2\2\u00f5\u010c\b\r\1\2"+
		"\u00f6\u00f7\5\60\31\2\u00f7\u00f8\b\r\1\2\u00f8\u010c\3\2\2\2\u00f9\u00fa"+
		"\7\27\2\2\u00fa\u010c\b\r\1\2\u00fb\u00fc\5.\30\2\u00fc\u00fd\b\r\1\2"+
		"\u00fd\u010c\3\2\2\2\u00fe\u00ff\5,\27\2\u00ff\u0100\b\r\1\2\u0100\u010c"+
		"\3\2\2\2\u0101\u0102\7\30\2\2\u0102\u0103\7\31\2\2\u0103\u0104\5.\30\2"+
		"\u0104\u0105\b\r\1\2\u0105\u010c\3\2\2\2\u0106\u0107\7\30\2\2\u0107\u0108"+
		"\7\31\2\2\u0108\u0109\5,\27\2\u0109\u010a\b\r\1\2\u010a\u010c\3\2\2\2"+
		"\u010b\u00f2\3\2\2\2\u010b\u00f4\3\2\2\2\u010b\u00f6\3\2\2\2\u010b\u00f9"+
		"\3\2\2\2\u010b\u00fb\3\2\2\2\u010b\u00fe\3\2\2\2\u010b\u0101\3\2\2\2\u010b"+
		"\u0106\3\2\2\2\u010c\31\3\2\2\2\u010d\u010e\5\26\f\2\u010e\u010f\b\16"+
		"\1\2\u010f\u0114\3\2\2\2\u0110\u0111\5 \21\2\u0111\u0112\b\16\1\2\u0112"+
		"\u0114\3\2\2\2\u0113\u010d\3\2\2\2\u0113\u0110\3\2\2\2\u0114\33\3\2\2"+
		"\2\u0115\u0116\5\30\r\2\u0116\u0117\b\17\1\2\u0117\u011c\3\2\2\2\u0118"+
		"\u0119\5\36\20\2\u0119\u011a\b\17\1\2\u011a\u011c\3\2\2\2\u011b\u0115"+
		"\3\2\2\2\u011b\u0118\3\2\2\2\u011c\35\3\2\2\2\u011d\u011e\7\33\2\2\u011e"+
		"\u011f\7\34\2\2\u011f\u0120\5.\30\2\u0120\u0121\7\35\2\2\u0121\u0122\7"+
		"\31\2\2\u0122\u0123\5\30\r\2\u0123\u0124\b\20\1\2\u0124\37\3\2\2\2\u0125"+
		"\u0126\7\33\2\2\u0126\u0127\7\34\2\2\u0127\u0128\5.\30\2\u0128\u0129\7"+
		"\35\2\2\u0129\u012a\7\31\2\2\u012a\u012b\5\26\f\2\u012b\u012c\b\21\1\2"+
		"\u012c!\3\2\2\2\u012d\u0130\5$\23\2\u012e\u0130\5&\24\2\u012f\u012d\3"+
		"\2\2\2\u012f\u012e\3\2\2\2\u0130#\3\2\2\2\u0131\u0144\5\30\r\2\u0132\u0144"+
		"\5\26\f\2\u0133\u0134\5<\37\2\u0134\u0135\7\36\2\2\u0135\u0136\5<\37\2"+
		"\u0136\u0144\3\2\2\2\u0137\u0144\5,\27\2\u0138\u0144\5.\30\2\u0139\u013a"+
		"\7\30\2\2\u013a\u013b\7\31\2\2\u013b\u0144\5.\30\2\u013c\u013d\7\30\2"+
		"\2\u013d\u013e\7\31\2\2\u013e\u0144\5,\27\2\u013f\u0140\7\32\2\2\u0140"+
		"\u0141\7\30\2\2\u0141\u0142\7\31\2\2\u0142\u0144\7\27\2\2\u0143\u0131"+
		"\3\2\2\2\u0143\u0132\3\2\2\2\u0143\u0133\3\2\2\2\u0143\u0137\3\2\2\2\u0143"+
		"\u0138\3\2\2\2\u0143\u0139\3\2\2\2\u0143\u013c\3\2\2\2\u0143\u013f\3\2"+
		"\2\2\u0144%\3\2\2\2\u0145\u0146\7\33\2\2\u0146\u014c\7\34\2\2\u0147\u014d"+
		"\5.\30\2\u0148\u014d\7\27\2\2\u0149\u014a\7\27\2\2\u014a\u014b\7\6\2\2"+
		"\u014b\u014d\7\27\2\2\u014c\u0147\3\2\2\2\u014c\u0148\3\2\2\2\u014c\u0149"+
		"\3\2\2\2\u014d\u014e\3\2\2\2\u014e\u014f\7\35\2\2\u014f\u0150\7\31\2\2"+
		"\u0150\u0151\5$\23\2\u0151\'\3\2\2\2\u0152\u0153\5*\26\2\u0153\u0154\b"+
		"\25\1\2\u0154)\3\2\2\2\u0155\u0156\5:\36\2\u0156\u0157\b\26\1\2\u0157"+
		"\u016e\3\2\2\2\u0158\u0159\5<\37\2\u0159\u015a\b\26\1\2\u015a\u016e\3"+
		"\2\2\2\u015b\u015c\5> \2\u015c\u015d\b\26\1\2\u015d\u016e\3\2\2\2\u015e"+
		"\u015f\5.\30\2\u015f\u0160\b\26\1\2\u0160\u016e\3\2\2\2\u0161\u0162\5"+
		",\27\2\u0162\u0163\b\26\1\2\u0163\u016e\3\2\2\2\u0164\u0165\5\66\34\2"+
		"\u0165\u0166\b\26\1\2\u0166\u016e\3\2\2\2\u0167\u0168\5\62\32\2\u0168"+
		"\u0169\b\26\1\2\u0169\u016e\3\2\2\2\u016a\u016b\5@!\2\u016b\u016c\b\26"+
		"\1\2\u016c\u016e\3\2\2\2\u016d\u0155\3\2\2\2\u016d\u0158\3\2\2\2\u016d"+
		"\u015b\3\2\2\2\u016d\u015e\3\2\2\2\u016d\u0161\3\2\2\2\u016d\u0164\3\2"+
		"\2\2\u016d\u0167\3\2\2\2\u016d\u016a\3\2\2\2\u016e+\3\2\2\2\u016f\u0170"+
		"\7\r\2\2\u0170\u017c\b\27\1\2\u0171\u0172\5> \2\u0172\u0179\b\27\1\2\u0173"+
		"\u0174\7\6\2\2\u0174\u0175\5> \2\u0175\u0176\b\27\1\2\u0176\u0178\3\2"+
		"\2\2\u0177\u0173\3\2\2\2\u0178\u017b\3\2\2\2\u0179\u0177\3\2\2\2\u0179"+
		"\u017a\3\2\2\2\u017a\u017d\3\2\2\2\u017b\u0179\3\2\2\2\u017c\u0171\3\2"+
		"\2\2\u017c\u017d\3\2\2\2\u017d\u017e\3\2\2\2\u017e\u017f\7\16\2\2\u017f"+
		"\u0180\b\27\1\2\u0180-\3\2\2\2\u0181\u0182\5> \2\u0182\u0183\7\36\2\2"+
		"\u0183\u0184\5> \2\u0184\u0185\b\30\1\2\u0185/\3\2\2\2\u0186\u0187\5<"+
		"\37\2\u0187\u0188\7\36\2\2\u0188\u0189\5<\37\2\u0189\u018a\b\31\1\2\u018a"+
		"\61\3\2\2\2\u018b\u018c\7\34\2\2\u018c\u0198\b\32\1\2\u018d\u018e\5*\26"+
		"\2\u018e\u0195\b\32\1\2\u018f\u0190\7\6\2\2\u0190\u0191\5*\26\2\u0191"+
		"\u0192\b\32\1\2\u0192\u0194\3\2\2\2\u0193\u018f\3\2\2\2\u0194\u0197\3"+
		"\2\2\2\u0195\u0193\3\2\2\2\u0195\u0196\3\2\2\2\u0196\u0199\3\2\2\2\u0197"+
		"\u0195\3\2\2\2\u0198\u018d\3\2\2\2\u0198\u0199\3\2\2\2\u0199\u019a\3\2"+
		"\2\2\u019a\u019b\7\35\2\2\u019b\u019c\b\32\1\2\u019c\63\3\2\2\2\u019d"+
		"\u01a4\b\33\1\2\u019e\u019f\7\37\2\2\u019f\u01a0\5\66\34\2\u01a0\u01a1"+
		"\b\33\1\2\u01a1\u01a3\3\2\2\2\u01a2\u019e\3\2\2\2\u01a3\u01a6\3\2\2\2"+
		"\u01a4\u01a2\3\2\2\2\u01a4\u01a5\3\2\2\2\u01a5\65\3\2\2\2\u01a6\u01a4"+
		"\3\2\2\2\u01a7\u01a8\58\35\2\u01a8\u01a9\b\34\1\2\u01a9\u01c0\3\2\2\2"+
		"\u01aa\u01ab\58\35\2\u01ab\u01bb\b\34\1\2\u01ac\u01bc\7 \2\2\u01ad\u01ae"+
		"\7\5\2\2\u01ae\u01af\5(\25\2\u01af\u01b6\b\34\1\2\u01b0\u01b1\7\6\2\2"+
		"\u01b1\u01b2\5(\25\2\u01b2\u01b3\b\34\1\2\u01b3\u01b5\3\2\2\2\u01b4\u01b0"+
		"\3\2\2\2\u01b5\u01b8\3\2\2\2\u01b6\u01b4\3\2\2\2\u01b6\u01b7\3\2\2\2\u01b7"+
		"\u01b9\3\2\2\2\u01b8\u01b6\3\2\2\2\u01b9\u01ba\7\7\2\2\u01ba\u01bc\3\2"+
		"\2\2\u01bb\u01ac\3\2\2\2\u01bb\u01ad\3\2\2\2\u01bc\u01bd\3\2\2\2\u01bd"+
		"\u01be\b\34\1\2\u01be\u01c0\3\2\2\2\u01bf\u01a7\3\2\2\2\u01bf\u01aa\3"+
		"\2\2\2\u01c0\67\3\2\2\2\u01c1\u01c2\7\"\2\2\u01c2\u01c3\b\35\1\2\u01c3"+
		"9\3\2\2\2\u01c4\u01c5\7!\2\2\u01c5\u01c6\b\36\1\2\u01c6;\3\2\2\2\u01c7"+
		"\u01c8\7$\2\2\u01c8\u01c9\b\37\1\2\u01c9=\3\2\2\2\u01ca\u01cb\7%\2\2\u01cb"+
		"\u01cc\b \1\2\u01cc?\3\2\2\2\u01cd\u01ce\7&\2\2\u01ce\u01cf\b!\1\2\u01cf"+
		"A\3\2\2\2\u01d0\u01d1\58\35\2\u01d1\u01d2\b\"\1\2\u01d2C\3\2\2\2\37Y["+
		"h\u0086\u0089\u008c\u009d\u00a0\u00a3\u00b5\u00bf\u00ce\u00e1\u00ef\u010b"+
		"\u0113\u011b\u012f\u0143\u014c\u016d\u0179\u017c\u0195\u0198\u01a4\u01b6"+
		"\u01bb\u01bf";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}