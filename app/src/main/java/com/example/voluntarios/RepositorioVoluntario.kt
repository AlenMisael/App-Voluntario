package com.example.voluntarios

class RepositorioVoluntario(private val voluntarioDao: VoluntarioDao) {

    suspend fun insertar(voluntario: Voluntario): Long {
        return voluntarioDao.insert(voluntario)
    }

    suspend fun getByUid(uid: String): Voluntario? {
        return voluntarioDao.getByUid(uid)
    }

}