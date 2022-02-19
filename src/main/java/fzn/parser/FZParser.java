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
/**
 * @author Jean-Noel Monette
 */
package fzn.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import fzn.Model;
import minicpbp.util.exception.NotImplementedException;

import java.io.FileInputStream;
import java.io.IOException;


public class FZParser {
	public static Model readFlatZincModelFromFile(String fname, boolean acceptAnyCstr){
		try{
		//ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(fname));
		UnbufferedCharStream input = new UnbufferedCharStream(new FileInputStream(fname));
		return readFlatZincModel(input, acceptAnyCstr);
		}catch(IOException e){
	        System.err.println("File " + fname + " not found. Aborting.");
			System.exit(1);
		}
		return null;
	}
	public static Model readFlatZincModelFromString(String content, boolean acceptAnyCstr){
		ANTLRInputStream input = new ANTLRInputStream(content);
		return readFlatZincModel(input, acceptAnyCstr);
	}
	public static Model readFlatZincModel(CharStream input, boolean acceptAnyCstr){
		Model m = new Model(acceptAnyCstr);
		//try{
	        FlatzincLexer lex = new FlatzincLexer(input);
			//CommonTokenStream tokens = new CommonTokenStream(lex);
			lex.setTokenFactory(new CommonTokenFactory(true));
			TokenStream tokens = new UnbufferedTokenStream<CommonToken>(lex);
			FlatzincParser p = new FlatzincParser(tokens, m);
			p.setBuildParseTree(false);//in order to get acceptable performance on large files
			//Handling errors
			p.removeParseListeners();
			p.removeErrorListeners();
			//Handling errors
			//p.addErrorListener(new DiagnosticErrorListener());
			p.addErrorListener(new BaseErrorListener() {
              public void syntaxError(Recognizer<?, ?> recon, Object offendingSymbol, int line,
                  int positionInLine, String message, RecognitionException e) { 
            	  //System.out.println(offendingSymbol);
                throw new NotImplementedException("line "+line+":"+positionInLine+" "+message);
              }
              
            });
			//The following try/catch and prediction modes implement this: https://theantlrguy.atlassian.net/wiki/pages/viewpage.action?pageId=1900591
			p.getInterpreter().setPredictionMode(PredictionMode.SLL);
			
			//for debugging?
			//p.addErrorListener(new DiagnosticErrorListener());
			//p.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);

	        try{
	        	p.flatzinc_model();
	        }catch (Exception e){
	        	e.printStackTrace();
	        	//log.apply(0,"Simple SLL Parsing Failed. Fallback to complete LL parsing");
	        	//Add to remove the following line for being able to use unbuffered streams!
	        	//tokens.reset();
	        	p.reset();
	        	p.getInterpreter().setPredictionMode(PredictionMode.LL);    
	        	p.flatzinc_model();  
	        }
	        if(p.getNumberOfSyntaxErrors()>0){
	          
	          throw new NotImplementedException("Parsing Error Somewhere");
	          /*System.err.println("Syntax error. Aborting.");
	          System.err.println("If the flatzinc file is correct, please report to the developers.");
	          System.exit(1);*/
	        }
			return m;
		/*}catch(RecognitionException e){
			e.printStackTrace();//TODO: report more friendly messages
			System.err.println("Syntax error. Aborting.");
	          System.err.println("If the flatzinc file is correct, please report to the developers.");
			System.exit(1);
		}catch(ParsingException e){
		  System.err.println(e.getMessage());
		  //e.printStackTrace();
		  //TODO: report more friendly and complete messages
          System.err.println("Syntax error. Aborting.");
          System.err.println("If the flatzinc file is correct, please report to the developers.");
          System.exit(1);
		}*/
		//return null;
	}
}
