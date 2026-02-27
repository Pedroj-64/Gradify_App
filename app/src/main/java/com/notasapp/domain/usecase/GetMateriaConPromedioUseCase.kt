package com.notasapp.domain.usecase

import com.notasapp.domain.model.Materia
import com.notasapp.domain.repository.MateriaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso: obtiene una materia completa (con componentes y sub-notas) como Flow.
 *
 * La UI se suscribe a este Flow y se actualiza automáticamente cuando
 * el usuario ingresa o modifica una nota en Room.
 */
class GetMateriaConPromedioUseCase @Inject constructor(
    private val repository: MateriaRepository
) {
    operator fun invoke(materiaId: Long): Flow<Materia?> =
        repository.getMateriaConComponentes(materiaId)
}
