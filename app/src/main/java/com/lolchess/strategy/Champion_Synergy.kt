package com.lolchess.strategy


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ChampionSynergy:Fragment(){

    companion object {
        fun newInstance() = ChampionSynergy()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_second_image, container, false)
    }

}
