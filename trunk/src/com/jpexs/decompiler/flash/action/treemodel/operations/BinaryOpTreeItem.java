/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.treemodel.operations;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import java.util.List;

public abstract class BinaryOpTreeItem extends TreeItem {

   public TreeItem leftSide;
   public TreeItem rightSide;
   protected String operator = "";

   public BinaryOpTreeItem(Action instruction, int precedence, TreeItem leftSide, TreeItem rightSide, String operator) {
      super(instruction, precedence);
      this.leftSide = leftSide;
      this.rightSide = rightSide;
      this.operator = operator;
   }

   @Override
   public String toString(ConstantPool constants) {
      String ret = "";
      if (leftSide.precedence > precedence) {
         ret += "(" + leftSide.toString(constants) + ")";
      } else {
         ret += leftSide.toString(constants);
      }
      ret += hilight(operator);
      if (rightSide.precedence > precedence) {
         ret += "(" + rightSide.toString(constants) + ")";
      } else {
         ret += rightSide.toString(constants);
      }
      return ret;
   }

   @Override
   public boolean isCompileTime() {
      return leftSide.isCompileTime() && rightSide.isCompileTime();
   }

   @Override
   public List<com.jpexs.decompiler.flash.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.decompiler.flash.action.IgnoredPair> ret = super.getNeededActions();
      ret.addAll(leftSide.getNeededActions());
      ret.addAll(rightSide.getNeededActions());
      return ret;
   }
}