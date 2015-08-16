//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.vijayrawatsan.ipresultparser;

import com.itextpdf.text.pdf.parser.*;

public class CustomSimpleTextExtractionStrategy implements TextExtractionStrategy {
    private Vector lastStart;
    private Vector lastEnd;
    private final StringBuffer result = new StringBuffer();

    public CustomSimpleTextExtractionStrategy() {
    }

    public void beginTextBlock() {
    }

    public void endTextBlock() {
    }

    public String getResultantText() {
        return this.result.toString();
    }

    protected final void appendTextChunk(CharSequence text) {
        this.result.append(text);
    }

    public void renderText(TextRenderInfo renderInfo) {
        boolean firstRender = this.result.length() == 0;
        boolean hardReturn = false;
        LineSegment segment = renderInfo.getBaseline();
        Vector start = segment.getStartPoint();
        Vector end = segment.getEndPoint();
        if (!firstRender) {
            Vector x1 = this.lastStart;
            Vector x2 = this.lastEnd;
            float dist = x2.subtract(x1).cross(x1.subtract(start)).lengthSquared() / x2.subtract(x1).lengthSquared();
            float sameLineThreshold = 1.0F;
            if (dist > sameLineThreshold) {
                hardReturn = true;
            }
        }

        if (hardReturn) {
            this.appendTextChunk("\n");
        } else if (!firstRender && this.result.charAt(this.result.length() - 1) != 32 && renderInfo.getText().length() > 0 && renderInfo.getText().charAt(0) != 32) {
            float spacing = this.lastEnd.subtract(start).length();
            if (spacing > renderInfo.getSingleSpaceWidth() / 2.0F) {
                this.appendTextChunk("___");
            }
        }

        this.appendTextChunk(renderInfo.getText());
        this.lastStart = start;
        this.lastEnd = end;
    }

    public void renderImage(ImageRenderInfo renderInfo) {
    }
}
