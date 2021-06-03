package pt.gu.utils;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

public class ConstUtils {

    private static JSONObject root;

    public static Map<String,String> getConstStaticFields(Class<?> clazz, boolean swapKV)  {
        ArrayMap<String,String> out = new ArrayMap<>();
        for (Field f : clazz.getDeclaredFields()) {
            try {
                if (swapKV)
                    out.put(String.valueOf(f.get(null)),f.getName());
                else
                    out.put(f.getName(), String.valueOf(f.get(null)));
            } catch (IllegalAccessException ignore) {
            }
        }
        return out;
    }

    @Nullable
    public static ArrayMap<String,String> getConsts(Context context, String path){
        checkRoot(context);
        ArrayMap<String,String> out = new ArrayMap<>();
        try {
            JSONObject o = root;
            for (String s : path.split("/")) {
                o = o.getJSONObject(s);
            }
            if (o != null) {
                Iterator<String> keys = o.keys();
                String k;
                while (keys.hasNext())
                    out.put(k = keys.next(), o.get(k).toString());
            }
        } catch (JSONException e){}
        return out;
    }

    private static void checkRoot(Context context){
        final String s;
        try {
            if (root == null && (s = StringUtils.readStream(context.getAssets().open("consts.json"))).length() > 2){
                root = new JSONObject(s);
            }
        } catch (IOException | JSONException ignore){}
    }
}
