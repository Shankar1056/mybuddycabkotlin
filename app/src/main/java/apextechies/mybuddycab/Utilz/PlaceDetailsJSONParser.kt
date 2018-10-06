package apextechies.mybuddycab.Utilz

import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap

class PlaceDetailsJSONParser {

    /** Receives a JSONObject and returns a list  */
    fun parse(jObject: JSONObject): List<HashMap<String, String>> {

        var lat = java.lang.Double.valueOf(0.0)
        var lng = java.lang.Double.valueOf(0.0)

        val hm = HashMap<String, String>()
        val list = ArrayList<HashMap<String, String>>()

        try {
            lat = jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat") as Double
            lng = jObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng") as Double

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        hm["lat"] = java.lang.Double.toString(lat)
        hm["lng"] = java.lang.Double.toString(lng)

        list.add(hm)

        return list
    }
}
