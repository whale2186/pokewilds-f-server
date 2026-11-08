package com.pkmngen.game;

import java.util.*;
import java.util.function.ToDoubleFunction;

/** Screen layout in uniformly scaled UI units; independent of the world camera/zoom. */
final class LanHudLayout {
    final float width, height, panelWidth, margin = 12, padding = 8;
    final float headerHeight = 28, footerHeight = 44, editorHeight = 66, lineHeight = 18;
    final boolean columns;
    LanHudLayout(int pixelsWide, int pixelsHigh) {
        float w=Math.max(1,pixelsWide), h=Math.max(1,pixelsHigh);
        float preferred=Math.max(1, Math.min(2.5f, Math.min(w/640f,h/480f)));
        float scale=Math.min(preferred, Math.min(w/320f,h/240f));
        width=w/scale; height=h/scale;
        panelWidth=Math.min(width-2*margin,Math.max(280,Math.min(480,width*.38f)));
        columns=width >= 2*panelWidth+3*margin;
    }
    float top() { return height-margin; }
    float contentWidth() { return panelWidth-2*padding; }
    float rosterX() { return columns ? width-margin-panelWidth : margin; }
    float rosterTop(boolean details) { return top()-(columns ? 0 : headerHeight+(details ? 44 : 0)+margin); }
    int rosterRows(boolean details) {
        return Math.max(1,Math.min(8,(int)((rosterTop(details)-margin-footerHeight-margin-headerHeight)/22)));
    }
    float chatBottom(boolean typing, boolean shown) { return margin+(typing ? editorHeight : shown ? footerHeight : 0)+margin; }
    int chatRows(boolean typing, boolean history, boolean details, boolean notice, boolean shown) {
        float upper=shown ? top()-headerHeight-(details ? 44 : 0)-(notice ? 28 : 0)-margin : top();
        int room=Math.max(1,(int)((upper-chatBottom(typing,shown))/lineHeight));
        return Math.min(typing ? 12 : history ? 6 : 3,room);
    }
    static List<String> wrap(String text, float maxWidth, ToDoubleFunction<String> measure) {
        List<String> rows=new ArrayList<>();
        String remaining=text;
        while(!remaining.isEmpty()) {
            int end=0;
            while(end<remaining.length() && measure.applyAsDouble(remaining.substring(0,end+1))<=maxWidth) end++;
            if(end==remaining.length()) { rows.add(remaining); break; }
            end=Math.max(1,end);
            int space=remaining.lastIndexOf(' ',end);
            if(space>0) end=space;
            rows.add(remaining.substring(0,end));
            remaining=remaining.substring(end);
            while(remaining.startsWith(" ")) remaining=remaining.substring(1);
        }
        return rows;
    }
    static String tail(String text, float maxWidth, ToDoubleFunction<String> measure) {
        int start=0;
        while(start<text.length() && measure.applyAsDouble(text.substring(start))>maxWidth) start++;
        return text.substring(start);
    }
}
