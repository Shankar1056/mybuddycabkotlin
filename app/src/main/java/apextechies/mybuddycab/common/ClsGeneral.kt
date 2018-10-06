package apextechies.mybuddycab.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

import org.json.JSONObject

import java.util.ArrayList

object ClsGeneral {
    private var mContext: Context? = null


    fun setPreferences(context: Context, key: String, value: String) {
        mContext = context
        val editor = mContext!!.getSharedPreferences(
                "WED_APP", Context.MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.commit()
    }

}
