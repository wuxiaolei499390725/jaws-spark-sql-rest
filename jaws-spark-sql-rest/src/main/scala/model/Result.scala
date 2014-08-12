package model
import spray.json.DefaultJsonProtocol._
import scala.collection.JavaConverters._
import com.xpatterns.jaws.data.DTO.ResultDTO
import scala.Array.canBuildFrom
import shark.api.ResultSet
import shapeless.ToList
import actors.Configuration

/**
 * Created by emaorhian
 */
case class Result(schema: List[String], results: Array[Array[String]]) {
  def getSchema(): List[String] = {
    schema
  }

  def getResults(): Array[Array[String]] = {
    results
  }

  def toDTO(): ResultDTO = {
    val js = schema.asJava
    val result = results.map(_.toList.asJava).toList.asJava
    
    return new ResultDTO(js, result)
  }
}

object Result {
  implicit val logsJson = jsonFormat2(apply)

  def fromTuples(schema: List[String], filteredResults: Array[Tuple2[Object, Array[Object]]]): Result = {

    var results = Array[Array[String]]()
    filteredResults.foreach(tuple => {
      var row = Array[String]()
      tuple._2.foreach(field => row = row ++ Array(Option(field).getOrElse("Null").toString))
      results = results ++ Array(row)
    })
    Result(schema, results)
  }

  def fromResultDTO(result: ResultDTO): Result = {
    var schema = List[String]()
    var results = Array[Array[String]]()
    for (i <- 0 to result.schema.size() - 1) {
      schema = schema ++ List(result.schema.get(i))
    }

    for (rowIndex <- 0 to result.results.size() - 1) {
      var row = Array[String]()
      var rrow = result.results.get(rowIndex)
      for (fieldIndex <- 0 to rrow.size() - 1) {
        row = row ++ Array(rrow.get(fieldIndex))
      }
      results = results ++ Array(row)
    }

    Result(schema, results)
  }

  def fromResultSet(resultSet: ResultSet): Result = {
    var schema = List[String]()
    var results = Array[Array[String]]()

    Option(resultSet) match {
      case None => Configuration.log4j.info("Result set is null")
      case _ => {
        // add schema
        resultSet.schema.foreach(desc => schema = schema ++ List(desc.name + ":" + desc.dataType))
        // add results
        resultSet.results.foreach(res => results = results ++ Array(res.map(value => {
          Option(value) match {
            case None => "Null"
            case _ => value.toString()
          }
        })))
       
      }
    }
    Result(schema, results)
  }

  def trimResults(result: Result): Result = {
    Result(result.schema, result.results.map(row => row.map(field => field.trim())))
  }
}  
  