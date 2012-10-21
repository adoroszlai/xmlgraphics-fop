/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.pdf;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;

import org.apache.fop.pdf.DCTFilter;
import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilter;
import org.apache.fop.pdf.PDFFilterList;

/**
 * PDFImage implementation for the PDF renderer which handles raw JPEG images.
 */
public class ImageRawJPEGAdapter extends AbstractImageAdapter {

    private PDFFilter pdfFilter = null;

    /**
     * Creates a new PDFImage from an Image instance.
     * @param image the JPEG image
     * @param key XObject key
     */
    public ImageRawJPEGAdapter(ImageRawJPEG image, String key) {
        super(image, key);
    }

    /**
     * Returns the {@link ImageRawJPEG} instance for this adapter.
     * @return the image instance
     */
    public ImageRawJPEG getImage() {
        return ((ImageRawJPEG)this.image);
    }
    
    /** {@inheritDoc} */
    public void setup(PDFDocument doc) {
        pdfFilter = new DCTFilter();
        pdfFilter.setApplied(true);

        super.setup(doc);
    }

    /** {@inheritDoc} */
    public PDFDeviceColorSpace getColorSpace() {
        // DeviceGray, DeviceRGB, or DeviceCMYK
        return toPDFColorSpace(getImageColorSpace());
    }

    /** {@inheritDoc} */
    public int getBitsPerComponent() {
        return 8;
    }

    /** @return true for CMYK images generated by Adobe Photoshop */
    public boolean isInverted() {
        return getImage().isInverted();
    }
    
    /** {@inheritDoc} */
    public PDFFilter getPDFFilter() {
        return pdfFilter;
    }
    
    /** {@inheritDoc} */
    public void outputContents(OutputStream out) throws IOException {
        getImage().writeTo(out);
    }

    /** {@inheritDoc} */
    public String getFilterHint() {
        return PDFFilterList.JPEG_FILTER;
    }

}

