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

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends the capabilities of DefineButton by allowing any state transition to
 * trigger actions
 *
 * @author JPEXS
 */
public class DefineButton2Tag extends CharacterTag implements Container, BoundedTag {

   /**
    * ID for this character
    */
   public int buttonId;
   /**
    * Track as menu button
    */
   public boolean trackAsMenu;
   /**
    * Characters that make up the button
    */
   public List<BUTTONRECORD> characters;
   /**
    * Actions to execute at particular button events
    */
   public List<BUTTONCONDACTION> actions = new ArrayList<BUTTONCONDACTION>();

   @Override
   public int getCharacterID() {
      return buttonId;
   }

   /**
    * Constructor
    *
    * @param data Data bytes
    * @param version SWF version
    * @throws IOException
    */
   public DefineButton2Tag(byte data[], int version, long pos) throws IOException {
      super(34, "DefineButton2", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      buttonId = sis.readUI16();
      sis.readUB(7); //reserved
      trackAsMenu = sis.readUB(1) == 1;
      int actionOffset = sis.readUI16();
      characters = sis.readBUTTONRECORDList(true);
      if (actionOffset > 0) {
         actions = sis.readBUTTONCONDACTIONList();
      }
   }

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      if (Main.DISABLE_DANGEROUS) {
         return super.getData(version);
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(super.data);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream os = baos;
      if (Main.DEBUG_COPY) {
         os = new CopyOutputStream(os, bais);
      }
      SWFOutputStream sos = new SWFOutputStream(os, version);
      try {
         sos.writeUI16(buttonId);
         sos.writeUB(7, 0); //reserved
         sos.writeUB(1, trackAsMenu ? 1 : 0);

         ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
         OutputStream os2 = baos2;
         byte origbrdata[] = null;
         if (Main.DEBUG_COPY) {
            SWFInputStream sis = new SWFInputStream(bais, version);
            int len = sis.readUI16();
            if (len != 0) {
               origbrdata = sis.readBytes(len - 2);
               os2 = new CopyOutputStream(os2, new ByteArrayInputStream(origbrdata));
            }
         }
         SWFOutputStream sos2 = new SWFOutputStream(os2, version);
         sos2.writeBUTTONRECORDList(characters, true);
         sos2.close();
         byte brdata[] = baos2.toByteArray();
         if (Main.DEBUG_COPY) {
            if (origbrdata != null) {
               if (origbrdata.length != brdata.length) {
                  /*throw nso*/
               }
            }
         }
         if (Main.DEBUG_COPY) {
            sos = new SWFOutputStream(baos, version);
         }
         if ((actions == null) || (actions.isEmpty())) {
            sos.writeUI16(0);
         } else {
            sos.writeUI16(2 + brdata.length);
         }
         sos.write(brdata);
         if (Main.DEBUG_COPY) {
            sos = new SWFOutputStream(new CopyOutputStream(baos, bais), version);
         }
         sos.writeBUTTONCONDACTIONList(actions);
         sos.close();
      } catch (IOException e) {
         Logger.getLogger(DefineButton2Tag.class.getName()).log(Level.SEVERE, null, e);
      }
      return baos.toByteArray();
   }

   /**
    * Returns all sub-items
    *
    * @return List of sub-items
    */
   public List<Object> getSubItems() {
      List<Object> ret = new ArrayList<Object>();
      ret.addAll(actions);
      return ret;
   }

   /**
    * Returns number of sub-items
    *
    * @return Number of sub-items
    */
   @Override
   public int getItemCount() {
      return actions.size();
   }

   @Override
   public Set<Integer> getNeededCharacters() {
      HashSet<Integer> needed = new HashSet<Integer>();
      for (BUTTONRECORD r : characters) {
         needed.add(r.characterId);
      }
      return needed;
   }

   @Override
   public RECT getRect(HashMap<Integer, CharacterTag> allCharacters) {
      RECT rect = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
      for (BUTTONRECORD r : characters) {
         CharacterTag ch = allCharacters.get(r.characterId);
         if (ch instanceof BoundedTag) {
            RECT r2 = ((BoundedTag) ch).getRect(allCharacters);
            rect.Xmin = Math.min(r2.Xmin, rect.Xmin);
            rect.Ymin = Math.min(r2.Ymin, rect.Ymin);
            rect.Xmax = Math.max(r2.Xmax, rect.Xmax);
            rect.Ymax = Math.max(r2.Ymax, rect.Ymax);
         }
      }
      return rect;
   }
}