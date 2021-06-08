package pt.gu.utils;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Pair;

import androidx.annotation.Nullable;

public class MathUtils {

    @Nullable
    public static PointF poly1(PointF p1, PointF p2){
        final float dx, dy;
        return (dy = p2.y - p1.y) == 0 || (dx = p2.x - p1.x) == 0 ?
                null : new PointF(dy/dx,dx/p1.x);
    }

    public static float poly1(float x, PointF p1, PointF p2){
        final float dx, dy;
        return (dy = p2.y - p1.y) == 0 || (dx = p2.x - p1.x) == 0 ?
                x : x * dy / dx + dx / p1.x;
    }
    
    public static PointF[] transform(RectF dst, PointF... p){
        if (p.length == 0)
            return p;
        final PointF[] out = new PointF[p.length];
        final RectF inRect = getBounds(p);
        final float dx = (dst.right - dst.left) / (inRect.right - inRect.left);
        final float dy = (dst.top - dst.bottom) / (inRect.top - inRect.bottom);
        for (int i = 0 ; i < p.length ; i++){
            out[i] = new PointF((p[i].x - inRect.left) * dx + dst.left,(p[i].y - inRect.bottom) * dy + dst.bottom);
        }
        return out;
    }
    
    public static RectF getBounds(PointF... p){
        RectF f = new RectF();
        for (PointF p0 : p){
            if (p0.x < f.left) f.left = p0.x;
            if (p0.x > f.right) f.right = p0.x;
            if (p0.y < f.bottom) f.bottom = p0.y;
            if (p0.y > f.top) f.top = p0.y;
        }
        return f;
    }
}
