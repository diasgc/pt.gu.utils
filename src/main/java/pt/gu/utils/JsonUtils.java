package pt.gu.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();
    private static final boolean DBG = false;

    @Nullable
    public static JSONObject open(File file){
        try {
                String s = FileUtils.readFile(file.getAbsolutePath(), null);
            return s == null ? null : new JSONObject(s);
        } catch (Exception ignore){}
        return null;
    }

    public static JSONObject getJsonObjectInPath(JSONObject root, String... paths) {
        JSONObject o = root;
        for (String p : paths){
            try {
                o = o.getJSONObject(p);
            } catch (JSONException e){
                if (DBG) Log.e(TAG,e.toString());
            }
        }
        return o;
    }

    public static Object getObjectInPath(JSONObject root, String... paths) {
        Object o = root;
        for (String p : paths){
            try {
                if (o instanceof JSONObject)
                    o = ((JSONObject)o).get(p);
                else
                    return o;
            } catch (JSONException e){
                if (DBG) Log.e(TAG,e.toString());
            }
        }
        return o;
    }

    public static ArrayList<String> listKeys(JSONObject root){
        ArrayList<String> out = new ArrayList<>();
        final Iterator<String> i = root.keys();
        while (i.hasNext())
            out.add(i.next());
        return out;
    }

    public static void put(JSONObject root, String key, Object value) {
        try {
            root.put(key,value);
        } catch (JSONException e){
            if (DBG) Log.e(TAG,e.toString());
        }
    }

    public static void putString(JSONObject root, String key, String value) {
        try {
            root.put(key,value);
        } catch (JSONException e){
            if (DBG) Log.e(TAG,e.toString());
        }
    }

    public static JSONObject openAsset(Context context, String path) {
        try {
            return new JSONObject(FileUtils.readAsset(context,path,""));
        } catch (JSONException e){
            if (DBG) Log.e(TAG,e.toString());
        }
        return null;
    }

    @Nullable
    public static String getString(JSONObject rootJson, String pathJson){
        if (pathJson.startsWith("/"))
            pathJson = pathJson.substring(1);
        if (pathJson.indexOf('/') < 0)
            return getValue(rootJson,pathJson,null);
        Object current = rootJson;
        for (String segment : pathJson.split("/")){
            try {
                if ((current = ((JSONObject) current).get(segment)) instanceof JSONObject) {
                    continue;
                }
                else if (current instanceof String)
                    return (String) current;
                else
                    break;
            } catch (Exception e){
                if (DBG) Log.e(TAG,e.toString());
                break;
            }
        }
        return null;
    }

    public static String getValue(JSONObject rootJson, String key, String defVal){
        try {
            return rootJson.getString(key);
        } catch (Exception e){
            if (DBG) Log.e(TAG,e.toString());
        }
        return defVal;
    }

    public static float parseFloat(JSONObject root, String path, float defValue) {
        return TypeUtils.parseFloat(getString(root,path),defValue);
    }

    public static int parseInt(JSONObject root, String path, int defValue) {
        return TypeUtils.parseInt(getString(root,path),defValue);
    }


    public static String[] toStringArray(JSONArray o) {
        List<String> result = new ArrayList<>();
        final int len = o.length();
        String s;
        for (int i = 0 ; i < len ; i++){
            try {
                if ((s = o.getString(i)) != null)
                    result.add(s);
            } catch (JSONException e){
                if (DBG) Log.e(TAG,e.toString());
            }
        }
        return result.toArray(new String[0]);
    }
}
