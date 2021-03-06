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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class DefineBitsTag extends ImageTag implements TagChangedListener {

    public static final int ID = 6;

    public static final String NAME = "DefineBits";

    @SWFType(BasicType.UI8)
    public ByteArrayRange jpegData;

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineBitsTag(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
        jpegData = ByteArrayRange.EMPTY;
        forceWriteAsLong = true;
    }

    public DefineBitsTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        jpegData = sis.readByteRangeEx(sis.available(), "jpegData");
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterID);
            sos.write(jpegData);
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    @Override
    public void setImage(byte[] data) {
        throw new UnsupportedOperationException("Set image is not supported for DefineBits");
    }

    @Override
    public boolean importSupported() {
        // importing a new image will replace the current DefineBitsTag with a new DefineBitsJPEG2Tag
        return true;
    }

    @Override
    public InputStream getOriginalImageData() {
        if (swf.getJtt() != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] jttdata = swf.getJtt().jpegData;
                if (jttdata.length != 0) {
                    int jttErrorLength = hasErrorHeader(jttdata) ? 4 : 0;
                    baos.write(jttdata, jttErrorLength, jttdata.length - jttErrorLength - 2);
                }

                int errorLength = hasErrorHeader(jpegData) ? 4 : 0;
                baos.write(jpegData.getArray(), jpegData.getPos() + errorLength, jpegData.getLength() - errorLength);

                return new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException ex) {
                // this should never happen, since IOException comes from OutputStream, but ByteArrayOutputStream should never throw it
                throw new Error(ex);
            }
        }

        return null;
    }

    @Override
    public SerializableImage getImage() {
        if (cachedImage != null) {
            return cachedImage;
        }

        InputStream imageStream = getOriginalImageData();
        if (imageStream != null) {
            try {
                BufferedImage image = ImageHelper.read(imageStream);
                if (image == null) {
                    Logger.getLogger(DefineBitsTag.class.getName()).log(Level.SEVERE, "Failed to load image");
                    return null;
                }

                SerializableImage ret = new SerializableImage(image);
                if (Configuration.cacheImages.get()) {
                    cachedImage = ret;
                }

                return ret;
            } catch (IOException ex) {
                Logger.getLogger(DefineBitsTag.class.getName()).log(Level.SEVERE, "Failed to get image", ex);
            }
        }

        return null;
    }

    @Override
    public ImageFormat getImageFormat() {
        return ImageFormat.JPEG;
    }

    @Override
    public void handleEvent(Tag tag) {
        // cache should be cleared when Jtt tag changes
        clearCache();
    }
}
