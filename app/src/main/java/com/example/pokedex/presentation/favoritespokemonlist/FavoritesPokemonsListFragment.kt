package com.example.pokedex.presentation.favoritespokemonlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.common.appComponent
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.databinding.FragmentFavoritesPokemonsListBinding
import com.example.pokedex.common.observeOnce
import com.example.pokedex.presentation.base.BaseBindingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

class FavoritesPokemonsListFragment :
    BaseBindingFragment<FragmentFavoritesPokemonsListBinding>(FragmentFavoritesPokemonsListBinding::inflate) {

    private val viewModel: FavoritesPokemonListViewModel by viewModels {
        appComponent.viewModelsFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        val adapter = FavoritesPokemonListAdapter(requireContext())

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.visibility = View.INVISIBLE

        viewModel.getAllFavoritePokemons().observe(viewLifecycleOwner, { list ->
            adapter.submitList(list)
            binding.pikachuImage.visibility = if (list.isEmpty()) View.VISIBLE else View.INVISIBLE
        })

        binding.favoritesPokemonsRecyclerView.adapter = adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val pokemon = adapter.currentList[position]
                deletePokemon(pokemon)
            }
        }).attachToRecyclerView(binding.favoritesPokemonsRecyclerView)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.favorites_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteAllPokemons -> deleteAllPokemons()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deletePokemon(pokemon: Pokemon) {
        viewModel.delete(pokemon)
        Snackbar.make(requireView(), "Pokemon deleted", Snackbar.LENGTH_LONG).apply {
            setAction("Undo") {
                viewModel.addToFavorites(pokemon)
            }
            show()
        }
    }

    private fun deleteAllPokemons() {
        viewModel.getAllFavoritePokemons().observeOnce { list ->
            if (list.isEmpty()) {
                Toast.makeText(
                    requireContext(), R.string.you_dont_have_pokemons,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm_action)
                    .setMessage("Delete all pokemons?")
                    .setPositiveButton("Confirm") { dialog, _ ->
                        viewModel.deleteAllFavoritePokemons()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.create().show()
            }
        }
    }
}