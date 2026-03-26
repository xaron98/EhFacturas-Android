package es.ehfacturas.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import es.ehfacturas.data.db.entity.Categoria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Insertar categorías por defecto
        Categoria.categoriasDefecto.forEachIndexed { index, (nombre, icono) ->
            db.execSQL(
                "INSERT INTO categorias (nombre, icono, orden) VALUES (?, ?, ?)",
                arrayOf(nombre, icono, index)
            )
        }
    }
}
