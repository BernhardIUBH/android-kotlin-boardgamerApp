package com.example.boardgamerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerViewAdapter(
    private val proposalList:List<RecyclerViewEntry>,
    private val clickListener:(RecyclerViewEntry)->Unit
    ) : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.list_item, parent, false)
        return MyViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val entry = proposalList[position]
        holder.bind(entry, clickListener)
    }

    override fun getItemCount(): Int {
        return proposalList.size
    }

}

class MyViewHolder(val view: View): RecyclerView.ViewHolder(view) {
    fun bind(RecyclerViewEntry: RecyclerViewEntry, clickListener:(RecyclerViewEntry)->Unit) {
        val gameName = view.findViewById<TextView>(R.id.tvProposalGameName)
        val gameRating = view.findViewById<TextView>(R.id.tvProposalGameRating)
        gameName.text = RecyclerViewEntry.game_name
        gameRating.text = RecyclerViewEntry.rating.toString()

        view.setOnClickListener {
            clickListener(RecyclerViewEntry)
        }
    }
}