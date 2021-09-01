package com.example.pokedex.presentation.pokemonsearch

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.pokedex.R
import com.example.pokedex.domain.model.Pokemon
import com.example.pokedex.databinding.FragmentPokemonSearchBinding
import com.example.pokedex.utils.capitalized
import com.example.pokedex.presentation.viewmodel.PokemonViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.pokedex.utils.thereIsInternetConnection

@AndroidEntryPoint
class PokemonSearchFragment : Fragment() {

    private val viewModel: PokemonViewModel by viewModels()
    private lateinit var pokemonImageUrl: String

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPokemonSearchBinding.inflate(inflater)

        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.visibility = View.VISIBLE

        binding.apply {
            searchPokemonButton.setOnClickListener {
                if (thereIsInternetConnection(requireContext())) {
                    if (searchPokemonEdt.text.isNotEmpty()) {
                        viewModel.viewModelScope.launch {
                            val pokemonName = searchPokemonEdt.text.trim().toString().lowercase()
                            val pokemonResponse = viewModel.getPokemonByName(pokemonName).body()
                            if (pokemonResponse != null) {
                                pokemonIdText.text = pokemonResponse.id.toString()
                                pokemonNameText.text = pokemonResponse.name.capitalized()
                                pokemonHeightText.text = "${((pokemonResponse.height * 100) / 1000)}M"
                                pokemonWeightText.text = "${((pokemonResponse.weight * 100) / 1000)}KG"
                                Glide.with(requireContext()).load(pokemonResponse.sprites.frontDefault)
                                    .into(pokemonImage)
                                pokemonImageUrl = pokemonResponse.sprites.frontDefault
                            } else {
                                pokemonImage.setImageResource(R.drawable.ic_not_found)
                                val textViewList = listOf(
                                    pokemonIdText,
                                    pokemonNameText,
                                    pokemonWeightText,
                                    pokemonHeightText
                                )
                                for (textView in textViewList)
                                    textView.text = String()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.the_field_must_not_be_empty,
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.no_internet_connection,
                        Toast.LENGTH_LONG).show()
                }
            }

            addToFavoriteButton.setOnClickListener {
                if (pokemonIdText.text.isNotEmpty()) {
                    val pokemon = Pokemon(
                        pokemonIdText.text.toString().toInt(),
                        pokemonNameText.text.toString(),
                        pokemonHeightText.text.toString(),
                        pokemonWeightText.text.toString(),
                        pokemonImageUrl
                    )
                    viewModel.viewModelScope.launch {
                        if (viewModel.isAddedPokemonWithThisId(pokemon.id) == null) {
                            viewModel.insert(pokemon)
                            Toast.makeText(requireContext(), R.string.pokemon_has_been_added,
                                Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), R.string.this_pokemon_added_to_favorites,
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.failed_to_add_pokemon,
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }
}