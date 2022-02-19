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

import fzn.parser.intermediatemodel.ASTLiterals.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gustav Bjordal
 */
public class IDContainer {
    private Map<String,ASTId> idMap;
    private Map<String,ASTString> stringMap;
    private Map<Integer, ASTInt> intMap;
    private Map<Float, ASTFloat> floatMap;

    private static ASTBool t;
    private static ASTBool f;


    public IDContainer() {
        this.idMap = new HashMap<>();
        this.intMap = new HashMap<>();
        this.floatMap = new HashMap<>();
        this.stringMap = new HashMap<>();
        t = new ASTBool(true);
        f = new ASTBool(false);
    }

    public ASTId getId(String s){
        if(idMap.containsKey(s)){
            return idMap.get(s);
        }
        ASTId id = new ASTId(s);
        idMap.put(s, id);
        return id;
    }

    public ASTInt getInt(Integer i){
        if(intMap.containsKey(i)){
            return intMap.get(i);
        }
        ASTInt id = new ASTInt(i);
        intMap.put(i, id);
        return id;
    }

    public ASTBool getBool(boolean b){
        if(b){
            return t;
        }
        return f;
    }

    public ASTString getString(String s){
        if(stringMap.containsKey(s)){
            return stringMap.get(s);
        }
        ASTString id = new ASTString(s);
        stringMap.put(s, id);
        return id;
    }

    public ASTFloat getFloat(Float f){
        if(floatMap.containsKey(f)){
            return floatMap.get(f);
        }
        ASTFloat id = new ASTFloat(f);
        floatMap.put(f, id);
        return id;
    }
}
