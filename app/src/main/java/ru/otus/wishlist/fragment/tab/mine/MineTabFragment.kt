package ru.otus.wishlist.fragment.tab.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.wishlist.R
import ru.otus.wishlist.databinding.FragmentMineTabBinding

@AndroidEntryPoint
class MineTabFragment : Fragment(R.layout.fragment_mine_tab) {

    private lateinit var binding: FragmentMineTabBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMineTabBinding.inflate(inflater)
        return binding.root
    }
}