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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class FlatzincLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
		"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
		"T__25", "T__26", "T__27", "T__28", "T__29", "Boolconst", "PREDANNID", 
		"VARPARID", "Floatconst", "INT", "NUM", "STRING", "WS"
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


	public FlatzincLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Flatzinc.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\'\u0116\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\3\2\3\2\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3"+
		"\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\30\3\30\3\30"+
		"\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\34\3\34"+
		"\3\35\3\35\3\35\3\36\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3 \3 \3 \3 \3 "+
		"\3 \5 \u00df\n \3!\3!\7!\u00e3\n!\f!\16!\u00e6\13!\3\"\6\"\u00e9\n\"\r"+
		"\"\16\"\u00ea\3\"\3\"\3#\3#\3#\3#\3#\5#\u00f4\n#\3#\3#\3#\3#\5#\u00fa"+
		"\n#\3$\5$\u00fd\n$\3$\3$\3%\6%\u0102\n%\r%\16%\u0103\3&\3&\6&\u0108\n"+
		"&\r&\16&\u0109\3&\3&\3\'\3\'\3\'\6\'\u0111\n\'\r\'\16\'\u0112\3\'\3\'"+
		"\2\2(\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"C#E$G%I\2K&M\'\3\2\b\4\2C\\c|\6\2\62;C\\aac|\4\2GGgg\4\2--"+
		"//\3\2$$\4\2\13\f\"\"\2\u011e\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2"+
		"\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2"+
		"+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2"+
		"\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2"+
		"C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\3O\3\2\2\2\5Q\3"+
		"\2\2\2\7[\3\2\2\2\t]\3\2\2\2\13_\3\2\2\2\ra\3\2\2\2\17c\3\2\2\2\21e\3"+
		"\2\2\2\23n\3\2\2\2\25r\3\2\2\2\27v\3\2\2\2\31x\3\2\2\2\33z\3\2\2\2\35"+
		"}\3\2\2\2\37\u0088\3\2\2\2!\u008e\3\2\2\2#\u0096\3\2\2\2%\u009f\3\2\2"+
		"\2\'\u00a8\3\2\2\2)\u00ad\3\2\2\2+\u00b3\3\2\2\2-\u00b7\3\2\2\2/\u00bb"+
		"\3\2\2\2\61\u00be\3\2\2\2\63\u00c2\3\2\2\2\65\u00c8\3\2\2\2\67\u00ca\3"+
		"\2\2\29\u00cc\3\2\2\2;\u00cf\3\2\2\2=\u00d2\3\2\2\2?\u00de\3\2\2\2A\u00e0"+
		"\3\2\2\2C\u00e8\3\2\2\2E\u00f9\3\2\2\2G\u00fc\3\2\2\2I\u0101\3\2\2\2K"+
		"\u0105\3\2\2\2M\u0110\3\2\2\2OP\7=\2\2P\4\3\2\2\2QR\7r\2\2RS\7t\2\2ST"+
		"\7g\2\2TU\7f\2\2UV\7k\2\2VW\7e\2\2WX\7c\2\2XY\7v\2\2YZ\7g\2\2Z\6\3\2\2"+
		"\2[\\\7*\2\2\\\b\3\2\2\2]^\7.\2\2^\n\3\2\2\2_`\7+\2\2`\f\3\2\2\2ab\7<"+
		"\2\2b\16\3\2\2\2cd\7?\2\2d\20\3\2\2\2ef\7h\2\2fg\7w\2\2gh\7p\2\2hi\7e"+
		"\2\2ij\7v\2\2jk\7k\2\2kl\7q\2\2lm\7p\2\2m\22\3\2\2\2no\7c\2\2op\7p\2\2"+
		"pq\7p\2\2q\24\3\2\2\2rs\7n\2\2st\7g\2\2tu\7v\2\2u\26\3\2\2\2vw\7}\2\2"+
		"w\30\3\2\2\2xy\7\177\2\2y\32\3\2\2\2z{\7k\2\2{|\7p\2\2|\34\3\2\2\2}~\7"+
		"e\2\2~\177\7q\2\2\177\u0080\7p\2\2\u0080\u0081\7u\2\2\u0081\u0082\7v\2"+
		"\2\u0082\u0083\7t\2\2\u0083\u0084\7c\2\2\u0084\u0085\7k\2\2\u0085\u0086"+
		"\7p\2\2\u0086\u0087\7v\2\2\u0087\36\3\2\2\2\u0088\u0089\7u\2\2\u0089\u008a"+
		"\7q\2\2\u008a\u008b\7n\2\2\u008b\u008c\7x\2\2\u008c\u008d\7g\2\2\u008d"+
		" \3\2\2\2\u008e\u008f\7u\2\2\u008f\u0090\7c\2\2\u0090\u0091\7v\2\2\u0091"+
		"\u0092\7k\2\2\u0092\u0093\7u\2\2\u0093\u0094\7h\2\2\u0094\u0095\7{\2\2"+
		"\u0095\"\3\2\2\2\u0096\u0097\7o\2\2\u0097\u0098\7k\2\2\u0098\u0099\7p"+
		"\2\2\u0099\u009a\7k\2\2\u009a\u009b\7o\2\2\u009b\u009c\7k\2\2\u009c\u009d"+
		"\7|\2\2\u009d\u009e\7g\2\2\u009e$\3\2\2\2\u009f\u00a0\7o\2\2\u00a0\u00a1"+
		"\7c\2\2\u00a1\u00a2\7z\2\2\u00a2\u00a3\7k\2\2\u00a3\u00a4\7o\2\2\u00a4"+
		"\u00a5\7k\2\2\u00a5\u00a6\7|\2\2\u00a6\u00a7\7g\2\2\u00a7&\3\2\2\2\u00a8"+
		"\u00a9\7d\2\2\u00a9\u00aa\7q\2\2\u00aa\u00ab\7q\2\2\u00ab\u00ac\7n\2\2"+
		"\u00ac(\3\2\2\2\u00ad\u00ae\7h\2\2\u00ae\u00af\7n\2\2\u00af\u00b0\7q\2"+
		"\2\u00b0\u00b1\7c\2\2\u00b1\u00b2\7v\2\2\u00b2*\3\2\2\2\u00b3\u00b4\7"+
		"k\2\2\u00b4\u00b5\7p\2\2\u00b5\u00b6\7v\2\2\u00b6,\3\2\2\2\u00b7\u00b8"+
		"\7u\2\2\u00b8\u00b9\7g\2\2\u00b9\u00ba\7v\2\2\u00ba.\3\2\2\2\u00bb\u00bc"+
		"\7q\2\2\u00bc\u00bd\7h\2\2\u00bd\60\3\2\2\2\u00be\u00bf\7x\2\2\u00bf\u00c0"+
		"\7c\2\2\u00c0\u00c1\7t\2\2\u00c1\62\3\2\2\2\u00c2\u00c3\7c\2\2\u00c3\u00c4"+
		"\7t\2\2\u00c4\u00c5\7t\2\2\u00c5\u00c6\7c\2\2\u00c6\u00c7\7{\2\2\u00c7"+
		"\64\3\2\2\2\u00c8\u00c9\7]\2\2\u00c9\66\3\2\2\2\u00ca\u00cb\7_\2\2\u00cb"+
		"8\3\2\2\2\u00cc\u00cd\7\60\2\2\u00cd\u00ce\7\60\2\2\u00ce:\3\2\2\2\u00cf"+
		"\u00d0\7<\2\2\u00d0\u00d1\7<\2\2\u00d1<\3\2\2\2\u00d2\u00d3\7*\2\2\u00d3"+
		"\u00d4\7+\2\2\u00d4>\3\2\2\2\u00d5\u00d6\7v\2\2\u00d6\u00d7\7t\2\2\u00d7"+
		"\u00d8\7w\2\2\u00d8\u00df\7g\2\2\u00d9\u00da\7h\2\2\u00da\u00db\7c\2\2"+
		"\u00db\u00dc\7n\2\2\u00dc\u00dd\7u\2\2\u00dd\u00df\7g\2\2\u00de\u00d5"+
		"\3\2\2\2\u00de\u00d9\3\2\2\2\u00df@\3\2\2\2\u00e0\u00e4\t\2\2\2\u00e1"+
		"\u00e3\t\3\2\2\u00e2\u00e1\3\2\2\2\u00e3\u00e6\3\2\2\2\u00e4\u00e2\3\2"+
		"\2\2\u00e4\u00e5\3\2\2\2\u00e5B\3\2\2\2\u00e6\u00e4\3\2\2\2\u00e7\u00e9"+
		"\7a\2\2\u00e8\u00e7\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea\u00e8\3\2\2\2\u00ea"+
		"\u00eb\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ed\5A!\2\u00edD\3\2\2\2\u00ee"+
		"\u00ef\5G$\2\u00ef\u00f0\7\60\2\2\u00f0\u00f3\5I%\2\u00f1\u00f2\t\4\2"+
		"\2\u00f2\u00f4\5G$\2\u00f3\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00fa"+
		"\3\2\2\2\u00f5\u00f6\5G$\2\u00f6\u00f7\t\4\2\2\u00f7\u00f8\5G$\2\u00f8"+
		"\u00fa\3\2\2\2\u00f9\u00ee\3\2\2\2\u00f9\u00f5\3\2\2\2\u00faF\3\2\2\2"+
		"\u00fb\u00fd\t\5\2\2\u00fc\u00fb\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fe"+
		"\3\2\2\2\u00fe\u00ff\5I%\2\u00ffH\3\2\2\2\u0100\u0102\4\62;\2\u0101\u0100"+
		"\3\2\2\2\u0102\u0103\3\2\2\2\u0103\u0101\3\2\2\2\u0103\u0104\3\2\2\2\u0104"+
		"J\3\2\2\2\u0105\u0107\7$\2\2\u0106\u0108\n\6\2\2\u0107\u0106\3\2\2\2\u0108"+
		"\u0109\3\2\2\2\u0109\u0107\3\2\2\2\u0109\u010a\3\2\2\2\u010a\u010b\3\2"+
		"\2\2\u010b\u010c\7$\2\2\u010cL\3\2\2\2\u010d\u0111\t\7\2\2\u010e\u010f"+
		"\7\17\2\2\u010f\u0111\7\f\2\2\u0110\u010d\3\2\2\2\u0110\u010e\3\2\2\2"+
		"\u0111\u0112\3\2\2\2\u0112\u0110\3\2\2\2\u0112\u0113\3\2\2\2\u0113\u0114"+
		"\3\2\2\2\u0114\u0115\b\'\2\2\u0115N\3\2\2\2\r\2\u00de\u00e4\u00ea\u00f3"+
		"\u00f9\u00fc\u0103\u0109\u0110\u0112\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}