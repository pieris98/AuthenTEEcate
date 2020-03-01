package com.pieris.authenteecate

import CustomCallback
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

class HttpsPostAsyncTask(postData: Map<String, String>?,type:RequestType ,callback:CustomCallback ) :
    AsyncTask<String, Void, Void>() {    // This is the JSON body of the post
    private var response="response initialised"
    internal var postData: JSONObject? =
        null// This is a constructor that allows you to pass in the JSON body
    internal var type:RequestType?=null
    internal var callback:CustomCallback?=null

    init {
        if (postData != null) {
            this.postData = JSONObject(postData)
        }
        if (type != null) {
            this.type= type
        }

        if (callback!= null) {
            this.callback= callback
        }
    }

    private fun convertInputStreamToString(inputStream: InputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line:String?=null
        try {
            while ({ line = bufferedReader.readLine(); line }() != null)  {
                sb.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return sb.toString()
    }



    // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
    override fun doInBackground(vararg params: String): Void? {
        // Create the urlConnection
        var urlConnection:HttpsURLConnection?=null
        try {
            // This is getting the url from the string we passed in
            val url = URL(params[0])

            // Create the urlConnection
            urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.hostnameVerifier= HostnameVerifier { hostname, session -> true}
            urlConnection.setDoInput(true)
            urlConnection.setDoOutput(true)

            urlConnection.setRequestProperty("Content-Type", "application/json")

            urlConnection.setRequestMethod("POST")


            // OPTIONAL - Sets an authorization header
            urlConnection.setRequestProperty("Authorization", "someAuthString")

            // Send the post body
            if (this.postData != null) {
                val writer = OutputStreamWriter(urlConnection.getOutputStream())
                writer.write(postData!!.toString())
                writer.flush()
            }

            val statusCode = urlConnection.getResponseCode()

            if (statusCode == HttpURLConnection.HTTP_OK) {

                val inputStream = BufferedInputStream(urlConnection.getInputStream())

                response = convertInputStreamToString(inputStream)

                // From here you can convert the string to JSON with whatever JSON parser you like to use               // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method            }
                when (type) {
                    RequestType.REQUEST_TYPE_1->
                        // Use the response to create the object you need
                        callback?.completionHandler(true, type!!, response)


                    RequestType.REQUEST_TYPE_2->
                        // Use the response to create the object you need
                        callback?.completionHandler(true, type!!, response)
                    else->callback?.completionHandler(true, type!!, response)
                }


                // else {
                // Status code is not 200
                // Do something to handle the error
            }
            else{
                response="RESPONSE ERROR CODE ${statusCode}"
            }


        } catch (e: Exception) {
            Log.d(ContentValues.TAG,e.message)
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
    }
}