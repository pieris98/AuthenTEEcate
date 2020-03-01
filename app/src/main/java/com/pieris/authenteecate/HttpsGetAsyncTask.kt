package com.pieris.authenteecate

import android.content.ContentValues
import android.os.AsyncTask
import android.util.Log
import java.io.*

import java.net.HttpURLConnection
import java.net.URL

import javax.net.ssl.*
import kotlin.properties.Delegates



class HttpsGetAsyncTask(context:SSLContext): AsyncTask<String, Void, String>() {
    private var context:SSLContext by Delegates.notNull<SSLContext>()
    init{
        this.context=context
    }
    private var response="response initialised"

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


    override fun onPreExecute() {
        super.onPreExecute()
    }


    override  fun doInBackground(vararg params:String):String {
        val stringUrl = params[0]
        lateinit var result: String
        var line: String? = null
        var urlConnection: HttpsURLConnection? = null

        try {
            //Create a URL object holding our url
            val myUrl = URL(stringUrl) //Create a connection
            urlConnection = myUrl.openConnection() as HttpsURLConnection //Set methods and timeouts
            urlConnection.hostnameVerifier= HostnameVerifier { hostname, session -> true}
            urlConnection.sslSocketFactory=context?.socketFactory
            urlConnection.setRequestMethod("GET")
            urlConnection.setReadTimeout(1500)
            urlConnection.setConnectTimeout(15000)
            //Connect to our url
            urlConnection.connect() //Create a new InputStreamReader
            val statusCode = urlConnection.getResponseCode()

            if (statusCode == HttpURLConnection.HTTP_OK) {

                val inputStream = BufferedInputStream(urlConnection.getInputStream())

                response = convertInputStreamToString(inputStream)

            } else {
                response = "RESPONSE ERROR CODE ${statusCode}"
            }
        } catch (e: Exception) {
            Log.d(ContentValues.TAG,e.message)
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }
        return response
    }

    override  fun onPostExecute(result:String) {
        super.onPostExecute(result)
    }

}
