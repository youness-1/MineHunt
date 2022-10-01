package com.example.minehunt.fragments.minehunt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minehunt.databinding.FragmentMinehuntBinding
import com.example.minehunt.ItemViewModel

class MineHuntFragment : Fragment(), AdapterView.OnItemSelectedListener{

    private var _binding: FragmentMinehuntBinding? = null

    private val binding get() = _binding!!
    private lateinit var grid: GridView
    private val viewModel: ItemViewModel by activityViewModels()
    private lateinit var game: Game

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMinehuntBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val size = binding.size
        val start = binding.start
        val difficulty = binding.difficulty
        val player = viewModel.player
        val name = binding.name
        name.setText(viewModel.player.name)
        grid = binding.gridv
        binding.state.text=player.toString()
        game=Game(grid, player, context!!, binding.state, start, difficulty, size, name)
        grid.game=game
        if (viewModel.game == null) {
            viewModel.game=game
        }
        else{
            game.resume(viewModel.game!!)
            start.text="Surrender"
            difficulty.isEnabled=false
            size.isEnabled=false
            name.isEnabled=false
            grid.postInvalidate()
        }
        start.setOnClickListener {
            val value = grid.game.running.get()
            if(!value){
                grid.game.running.set(!value)
                println("Game Started!")
                player.name=name.text.toString()
                game.start(difficulty.selectedItemPosition)
                start.text="Surrender"
                difficulty.isEnabled=false
                size.isEnabled=false
                name.isEnabled=false
            }
            else{
                grid.game.running.set(!value)
                game.loose()
                start.text="Play"
                difficulty.isEnabled=true
                size.isEnabled=true
                name.isEnabled=true
            }
            grid.postInvalidate()
        }
        size.onItemSelectedListener = this
        return root
    }

    override fun onDestroyView() {
        try{
            game.mediaPlayer.stop()
        }
        catch(e: UninitializedPropertyAccessException){
        }
        if (game.running.get())
            viewModel.game=game
        else
            viewModel.game=null
        super.onDestroyView()
        _binding = null
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        try{
            when(p2){
                0 -> grid.tileSize = 120f
                1 -> grid.tileSize = 100f
                2 -> grid.tileSize = 80f
            }
        }catch(e: Exception){}
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onStop() {
        try{
            game.mediaPlayer.pause()
        }catch(e: Exception){}
        super.onStop()
    }

    override fun onStart() {
        try{
            if (game.running.get())
                game.mediaPlayer.start()
        }catch(e: Exception){}
        super.onStart()
    }
}