package com.gradify.backend

import com.gradify.backend.controller.RecomendacionesController
import com.gradify.backend.service.GeminiProxyService
import com.gradify.backend.service.PromptBuilder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecomendacionesController::class)
@TestPropertySource(properties = ["app.token=secreto", "rate-limit.burst-capacity=2", "rate-limit.requests-per-minute=1"])
class RecomendacionesControllerTest {

    @Autowired lateinit var mvc: MockMvc
    @MockBean lateinit var gemini: GeminiProxyService
    @MockBean lateinit var prompts: PromptBuilder

    private fun call(token: String?): ResultActions = mvc.perform(
        post("/api/v1/recomendaciones")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"nombreMateria":"Cálculo"}""")
            .apply { token?.let { header("X-Gradify-Token", it) } }
    )

    @Test
    fun `rechaza sin token y limita por IP`() {
        call(null).andExpect(status().isUnauthorized)
        call("malo").andExpect(status().isUnauthorized)
        // el mock de Gemini devuelve null -> 503, pero cuenta contra el bucket (burst = 2)
        call("secreto").andExpect(status().isServiceUnavailable)
        call("secreto").andExpect(status().isServiceUnavailable)
        call("secreto").andExpect(status().isTooManyRequests)
    }
}
