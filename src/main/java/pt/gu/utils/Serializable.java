package pt.gu.utils;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("unused")
public interface Serializable {

    @Override
    String toString();

    JSONObject toJson();

    Element toElement(Document root);

}
