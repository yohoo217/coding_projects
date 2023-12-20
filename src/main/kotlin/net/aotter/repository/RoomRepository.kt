package net.aotter.repository

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import net.aotter.model.Room
import org.jboss.logging.Logger
import java.net.URL
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class RoomRepository {

    @Inject
    lateinit var logger: Logger

    private val SHEET_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vTFSxyakG_CmwMV-mxmVuBft46P6ut897mIvRPn37HPZbunSe9wIg-pcAA5rwIUSuCjqSQoJEle-PL0/pub?output=csv"

    fun getRooms(): List<Room>? = readRemoteCSVMKII(SHEET_URL)
            ?.map {
                Room(
                        it["id"],
                        it["title"],
                        it["mem"],
                        it["desc"],
                        it["imgSrc"],
                        it["url"],
                        it["price"]
                )
            }

    fun getRoomTitles(): List<String> {
        val rooms = getRooms()
        return rooms?.mapNotNull { it.title } ?: emptyList()
    }

    fun getRoomPhotos(): MutableList<Map<String, String?>>? = readRemoteCSVMKII(SHEET_URL)

    fun findRoomById(id: String) = getRooms()?.find { it.id == id }

    private fun readRemoteCSVMKII(url: String): MutableList<Map<String, String?>>? {
        return try {
            val objectMapper: ObjectMapper = CsvMapper()
            val schema: CsvSchema = CsvSchema.emptySchema().withHeader()
            val iterator: MappingIterator<Map<String, String?>> =
                    objectMapper.readerFor(Map::class.java).with(schema).readValues(URL(url))
            iterator.readAll()
        } catch (e: Exception) {
            logger.warn("readRemoteCSVMKII  failed", e)
            null
        }
    }
}