package com.codepath.apps.restclienttemplate.models

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.codepath.apps.restclienttemplate.R
import com.codepath.apps.restclienttemplate.TwitterApplication
import com.codepath.apps.restclienttemplate.TwitterClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {

    lateinit var etCompose: EditText
    lateinit var btnTweet: Button
    lateinit var countChar: TextView

    lateinit var client: TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose = findViewById(R.id.etTweetCompose)
        btnTweet = findViewById(R.id.btnTweet)
        countChar = findViewById(R.id.countChar)

        countChar.text = "280"

        client = TwitterApplication.getRestClient(this)

        btnTweet.setOnClickListener{
            val tweetContent = etCompose.text.toString()

            if(tweetContent.isEmpty()){
                Toast.makeText(this, "Empty tweets not allowed!", Toast.LENGTH_SHORT).show()
            }
            else if(tweetContent.length > 280){
                Toast.makeText(this, "Tweet is too long! Limit is 150 characters", Toast.LENGTH_SHORT).show()
            }
            else{
                client.publishTweet(tweetContent, object:JsonHttpResponseHandler(){
                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.i(TAG, "Successfully publish tweet")

                        val tweet = Tweet.fromJson(json.jsonObject)

                        val intent = Intent()
                        intent.putExtra("tweet", tweet)
                        setResult(RESULT_OK, intent)
                        finish()
                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        response: String?,
                        throwable: Throwable?
                    ) {
                        Log.e(TAG, "Failed to publish tweet", throwable)
                    }
                })

            }
        }

        etCompose.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Fires right as the text is being changed (even supplies the range of text)
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Fires right before text is changing
            }

            override fun afterTextChanged(p0: Editable) {
                // Fires right after the text has changed
                val charLen = 280 - p0.length
                countChar.text = charLen.toString()
                if(charLen >= 0){
                    btnTweet.isEnabled = true
                    countChar.setTextColor(Color.BLACK)
                }
                else{
                    btnTweet.isEnabled = false
                    countChar.setTextColor(Color.RED)
                }
            }

        })
    }

    companion object{
        var TAG = "ComposeActivity"
    }

}