package pt.gu.utils;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.File;

public class JsonUtils {

    @Nullable
    public static JSONObject open(File file){
        try {
            String s = FileUtils.readFile(file.getAbsolutePath(), null);
            return s == null ? null : new JSONObject(s);
        } catch (Exception ignore){}
        return null;
    }
}
