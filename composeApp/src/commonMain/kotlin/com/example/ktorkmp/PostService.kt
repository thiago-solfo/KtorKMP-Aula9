package com.example.ktorkmp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class PostService {
    val cliente = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun pegarPosts(pagina: Int, limite: Int, idUsuario: Int? = null): List<Post> {
        val response = cliente.get("https://jsonplaceholder.typicode.com/posts") {
            parameter("_page", pagina)
            parameter("_limit", limite)
            if (idUsuario != null) {
                parameter("userId", idUsuario)
            }
        }
        
        return when (response.status.value) {
            in 200..299 -> response.body()
            404 -> throw Exception("Posts não encontrados (404)")
            500 -> throw Exception("Erro interno no servidor (500)")
            else -> throw Exception("Erro inesperado: ${response.status.value}")
        }
    }
}
