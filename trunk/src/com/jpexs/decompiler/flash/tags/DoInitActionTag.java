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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoInitActionTag extends CharacterTag implements ASMSource {

   /**
    * Identifier of Sprite
    */
   public int spriteId = 0;
   /**
    * List of actions to perform
    */
   //public List<Action> actions = new ArrayList<Action>();
   public byte[] actionBytes;

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public DoInitActionTag(byte[] data, int version, long pos) throws IOException {
      super(59, "DoInitAction", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      spriteId = sis.readUI16();
      //actions = sis.readActionList();
      actionBytes = sis.readBytes(sis.available());
   }

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeUI16(spriteId);
         sos.write(actionBytes);
         //sos.write(Action.actionsToBytes(actions, true, version));
         sos.close();
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }

   /**
    * Whether or not this object contains ASM source
    *
    * @return True when contains
    */
   public boolean containsSource() {
      return true;
   }

   /**
    * Converts actions to ASM source
    *
    * @param version SWF version
    * @return ASM source
    */
   public String getASMSource(int version) {
      List<Action> actions = new ArrayList<Action>();
      try {
         actions = (new SWFInputStream(new ByteArrayInputStream(actionBytes), version)).readActionList();
      } catch (IOException ex) {
         Logger.getLogger(DoInitActionTag.class.getName()).log(Level.SEVERE, null, ex);
      }
      return Action.actionsToString(actions, null, version);
   }

   public List<Action> getActions(int version) {
      try {
         return (new SWFInputStream(new ByteArrayInputStream(actionBytes), version)).readActionList();
      } catch (IOException ex) {
         Logger.getLogger(DoInitActionTag.class.getName()).log(Level.SEVERE, null, ex);
         return new ArrayList<Action>();
      }
   }

   public void setActions(List<Action> actions, int version) {
      actionBytes = Action.actionsToBytes(actions, true, version);
   }

   public byte[] getActionBytes() {
      return actionBytes;
   }

   public void setActionBytes(byte[] actionBytes) {
      this.actionBytes = actionBytes;
   }

   @Override
   public int getCharacterID() {
      return spriteId;
   }
}