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

public class Type {

	public boolean isArray = false;
	public int size = 1;
	public boolean isVar = false;
	public int typ;
	
	public static final String[] typeName = {"null","bool","int","set of int","float", "string","annotation","function","let","constraint"};
	public static final int NULL = 0;
	public static final int BOOL = 1;
	public static final int INT = 2;
	public static final int SET = 3;
	public static final int FLOAT = 4;
	public static final int STRING = 5;
	public static final int ANNOTATION = 6;
	
	public Type(int typ){
		
		this.typ = typ;
	}
	public Type(Type t){
		this.typ = t.typ;
		this.size = t.size;
		this.isArray = t.isArray;
		this.isVar = t.isVar;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isArray ? 1231 : 1237);
		result = prime * result + (isVar ? 1231 : 1237);
		result = prime * result + size;
		result = prime * result + typ;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Type other = (Type) obj;
		if (isArray != other.isArray)
			return false;
		if (isVar != other.isVar)
			return false;
		if (size != other.size)
			return false;
		if (typ != other.typ)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Type [isArray=" + isArray + ", size=" + size + ", isVar="
				+ isVar + ", type=" + typ + "]";
	}
	
}
