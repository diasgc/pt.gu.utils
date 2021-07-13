package pt.gu.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class CanvasUtils {

    public static class PolarF {

        public static final float PI2 = (float) Math.PI * 2;

        public float rad;
        public float ang;

        public PolarF(float rad, float ang){
            this.rad = rad;
            this.ang = ang;
        }

        public PolarF(PolarF polarF){
            rad = polarF.rad;
            ang = polarF.ang;
        }

        public PolarF(float x, float y, PointF center){
            fromPointF(new PointF(x,y),center);
        }

        public PolarF(float x, float y, float cx, float cy){
            fromPointF(new PointF(x,y),new PointF(cx,cy));
        }

        public PolarF(PointF pointF, PointF center){
            fromPointF(pointF,center);
        }

        public void set(float rad, float ang){
            this.rad = rad;
            this.ang = ang;
        }

        public void set(PolarF polarF) {
            this.rad = polarF.rad;
            this.ang = polarF.ang;
        }

        public void addAngle(float add){
            this.ang = (ang + add + PI2) % PI2;
        }

        public void fromPointF(PointF xy, PointF center){
            this.rad = (float) Math.hypot(xy.x-center.x,xy.y-center.y);
            this.ang = (float) Math.atan2(xy.y-center.y, xy.x-center.x);
            if (ang < 0)
                ang += (float) Math.PI * 2;
        }

        public float getX(PointF center){
            return (float)(center.x + rad*Math.cos(ang));
        }

        public float getY(PointF center){
            return (float)(center.y + rad*Math.sin(ang));
        }

        public static float getRad(PointF xy, PointF center){
            return  (float) Math.hypot(xy.x-center.x,xy.y-center.y);
        }

        public PointF toPointF(PointF center) {
            return new PointF(center.x + rad*(float)Math.cos(ang),center.y + rad*(float)Math.sin(ang));
        }

        @Override
        public String toString() {
            return "("+ rad +" , "+ ang +")";
        }

        public float getDegrees() {
            return (ang * 360f / PI2 + 360f) % 360f;
        }
    }

    public static class Helper {

        private final double pi2 = Math.PI * 2;
        private final Point mSize = new Point(0,0);
        private final PointF mCenter = new PointF(0,0);
        private float maxRad = 0;

        public Helper(){}

        public Helper(int width, int height){
            setSize(width, height);
        }

        public void setSize(int w, int h) {
            mSize.set(w,h);
            mCenter.set(w/2f,h/2f);
            maxRad = Math.min(mCenter.x,mCenter.y);
        }

        public void setCenter(float x, float y){
            mCenter.set(x,y);
        }

        public void drawLine(Canvas canvas, PolarF start, PolarF end, Paint p){
            canvas.drawLine(start.getX(mCenter),start.getY(mCenter),
                    end.getX(mCenter),end.getY(mCenter),p);
        }

        public void drawLine(Canvas canvas, PolarF start, float lenght, @NonNull Paint p){
            drawLine(canvas,start,new PolarF(start.rad+lenght,start.ang),p);
        }

        public void drawCircle(Canvas canvas, PolarF p, float rad, @NonNull Paint paint){
            canvas.drawCircle(p.getX(mCenter),p.getY(mCenter),rad,paint);
        }

        public void drawCircle(Canvas canvas, PolarF p, float radOffset, float rad, @NonNull Paint paint){
            PolarF p2 = new PolarF(p.rad+radOffset,p.ang);
            canvas.drawCircle(p2.getX(mCenter),p2.getY(mCenter),rad,paint);
        }

        public void drawCircle(Canvas canvas, float dist, float ang, float rad, @NonNull Paint paint){
            PolarF p = new PolarF(dist,ang);
            canvas.drawCircle(p.getX(mCenter),p.getY(mCenter),rad,paint);
        }

        public void drawArc(Canvas canvas, float rad, float startAng, float sweepAng, Paint paint){
            canvas.drawArc(mCenter.x-rad,mCenter.y-rad,mCenter.x+rad,mCenter.y+rad,startAng, sweepAng,true,paint);
        }

        public void drawText(Canvas canvas, String text, float xOffset, float yOffset, float txtSizeSp, int color){
            TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            tp.setTextAlign(TextPaint.Align.CENTER);
            tp.setTextSize(txtSizeSp);
            tp.setColor(color);
            canvas.drawText(text,mCenter.x + xOffset, mCenter.y + yOffset, tp);
        }

        public void drawText(Canvas canvas, String text, float xOffset, float yOffset, @Nullable Float txtSizeSp, TextPaint tp){
            if (txtSizeSp != null)
                tp.setTextSize(txtSizeSp);
            canvas.drawText(text,mCenter.x + xOffset, mCenter.y + yOffset, tp);
        }

        public float getRad(PointF point){
            return (float) Math.hypot(mCenter.x-point.x,mCenter.y-point.y);
        }

        public float getRad(float x, float y){
            return (float) Math.hypot(x-mCenter.x,y-mCenter.y);
        }

        public int getAngleValue(float x, float y, int max){
            PolarF p = new PolarF(x,y,mCenter);
            return (int)(max * ((p.ang + pi2) % pi2) / pi2);
        }

        public float getAngleValueNorm(float x, float y){
            PolarF p = new PolarF(x,y,mCenter);
            return (float)((p.ang + pi2) % pi2 / pi2);
        }

        public int getWidth(){
            return mSize.x;
        }

        public int getHeight(){
            return mSize.y;
        }

        public Point getSize(){
            return mSize;
        }

        public PointF getCenter(){
            return mCenter;
        }

    }

    public static void drawGrid(Canvas c, RectF bounds, Point mesh, int color, float alpha){
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1f);
        p.setColor(color);
        p.setAlpha((int)(alpha * 255));
        PointF step = new PointF(bounds.width()/(float)mesh.x,bounds.height()/(float)mesh.y);
        c.drawRect(bounds,p);
        for (int i = 0 ; i < mesh.x; i++)
            c.drawLine(i*step.x,bounds.top,i*step.x,bounds.bottom,p);
        for (int i = 0 ; i < mesh.y; i++)
            c.drawLine(bounds.left,i*step.y,bounds.right,i*step.y,p);
    }

}
