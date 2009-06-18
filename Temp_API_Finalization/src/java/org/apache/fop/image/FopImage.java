/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.image;

import java.io.InputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.Color;

/**
 * Fop image interface for loading images.
 *
 * @author Eric SCHAEFFER
 */
public interface FopImage {
    /**
     * Flag for loading dimensions.
     */
    int DIMENSIONS = 1;

    /**
     * Flag for loading original data.
     */
    int ORIGINAL_DATA = 2;

    /**
     * Flag for loading bitmap data.
     */
    int BITMAP = 4;

    /**
     * Get the mime type of this image.
     * This is used so that when reading from the image it knows
     * what type of image it is.
     *
     * @return the mime type string
     */
    String getMimeType();

    /** @return the original URI used to access this image. */
    String getOriginalURI();
    
    /**
     * Load particular inforamtion for this image
     * This must be called before attempting to get
     * the information.
     *
     * @param type the type of loading required
     * @return boolean true if the information could be loaded
     */
    boolean load(int type);

    /**
     * Returns the image width.
     * @return the width in pixels
     */
    int getWidth();

    /**
     * Returns the image height.
     * @return the height in pixels
     */
    int getHeight();
    
    /** 
     * @return the intrinsic image width (in millipoints) 
     */
    int getIntrinsicWidth();

    /**
     *  @return the intrinsic image width (in millipoints) 
     */
    int getIntrinsicHeight();

    /**
     * @return the horizontal bitmap resolution (in dpi)
     */
    double getHorizontalResolution();
    
    /**
     * @return the vertical bitmap resolution (in dpi)
     */
    double getVerticalResolution();

    /**
     * Returns the color space of the image.
     * @return the color space
     */
    ColorSpace getColorSpace();

    /**
     * Returns the ICC profile.
     * @return the ICC profile, null if none is available
     */
    ICC_Profile getICCProfile();

    /**
     * Returns the number of bits per pixel for the image.
     * @return the number of bits per pixel
     */
    int getBitsPerPixel();

    /**
     * Indicates whether the image is transparent.
     * @return True if it is transparent
     */
    boolean isTransparent();

    /**
     * For transparent images. Returns the transparent color.
     * @return the transparent color
     */
    Color getTransparentColor();

    /**
     * Indicates whether the image has a Soft Mask (See section 7.5.4 in the
     * PDF specs)
     * @return True if a Soft Mask exists
     */
    boolean hasSoftMask();

    /**
     * For images with a Soft Mask. Returns the Soft Mask as an array.
     * @return the Soft Mask
     */
    byte[] getSoftMask();

    /**
     * Returns the decoded and uncompressed image as a array of
     * width * height * [colorspace-multiplicator] pixels.
     * @return the bitmap
     */
    byte[] getBitmaps();
    /**
     * Returns the size of the image.
     * width * (bitsPerPixel / 8) * height, no ?
     * @return the size
     */
    int getBitmapsSize();

    /**
     * Returns the encoded/compressed image as an array of bytes.
     * @return the raw image
     */
    byte[] getRessourceBytes();

    /**
     * Returns the number of bytes of the raw image.
     * @return the size in bytes
     */
    int getRessourceBytesSize();

    /**
     * Image info class.
     * Information loaded from analyser and passed to image object.
     */
    public static class ImageInfo {
        /** InputStream to load the image from */
        public InputStream inputStream;
        /** Original URI the image was accessed with */
        public String originalURI;
        /** image width (in pixels) */
        public int width;
        /** image height (in pixels) */
        public int height;
        /** horizontal bitmap resolution (in dpi) */
        public double dpiHorizontal = 72.0f;
        /** vertical bitmap resolution (in dpi) */
        public double dpiVertical = 72.0f;
        /** implementation-specific data object (ex. a SVG DOM for SVG images) */
        public Object data;
        /** MIME type of the image */
        public String mimeType;
        /** implementation-specific String (ex. the namespace for XML-based images) */
        public String str;
    }

}

