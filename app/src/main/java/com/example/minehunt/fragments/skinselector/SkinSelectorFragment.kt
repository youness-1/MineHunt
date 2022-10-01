package com.example.minehunt.fragments.skinselector

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.minehunt.databinding.FragmentSkinselectorBinding
import com.example.minehunt.Skin
import com.example.minehunt.ItemViewModel

class SkinSelectorFragment : Fragment() {

    private var _binding: FragmentSkinselectorBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkinselectorBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val skin = binding.skin
        skin.setOnCheckedChangeListener { _, i ->
            viewModel.player.sprite = when(skin.indexOfChild(skin.findViewById<RadioButton>(i))){
                1 -> Skin.values()[0]
                2 -> Skin.values()[1]
                4 -> Skin.values()[2]
                5 -> Skin.values()[3]
                6 -> Skin.values()[4]
                8 -> Skin.values()[5]
                else -> {Skin.PLAYER1}
            }
        }
        val button = skin.getChildAt(when(Skin.values().indexOf(viewModel.player.sprite)){
            0 -> 1
            1 -> 2
            2 -> 4
            3 -> 5
            4 -> 6
            5 -> 8
            else -> {1}
        })
        skin.check(button.id)
        lock(binding)
        unlock(binding)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun lock(binding:FragmentSkinselectorBinding){
        binding.player3.isEnabled = false
        binding.player3.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding.player4.isEnabled = false
        binding.player4.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding.player5.isEnabled = false
        binding.player5.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding.player6.isEnabled = false
        binding.player6.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
    }

    private fun unlock(binding:FragmentSkinselectorBinding){
        val score = viewModel.player.score
        if(score>=50){
            binding.player3.isEnabled = true
            binding.player3.backgroundTintList = null
            binding.player4.isEnabled = true
            binding.player4.backgroundTintList = null
            binding.player5.isEnabled = true
            binding.player5.backgroundTintList = null
        }
        if(score>=150){
            binding.player6.isEnabled = true
            binding.player6.backgroundTintList = null
        }
    }
}

