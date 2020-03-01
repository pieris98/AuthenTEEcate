package com.pieris.authenteecate

import CustomCallback
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_register.*
import java.io.InputStream
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

enum class RequestType {
    REQUEST_TYPE_1,
    REQUEST_TYPE_2
}


class RegisterActivity : AppCompatActivity(),CustomCallback {
    val tag= "Register Activity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Load CA from file
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = getResources().openRawResource(
            getResources().getIdentifier("tee",
                "raw", getPackageName()))
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        System.out.println("ca=" + ca.subjectDN)

// Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

// Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

// Create an SSLContext that uses our TrustManager
        val context: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }


        fun newRegister() {
            val email = emailTextRegister.text.toString()
            val password = passwordRegister.text.toString()
            val passwordConfirm = passwordRegister2.text.toString()
            if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter text in Email and Password fields",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            if (password != passwordConfirm) {
                Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show()
                return
            }
            Log.d(tag, "Email is: $email")
            Log.d(tag, "Password: $password")
            var keyStore= KeyStore.getInstance("AndroidKeystore")
            //generating private key
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    "masterkey",
                    KeyProperties.PURPOSE_SIGN)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS).setUserConfirmationRequired(true).setIsStrongBoxBacked(true)
                    .build())

            val keypair=keyPairGenerator.generateKeyPair()

            val baseUrl = ("https://${addressField.text}")
            var  postData= HashMap<String,String> ()
            postData["email"]= emailTextRegister.text.toString()
            postData["password"]= passwordRegister.text.toString()
            postData["key"]=keypair.public.encoded.toString()
            val task = HttpsPostAsyncTask(postData, RequestType.REQUEST_TYPE_2,this)
            //@TODO change /test/post to the new API of flask
            task.execute(baseUrl + "/test/post")
        }


        registerButton.setOnClickListener(){
            newRegister()
        }
    }


    override fun completionHandler(success: Boolean?, type: RequestType, response:Any) {
        when (type){
            RequestType.REQUEST_TYPE_1-> runOnUiThread(Runnable {
                //test_response_get.text=response.toString()
            })
            RequestType.REQUEST_TYPE_2-> runOnUiThread(Runnable {
                if(response.equals("Register successful!")) {
                    //build an alert dialog
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Registration Successful!")
                    builder.setMessage("You can now login to AuthenTEEcate!")
                    builder.setPositiveButton("Take me to Login!") { _, _ ->
                        //@TODO add sharedprefs edit of firstLaunch boolean
                        finish()//move back to LoginActivity
                    }
                    builder.show()
                }
            });
        }
    }

}
