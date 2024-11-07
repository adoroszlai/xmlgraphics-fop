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

package org.apache.fop.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.batik.gvt.text.TextPaintInfo;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTGlyphVector;

/**
 * Renders the attributed character iterator of a {@link org.apache.batik.bridge.TextNode}.
 * This class draws the text directly into the PDFGraphics2D so that
 * the text is not drawn using shapes which makes the PDF files larger.
 * If the text is simple enough to draw then it sets the font and calls
 * drawString. If the text is complex or the cannot be translated
 * into a simple drawString the StrokingTextPainter is used instead.
 *
 * @version $Id$
 */
class PDFTextPainter extends NativeTextPainter {

    private static final int[] PA_ZERO = new int[4];

    private PDFGraphics2D pdf;

    private PDFTextUtil textUtil;

    private double prevVisibleGlyphWidth;

    private boolean repositionNextGlyph;

    /**
     * Create a new PDF text painter with the given font information.
     *
     * @param fi the font info
     */
    public PDFTextPainter(FontInfo fi) {
        super(fi);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isSupported(Graphics2D g2d) {
        return g2d instanceof PDFGraphics2D;
    }

    @Override
    protected void preparePainting(Graphics2D g2d) {
        pdf = (PDFGraphics2D) g2d;
    }

    @Override
    protected void saveGraphicsState() {
        pdf.saveGraphicsState();
    }

    @Override
    protected void restoreGraphicsState() {
        pdf.restoreGraphicsState();
    }

    @Override
    protected void setInitialTransform(AffineTransform transform) {
        createTextUtil();
        textUtil.concatMatrix(transform);
    }

    private void createTextUtil() {
        textUtil = new PDFTextUtil(pdf.fontInfo) {
            protected void write(String code) {
                pdf.currentStream.write(code);
            }
            protected void write(StringBuffer code) {
                pdf.currentStream.append(code);
            }
        };
    }

    @Override
    protected void clip(Shape clip) {
        pdf.writeClip(clip);
    }

    @Override
    protected void writeGlyphs(FOPGVTGlyphVector gv, GeneralPath debugShapes) throws IOException {
        if (gv.getGlyphPositionAdjustments() == null) {
            super.writeGlyphs(gv, debugShapes);
        } else {
            Point2D prevPos = null;
            AffineTransform prevGlyphTransform = null;
            font = ((FOPGVTFont) gv.getFont()).getFont();
            int[][] glyphPositionAdjustments = gv.getGlyphPositionAdjustments();
            double glyphXPos = 0f;
            double glyphYPos = 0f;
            double prevAdjustedGlyphXPos = 0f;
            double prevAdjustedGlyphYPos = 0f;
            for (int index = 0; index < gv.getNumGlyphs(); index++) {
                if (!gv.isGlyphVisible(index)) {
                    continue;
                }
                Point2D glyphPos = gv.getGlyphPosition(index);
                AffineTransform glyphTransform = gv.getGlyphTransform(index);
                int[] positionAdjust = ((index > glyphPositionAdjustments.length)
                        || (glyphPositionAdjustments[index] == null)) ? PA_ZERO : glyphPositionAdjustments[index];
                if (log.isTraceEnabled()) {
                    log.trace("pos " + glyphPos + ", transform " + glyphTransform);
                }

                positionGlyph(prevPos, glyphPos, glyphTransform != null || prevGlyphTransform != null);

                char glyph = (char) gv.getGlyphCode(index);
                double adjustedGlyphXPos = glyphXPos + positionAdjust[0];
                double adjustedGlyphYPos = glyphYPos + positionAdjust[1];
                double tdXPos = (adjustedGlyphXPos - prevAdjustedGlyphXPos) / 1000f;
                double tdYPos = (adjustedGlyphYPos - prevAdjustedGlyphYPos) / 1000f;

                textUtil.writeTd(tdXPos, tdYPos);

                writeGlyph(glyph, getLocalTransform(glyphPos, glyphTransform));

                //Update last position
                prevPos = glyphPos;
                prevGlyphTransform = glyphTransform;
                glyphXPos = glyphPos.getX() + positionAdjust[2];
                glyphYPos = glyphPos.getY() + positionAdjust[3];
                prevAdjustedGlyphXPos = adjustedGlyphXPos;
                prevAdjustedGlyphYPos = adjustedGlyphYPos;
            }
        }
    }

    @Override
    protected void beginTextObject() {
        applyColorAndPaint(tpi);
        textUtil.beginTextObject();
        boolean stroke = (tpi.strokePaint != null) && (tpi.strokeStroke != null);
        textUtil.setTextRenderingMode(tpi.fillPaint != null, stroke, false);
    }

    @Override
    protected void endTextObject() {
        textUtil.writeTJ();
        textUtil.endTextObject();
    }

    private void applyColorAndPaint(TextPaintInfo tpi) {
        Paint fillPaint = tpi.fillPaint;
        Paint strokePaint = tpi.strokePaint;
        Stroke stroke = tpi.strokeStroke;
        int fillAlpha = PDFGraphics2D.OPAQUE;
        int strokeAlpha = PDFGraphics2D.OPAQUE;
        if (fillPaint instanceof Color) {
            Color col = (Color) fillPaint;
            pdf.applyColor(col, true);
            fillAlpha = col.getAlpha();
        }
        if (strokePaint instanceof Color) {
            Color col = (Color) strokePaint;
            pdf.applyColor(col, false);
            strokeAlpha = col.getAlpha();
        }
        pdf.applyPaint(fillPaint, true);
        pdf.applyStroke(stroke);
        if (strokePaint != null) {
            pdf.applyPaint(strokePaint, false);
        }
        pdf.applyAlpha(fillAlpha, strokeAlpha);
    }

    @Override
    protected void positionGlyph(Point2D prevPos, Point2D glyphPos, boolean reposition) {
        // TODO Glyph transforms could be refined so not every char has to be painted
        // with its own TJ command (stretch/squeeze case could be optimized)
        repositionNextGlyph = (prevPos == null
                || prevPos.getY() != glyphPos.getY()
                || reposition);
        if (!repositionNextGlyph) {
            double xdiff = glyphPos.getX() - prevPos.getX();
            double effxdiff = (1000 * xdiff) - prevVisibleGlyphWidth;
            if (effxdiff != 0) {
                double adjust = (-effxdiff / font.getFontSize());
                textUtil.adjustGlyphTJ(adjust * 1000);
            }
        }
    }

    @Override
    protected void writeGlyph(char glyph, AffineTransform transform) {
        prevVisibleGlyphWidth = font.getWidth(glyph);
        boolean encodingChanging = false; // used for single byte
        if (!textUtil.isMultiByteFont(font.getFontName())) {
            int encoding = glyph / 256;
            glyph = (char) (glyph % 256);
            if (textUtil.getCurrentEncoding() != encoding) {
                textUtil.setCurrentEncoding(encoding);
                encodingChanging = true;
            }
        }
        if (repositionNextGlyph || encodingChanging) {
            textUtil.writeTJ();
            if (font != textUtil.getCurrentFont() || encodingChanging) {
                textUtil.setCurrentFont(font);
                textUtil.writeTf(font);
            }
            textUtil.writeTextMatrix(transform);
        }
        textUtil.writeTJMappedChar(glyph);
    }

}
