/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.action.flashlite;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.FSCommand2ActionItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionFSCommand2 extends Action {

    public ActionFSCommand2() {
        super(0x2D, 0);
    }

    @Override
    public String toString() {
        return "FSCommand2";
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        long numArgs = popLong(stack);
        GraphTargetItem command = stack.pop();
        List<GraphTargetItem> args = new ArrayList<>();
        for (long l = 0; l < numArgs; l++) {
            args.add(stack.pop());
        }
        stack.push(new FSCommand2ActionItem(this, command, args));
    }
}
