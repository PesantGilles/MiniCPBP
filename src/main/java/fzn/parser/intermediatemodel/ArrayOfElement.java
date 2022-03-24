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
package fzn.parser.intermediatemodel;

import java.util.ArrayList;
import java.util.List;

import minicpbp.util.exception.NotImplementedException;

public class ArrayOfElement extends Element{

	public List<Element> elements;
	public ArrayOfElement(){
		elements = new ArrayList<Element>();
	}
	@Override
	public String toString() {
		return "Array [elements=" + elements + ", name=" + name /*+ ", id=" + id*/
				+ ", type=" + typ/* + ", annotations=" + annotations*/ + "]";
	}
	public void close() {
	  if(elements.size()>0){
	    int typ = elements.get(0).typ.typ;
	    for(Element e: elements){
	      if(typ != e.typ.typ) throw new NotImplementedException("Not all same type in array");
	    }
	    this.typ.typ = typ;
	  }
	}
	
}
