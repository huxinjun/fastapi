package org.pulp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.pulp.kotlin.activity.KtActivity2
import org.pulp.main.MainActivity
import org.pulp.main.R

class HomeActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


    }

    fun goFastApiPage(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun goViewDslPage(view: View) {
        startActivity(Intent(this, KtActivity2::class.java))
    }

}