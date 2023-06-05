package  net.aotter  //  套件名稱

import  com.fasterxml.jackson.databind.MappingIterator  //  引入套件
import  com.fasterxml.jackson.databind.ObjectMapper
import  com.fasterxml.jackson.dataformat.csv.CsvMapper
import  com.fasterxml.jackson.dataformat.csv.CsvSchema
import  io.quarkus.qute.CheckedTemplate
import  io.quarkus.qute.TemplateInstance
import  org.jboss.logging.Logger
import  java.io.BufferedReader
import  java.io.InputStreamReader
import  java.net.URL
import  java.nio.charset.StandardCharsets

import  javax.inject.Inject
import  javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import  javax.ws.rs.Path
import javax.ws.rs.PathParam
import  javax.ws.rs.Produces
import  javax.ws.rs.core.MediaType

//  讀取遠端CSV檔案的函式
private  fun  readRemoteCSV(url:  String):  List<String>  {
    val  csvUrl  =  URL(url)
    val  connection  =  csvUrl.openConnection()  //  開啟連線
    val  inputStream  =  connection.getInputStream()  //  取得資料流

    val  titles  =  mutableListOf<String>()  //  儲存  CSV  的  title

    BufferedReader(InputStreamReader(inputStream,  StandardCharsets.UTF_8)).use  {  reader  ->
        var  line:  String?
        while  (reader.readLine().also  {  line  =  it  }  !=  null)  {  //  讀取每一行
            val  columns  =  line?.split(",")  //  分割每一行的欄位
            val  title  =  columns?.get(1)  //  取得  title
            title?.let  {  titles.add(it)  }  //  如果  title  不為  null，就加入到  titles  裡面
            //  在此處理  CSV  檔案的資料
        }
    }

    return  titles  //  回傳檔案的  title
}

@Path("/")
class  GreetingResource  {
    @Inject
    lateinit  var  logger:  Logger  //  建立一個  logger

    @CheckedTemplate(requireTypeSafeExpressions  =  false)  //  設定模板
    object  Templates  {
        @JvmStatic
        external  fun  hello():  TemplateInstance  //  建立  hello  的模板
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_HTML)  //  設定  response  的樣式
    fun  hello():  TemplateInstance  {  //  宣告  hello  的函式
        val  url  =  "https://docs.google.com/spreadsheets/d/e/2PACX-1vTFSxyakG_CmwMV-mxmVuBft46P6ut897mIvRPn37HPZbunSe9wIg-pcAA5rwIUSuCjqSQoJEle-PL0/pub?output=csv"  //  宣告檔案的  URL
        val  titles:  List<String>  =  readRemoteCSV(url)  //  讀取檔案標題，儲存在  titles  變數中
        val  photos  =  readRemoteCSVMKII(url)  //  讀取檔案資料，儲存在  photos  變數中
        logger.info("$photos")  //  將  photos  變數傳給  logger

        return  Templates.hello()
                .data("title",  titles.joinToString(",  "))  //  將  titles  變數傳到模板中的  title  變數
                .data("description",  "花蓮的一個好居處")  //  將一段敘述傳到模板中的  description  變數
                .data("member",  "")  //  建立一個  member  變數，目前是空字串
                .data("photos",  photos)  //  將讀取到的檔案資料傳到模板中的  photos  變數
    }

    private  fun  readRemoteCSVMKII(url:  String):  MutableList<Map<String,  String?>>?  {  //  讀取  CSV  檔案的函式
        return  try  {
            val  objectMapper:  ObjectMapper  =  CsvMapper()  //  宣告一個  CsvMapper  物件
            val  schema:  CsvSchema  =  CsvSchema.emptySchema().withHeader()  //  宣告一個  CsvSchema  物件，並將第一列當成  header
            val  iterator:  MappingIterator<Map<String,  String?>>  =  //  宣告一個  MappingIterator  物件
                    objectMapper.readerFor(Map::class.java).with(schema).readValues(URL(url))  //  讀取遠端檔案
            iterator.readAll()  //  回傳讀取到的檔案資料
        }  catch  (e:  Exception)  {
            logger.warn("readRemoteCSVMKII  failed",  e)  //  讀取失敗，顯示警告訊息
            null  //  回傳  null
        }
    }
}