package pt.gu.utils;

import android.content.res.XmlResourceParser;

import org.w3c.dom.NodeList;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class XmlUtils {

    public static boolean setParserToTagsPath(XmlResourceParser parser, String path){
        String[] p = path.split("/");
        int e = -1, i = 0;
        try {
            while (e != XmlResourceParser.END_DOCUMENT) {
                if (e == XmlResourceParser.START_TAG && p[i].equals(parser.getName()) && i == parser.getDepth() - 1){
                    i++;
                    if (i == p.length)
                        return true;
                }
                e = parser.next();
            }
        } catch (IOException | XmlPullParserException ignore){}
        return false;
    }

    public static void skipTag(XmlResourceParser parser) throws IOException, XmlPullParserException{
        final String tag = parser.getName();
        final int depth = parser.getDepth();
        int e = -1;
        while (e != XmlResourceParser.END_TAG && parser.getDepth() == depth|| e != XmlResourceParser.END_DOCUMENT){
            e = parser.nextTag();
        }
    }
}
