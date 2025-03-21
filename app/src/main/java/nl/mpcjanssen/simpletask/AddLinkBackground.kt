/**
 * This file is part of Simpletask.

 * Copyright (c) 2009-2012 Todo.txt contributors (http://todotxt.com)
 * Copyright (c) 2013- Mark Janssen

 * LICENSE:

 * Todo.txt Touch is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.

 * Todo.txt Touch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.

 * You should have received a copy of the GNU General Public License along with Todo.txt Touch.  If not, see
 * //www.gnu.org/licenses/>.

 * @author Mark Janssen
 * *
 * @license http://www.gnu.org/licenses/gpl.html
 * *
 * @copyright 2009-2012 Todo.txt contributors (http://todotxt.com)
 * *
 * @copyright 2013- Mark Janssen
 */
package nl.mpcjanssen.simpletask

import android.R.style.ThemeOverlay_Material
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.view.ContextThemeWrapper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ShareCompat
import com.google.android.material.snackbar.Snackbar
import nl.mpcjanssen.simpletask.task.Task
//import nl.mpcjanssen.simpletask.util.showToastLong
import nl.mpcjanssen.simpletask.util.showToastShort
import nl.mpcjanssen.simpletask.util.todayAsString

class AddLinkBackground : Activity() {
    val tag = "AddLinkBackground"

    public override fun onCreate(instance: Bundle?) {
        Log.d(tag, "onCreate()")
        super.onCreate(instance)
        // For the snackbar
        setContentView(R.layout.add_link)

        val append_text = TodoApplication.config.shareAppendText
        val intentReader = ShareCompat.IntentReader.from(this)
        val uri = intentReader.stream
        val subject = intentReader.subject ?: ""
        val mimeType = intentReader.type ?: ""
        Log.i(tag, "Added link to content ($mimeType)")
        if (uri == null) {
            // showToastLong(TodoApplication.app, R.string.share_link_failed)

            val context: Context = ContextThemeWrapper(this, ThemeOverlay_Material)
            val layout: CoordinatorLayout = findViewById(R.id.rootLayout)
            val snackBar = Snackbar.make(context, layout, getString(R.string.share_link_failed), Snackbar.LENGTH_INDEFINITE)
            snackBar.setAction(getString(R.string.close)) { finish() }
            snackBar.show()
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, 5000)

            return
        }

        addBackgroundTask("$subject $uri", append_text)
    }

    private fun addBackgroundTask(sharedText: String, appendText: String) {
        val todoList = TodoApplication.todoList
        Log.d(tag, "Adding background tasks to todolist $todoList")

        val rawLines = sharedText.split("\r\n|\r|\n".toRegex()).filterNot(String::isBlank)
        val lines = if (appendText.isBlank()) {
            rawLines
        } else {
            rawLines.map { "$it $appendText" }
        }
        val tasks = lines.map { text ->
            if (TodoApplication.config.hasPrependDate) {
                Task(text, todayAsString)
            } else {
                Task(text)
            }
        }

        todoList.add(tasks, TodoApplication.config.hasAppendAtEnd)
        todoList.notifyTasklistChanged(
            TodoApplication.config.todoFile,
            save = true,
            refreshMainUI = true
        )
        showToastShort(TodoApplication.app, R.string.link_added)
        if (TodoApplication.config.hasShareTaskShowsEdit) {
            todoList.editTasks(this, tasks, "")
        }
        finish()
    }
}
