package pt.gu.utils;

import android.graphics.PointF;

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
}
