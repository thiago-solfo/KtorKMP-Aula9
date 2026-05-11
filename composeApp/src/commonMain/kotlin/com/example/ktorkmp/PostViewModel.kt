package com.example.ktorkmp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.ktor.utils.io.errors.IOException

sealed class PostsUiState {
    object Loading : PostsUiState()
    data class Success(
        val posts: List<Post>, 
        val isPaginating: Boolean = false,
        val endOfPaginationReached: Boolean = false
    ) : PostsUiState()
    data class Error(val message: String) : PostsUiState()
}

class PostViewModel : ViewModel() {
    private val servico = PostService()
    
    private val _uiState = MutableStateFlow<PostsUiState>(PostsUiState.Loading)
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    private var paginaCorrente = 1
    private val limite = 10
    private var idFiltro: Int? = null
    private var currentPosts = mutableListOf<Post>()

    init {
        carregarPosts(reset = true)
    }

    fun onUserIdFilterChanged(texto: String) {
        val idDigitado = texto.toIntOrNull()
        if (idDigitado != idFiltro) {
            idFiltro = idDigitado
            carregarPosts(reset = true)
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState is PostsUiState.Success && !currentState.isPaginating && !currentState.endOfPaginationReached) {
            paginaCorrente++
            carregarPosts(reset = false)
        }
    }

    fun carregarPosts(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                paginaCorrente = 1
                currentPosts.clear()
                _uiState.value = PostsUiState.Loading
            } else {
                val currentState = _uiState.value
                if (currentState is PostsUiState.Success) {
                    _uiState.value = currentState.copy(isPaginating = true)
                }
            }

            try {
                val novos = servico.pegarPosts(paginaCorrente, limite, idFiltro)
                
                if (novos.isEmpty()) {
                    val currentState = _uiState.value
                    if (currentState is PostsUiState.Success) {
                        _uiState.value = currentState.copy(isPaginating = false, endOfPaginationReached = true)
                    } else {
                        _uiState.value = PostsUiState.Success(emptyList(), endOfPaginationReached = true)
                    }
                } else {
                    currentPosts.addAll(novos)
                    _uiState.value = PostsUiState.Success(
                        posts = currentPosts.toList(),
                        isPaginating = false,
                        endOfPaginationReached = novos.size < limite
                    )
                }
            } catch (e: IOException) {
                _uiState.value = PostsUiState.Error("Falha de conexão. Verifique sua internet.")
            } catch (e: Exception) {
                _uiState.value = PostsUiState.Error(e.message ?: "Ocorreu um erro inesperado")
            }
        }
    }
}
