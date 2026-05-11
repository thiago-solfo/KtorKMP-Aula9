package com.example.ktorkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PostListScreen()
        }
    }
}

@Composable
fun PostListScreen(viewModel: PostViewModel = viewModel { PostViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var textoFiltroUsuarioId by remember { mutableStateOf("") }
    
    val estadoDaLista = rememberLazyListState()

    val deveCarregarMais = remember {
        derivedStateOf {
            val layoutInfo = estadoDaLista.layoutInfo
            val ultimoItemVisivel = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalDeItens = layoutInfo.totalItemsCount
            ultimoItemVisivel >= totalDeItens - 2 && totalDeItens > 0
        }
    }

    LaunchedEffect(deveCarregarMais.value) {
        if (deveCarregarMais.value) {
            viewModel.loadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = textoFiltroUsuarioId,
            onValueChange = { novoTexto ->
                textoFiltroUsuarioId = novoTexto
                viewModel.onUserIdFilterChanged(novoTexto)
            },
            label = { Text("Filtrar por User ID") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        when (val state = uiState) {
            is PostsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PostsUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.carregarPosts(reset = true) }) {
                        Text("Tentar Novamente")
                    }
                }
            }
            is PostsUiState.Success -> {
                LazyColumn(
                    state = estadoDaLista,
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(state.posts) { postagem ->
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "User ID: ${postagem.userId}", 
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = postagem.title, 
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = postagem.body, 
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    if (state.isPaginating) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp), 
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    if (state.endOfPaginationReached && state.posts.isNotEmpty()) {
                        item {
                            Text(
                                "Fim das postagens", 
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
