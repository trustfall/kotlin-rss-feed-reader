package com.example.trustfall.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary : String = ""
    var imageUrl: String = ""

    override fun toString(): String {
        return """
            name = $name
            artist = $name
            releaseDate = $releaseDate
            imageUrl = $imageUrl
        """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    // by lazy performs the operation when the var is first assigned a value to avoid nulls
    private var downloadData: DownloadData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
    }

    private fun downloadUrl(feedUrl: String) {
        Log.d(TAG, "downloadUrl starting AsyncTask")
        downloadData = DownloadData(this, xmlListView)
        downloadData?.execute(feedUrl)
        Log.d(TAG, "downloadUrl: done")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val feedUrl: String

        when (item.itemId) {
            R.id.mnuFree -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"
            R.id.mnuPaid -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml"
            R.id.mnuSongs -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml"
            else -> return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    companion object {
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter
            }

            override fun doInBackground(vararg params: String?): String {
                Log.d(TAG, "doInBackground: Starts with ${params[0]}")
                val rssFeed = downloadXML(params[0])
                if(rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            private fun downloadXML(urlPath: String?): String {
                return URL(urlPath).readText()
            }
        }
    }


}
