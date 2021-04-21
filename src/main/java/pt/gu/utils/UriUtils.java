package pt.gu.utils;

import android.net.Uri;

import java.util.List;

public class UriUtils {

    public static Uri getContentUri(String authority, String... paths){
        Uri.Builder b = new Uri.Builder()
                .scheme("content")
                .authority(authority);
        setPathSegments(b, paths);
        return b.build();
    }

    public static void setPathSegments(Uri.Builder b, String... paths){
        if (paths != null && paths.length > 0) {
            for (String p : paths) {
                b.appendPath(p);
            }
        }
    }

    public static void setPathSegments(Uri.Builder b, List<String> paths){
        if (paths != null && paths.size() > 0) {
            for (String p : paths)
                b.appendPath(p);
        }
    }

    public static Uri setPathSegments(Uri src, List<String> paths){
        Uri.Builder b = new Uri.Builder().scheme(src.getScheme()).authority(src.getAuthority());
        setPathSegments(b, paths);
        return b.build();
    }

    public static Uri setPathSegments(Uri src, String... paths){
        Uri.Builder b = new Uri.Builder().scheme(src.getScheme()).authority(src.getAuthority());
        setPathSegments(b, paths);
        return b.build();
    }

    public static Uri setLastSegment(Uri src, String newLastSegment){
        List<String> paths = src.getPathSegments();
        if (paths != null && paths.size() > 0) {
            paths.set(paths.size() - 1, newLastSegment);
            return setPathSegments(src, paths);
        }
        return src.buildUpon().appendPath(newLastSegment).build();
    }

    public static Uri removeLastSegmentPath(Uri uri) {
        List<String> paths = uri.getPathSegments();
        String query = uri.getQuery();
        Uri.Builder b = new Uri.Builder();
        b.scheme(uri.getScheme()).authority(uri.getAuthority());
        for (int i = 0 ; i < paths.size() -1 ; i++)
            b.appendPath(paths.get(i));
        b.query(query);
        return b.build();
    }
}
