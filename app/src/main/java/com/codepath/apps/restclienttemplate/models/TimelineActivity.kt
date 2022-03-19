package com.codepath.apps.restclienttemplate.models

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.R
import com.codepath.apps.restclienttemplate.TwitterApplication
import com.codepath.apps.restclienttemplate.TwitterClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import org.json.JSONException

class TimelineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient

    lateinit var rvTweets: RecyclerView

    lateinit var adapter: TweetsAdapter

    lateinit var swipeContainer: SwipeRefreshLayout


    val tweets = ArrayList<Tweet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)
        client = TwitterApplication.getRestClient(this)

        swipeContainer = findViewById(R.id.swipeContainer)

        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing timeline")
            populateHomeTimeline()
        }

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light)

        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)

        rvTweets.layoutManager = LinearLayoutManager(this)
        rvTweets.adapter = adapter

        populateHomeTimeline()

        //set color
        val actionBar = supportActionBar
        val colorDrawable = ColorDrawable(Color.parseColor("#1DA1F2"))

        actionBar?.setBackgroundDrawable(colorDrawable)
    }

    var editActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If the user comes back to this activity from EditActivity
        // with no error or cancellation
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // Get the data passed from EditActivity
            if (data != null){
                val tweet = data?.extras.getParcelable("tweet") as Tweet

                //Update timeline
                //Modifying the data source of tweet
                tweets.add(0, tweet)

                //Update adapter
                adapter.notifyItemInserted(0)
                rvTweets.smoothScrollToPosition(0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.compose){
            val intent = Intent(this, ComposeActivity::class.java)
            editActivityResultLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun populateHomeTimeline(){
        client.getHomeTimeline(object: JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                Log.i(TAG, "onSuccess $json")

                val jsonArray = json.jsonArray
                try{
                    //clear out our currently fetched tweets
                    adapter.clear()
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    adapter.notifyDataSetChanged()
                    swipeContainer.setRefreshing(false)
                }catch(e: JSONException){
                    Log.e(TAG, "JSON Exception $e")
                }
            }
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $statusCode")
            }
        })
    }

    companion object{
        val TAG = "TimelineActivity"
    }
}