package com.neobis.israil.kotlinnotes.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.Toast
import com.neobis.israil.kotlinnotes.R
import com.neobis.israil.kotlinnotes.data.DbManager
import com.neobis.israil.kotlinnotes.model.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.row.view.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    var listNotes = ArrayList<Note>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LoadQuery("%")
    }

    override fun onResume() {
        super.onResume()
        LoadQuery("%")
    }

    fun LoadQuery(title: String) {
        var dbManager = DbManager(this)
        val projections = arrayOf("ID","Title","Description")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager.Query(projections,"Title like ?",selectionArgs,"Title")
        listNotes.clear()
        if(cursor.moveToFirst()){
            do {
                val ID = cursor.getInt(cursor.getColumnIndex("ID"))
                val Title = cursor.getString(cursor.getColumnIndex("Title"))
                val Description = cursor.getString(cursor.getColumnIndex("Description"))

                listNotes.add(Note(ID, Title, Description))
            }while (cursor.moveToNext())

        }

        var myNotesAdapter = MyNotesAdapter(this, listNotes)
        notesLv.adapter = myNotesAdapter

        //get total number of tasks from listView
        val total = notesLv.count

        //actionBar
        val mActionBar  = supportActionBar
        if (mActionBar !=null){
            mActionBar.subtitle = "You have $total note(s) in list ..."

        }

    }

    //adapter


   override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item!=null){
            when(item.itemId){
                R.id.addNote -> {
                    startActivity(Intent(this, AddNoteActivity::class.java))
                }
                R.id.action_settings ->{
                    Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class MyNotesAdapter: BaseAdapter {
        var listNotesAdapter = ArrayList<Note>()
        var context: Context?=null

        constructor(context: Context?, listNotesAdapter: ArrayList<Note>) : super() {
            this.listNotesAdapter = listNotesAdapter
            this.context = context
        }



        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var myView = layoutInflater.inflate(R.layout.row,null)
            var myNote = listNotesAdapter[position]
            myView.titleTv.text = myNote.noteName
            myView.descTv.text = myNote.noteDes

            //delete button click
            myView.deleteBtn.setOnClickListener{
                var dbManager = DbManager(this.context!!)
                val selectionArgs = arrayOf(myNote.noteId.toString())
                dbManager.delete("ID=?",selectionArgs)
                LoadQuery("%")
            }
            //edit,update buttons click
            myView.editBtn.setOnClickListener {
                GoToUpdateFun(myNote)
            }

            //copy button click
            myView.copyBtn.setOnClickListener {
                val title = myView.titleTv.text.toString()
                val desc = myView.descTv.text.toString()
                val common = title +"\n"+desc
                val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE)as ClipboardManager
                clipBoard.text = common
                Toast.makeText(this@MainActivity,"Copied...",Toast.LENGTH_SHORT).show()
            }

            //share button click
            myView.shareBtn.setOnClickListener {
                val title = myView.titleTv.text.toString()
                val desc = myView.descTv.text.toString()
                val common = title +"\n"+desc

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plane"
                shareIntent.putExtra(Intent.EXTRA_TEXT,common)
                startActivity(Intent.createChooser(shareIntent,common))

            }
            return myView
        }


        override fun getItem(position: Int): Any {
            return listNotesAdapter[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listNotesAdapter.size
        }

        private fun GoToUpdateFun(myNote: Note) {
            var intent  = Intent(this@MainActivity, AddNoteActivity::class.java)
            intent.putExtra("ID",myNote.noteId)  //put id
            intent.putExtra("name",myNote.noteName) //put name
            intent.putExtra("des",myNote.noteDes)  // put description
            startActivity(intent)
        }
    }

}

