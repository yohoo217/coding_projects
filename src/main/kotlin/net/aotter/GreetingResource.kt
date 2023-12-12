package net.aotter

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import net.aotter.repository.RoomRepository
import org.jboss.logging.Logger
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
class GreetingResource {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var roomRepository: RoomRepository

    @CheckedTemplate(requireTypeSafeExpressions = false)  //  設定模板，server 在呼叫時會把全部的 template 都掃過，把符合的呼叫符合的網址
    object Templates {
        @JvmStatic
        external fun hello(): TemplateInstance  //  建立  hello  的模板
        @JvmStatic
        external fun post(): TemplateInstance  //  建立  post  的模板 像是做了一個
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_HTML)
    fun hello(): TemplateInstance {
        val rooms = roomRepository.getRooms()
        val titles: List<String> = roomRepository.getRoomTitles()
        val photos = roomRepository.getRoomPhotos()
        logger.info("$photos")
        return Templates.hello()
            .data("title", titles.joinToString(", "))
            .data("description", "花蓮的一個好居處")
            .data("member", "")
            .data("photos", photos)
    }

    @GET
    @Path("/room/{id}")
    fun showRoomById(@PathParam("id") id: String): TemplateInstance? {
        val titles: List<String> = roomRepository.getRoomTitles()
        val photos = roomRepository.getRoomPhotos()
        val photo = photos?.find { it["id"] == id }
        logger.info("$photos")

        roomRepository.findRoomById(id)

        return if (photo != null) {
            Templates.post()
                .data("title", titles.joinToString(", "))
                .data("id", id)
        } else {
            throw NotFoundException()
        }
    }
}