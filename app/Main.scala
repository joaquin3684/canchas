

import java.sql.Timestamp

import JsonFormats.{DateTimeDeserializer, DateTimeSerializer}
import akka.http.scaladsl.model.DateTime
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.module.SimpleModule
import models._
import slick.jdbc.MySQLProfile.api._
import schemas.Schemas

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Success
import com.github.t3hnar.bcrypt._
import repositories.Db
import schemas.Schemas.{estados, usuariosObrasSociales, ventas, visitas}
import services.JsonMapper
import slick.jdbc.GetResult

object Main extends App {

  //val db = Database.forConfig("db.default")


 /* val a = db.run(Schemas.allSchemas.drop)
  Await.result(a, Duration.Inf)
*/

 /* val initUser = Schemas.usuarios ++= Seq(
                                        Usuario("200", "200", "200".bcrypt, "200", None)
                                        )

  val initPerfiles = Schemas.perfiles ++= Seq(
                                            Perfil("admin"),
                                            Perfil("operador"),
                                            Perfil("supervisor")
                                            )

  val initUserPerfil = Schemas.usuariosPerfiles ++= Seq(
                                                      UsuarioPerfil("200", "admin")
                                                      )


  val initObrasSociales = Schemas.obrasSociales ++= Seq(
                                                  ObraSocial("cobertec"),
                                                  ObraSocial("osde"),
                                                  ObraSocial("medicus"),
  )

  val initUserObraSocial = Schemas.usuariosObrasSociales ++= Seq(
                                                              UsuarioObraSocial("200", "cobertec"),
                                                              UsuarioObraSocial("200", "medicus"),
                                                              UsuarioObraSocial("200", "osde"),
                                                              )

  val initPantallas = Schemas.pantallas ++= Seq(
                                      Pantalla("usuario"),
                                      )

  val initRutas = Schemas.rutas ++= Seq(
                                Ruta("/obraSocial/all"),
                                Ruta("/perfil/all"),
                                )

  val initPerfilPantalla = Schemas.perfilesPantallas ++= Seq(
                                                            PerfilPantalla("admin", "usuario"),

                                                            )

  val initPantallaRuta = Schemas.pantallasRutas ++= Seq(
                                                    PantallaRuta("usuario", "/obraSocial/all"),
                                                    PantallaRuta("usuario", "/perfil/all"),
                                                  )

  val seq = DBIO.seq(
                    Schemas.allSchemas.create,
                    initUser,
                    initObrasSociales,
                    initPerfiles,
                    initPantallas,
                    initRutas,
                    initUserObraSocial,
                    initUserPerfil,
                    initPerfilPantalla,
                    initPantallaRuta
  )
  val e = db.run(seq.transactionally)*/
 // Await.result(e, Duration.Inf)


  /*val query = {
    for {
      e <- estados.filter(x => x.estado === "Visita creada"  && !(x.idVenta in estados.filter(x => x.estado === "Visita confirmada").map(_.idVenta)))
      v <- ventas.filter(x => x.dni === e.idVenta && x.idObraSocial.inSetBind(obs))
      vis <- visitas.filter(x => x.idVenta === v.dni).sortBy(_.idVenta.desc)
    } yield (v, vis)
  }.result.statements.foreach(println)
*/
 import java.io._

  import org.apache.pdfbox.pdfparser.PDFParser
  import org.apache.pdfbox.pdmodel.PDDocument
  import org.apache.pdfbox.util.PDFTextStripper

  def prueba = {
    try {

      val parser = new PDFParser(new FileInputStream("/home/joaquin/Documentos/Factura Tecno.pdf"));
      parser.parse()
      val cosDoc = parser.getDocument();
      val pdfStripper = new PDFTextStripper();
      val pdDoc = new PDDocument(cosDoc);
      pdfStripper.setStartPage(1)
      pdfStripper.setEndPage(1)
      val parsedText = pdfStripper.getText(pdDoc);

      Some(parsedText)

      /*val pdf = PDDocument.load(new File("/home/joaquin/Descargas/prueba.PDF"))
      val stripper = new PDFTextStripper
      stripper.setStartPage(1)
      stripper.setEndPage(2)
      Some(stripper.getText(pdf))*/
    } catch {
      case t: Throwable =>
        t.printStackTrace
        None
    }
  }

  prueba

  val str = "0.00 20.20.30 A20.200,00A a20.400.400,00A 20.220,00"
  val cuit = "((\\d{2}-\\d{8}-\\d)|((?<!.)(20|30|24|25|26|27)\\d{9}))".r
  val tipoFactura = "(^|\n)[ABCM][\n\\s]".r
  val puntoVenta = "\\d{4}\\s?-\\s?\\d{8}".r
  val fechaEmision = "\\d{2}\\s?[-/]\\s?\\d{2}\\s?[-/]\\s?\\d{2,4}".r
  val respoInsc = ("(?i)" + "iva responsable inscripto".mkString("\\s?")).r

  val pruebaPunto = "[\\.,]".r
  val importes =  "(?<![\\.,0-9])(((\\d{1,3}([.,]\\d{3})*)[.,]\\d{2})|\\d+[.,]\\d{2})(?![\\.,])".r

  val codigoBarra = "\\d{40}".r

  val c = (cuit findAllIn prueba.get).map(_.replaceAll("\\s", "")).mkString(", ")
  val fac = (tipoFactura findAllIn prueba.get).map(_.replaceAll("\\s", "")).mkString(", ")
  val a = (puntoVenta findAllIn prueba.get).map(_.replaceAll("\\s", "")).mkString(" ")
  val fec = (fechaEmision findAllIn prueba.get).map(_.replaceAll("\\s", "")).mkString(" ")
  val resp = (respoInsc findAllIn prueba.get).map(_.replaceAll("\\s", "")).mkString(" ")
  val imp = (importes findAllIn prueba.get).mkString(" ")
  val puntos = (pruebaPunto findAllIn prueba.get).mkString(" ")
  /*val cod = (codigoBarra findAllIn prueba.get).mkString(" ")
  val cuit2 = cod.take(11)
  val tipoComp = cod.substring(11,13)
  val puntoVenta2 = cod.substring(13,17)
  val cai = cod.substring(17,31)
  val fecha = cod.substring(31,39)
*/
  println("cuit: " + c)
  println("tipo factura: " + fac)
  println("punto venta: " + a)
  println("fecha emision: " + fec)
  println("resp: " + resp)
  println("importes: " + imp)
  println("putnos: " + puntos)
 /* println("codigo de barra : " + cod)
  println("cuit : " + cuit2)
  println("tipo comprobante : " + tipoComp)
  println("punto venta : " + puntoVenta2)
  println("CAI : " + cai)
  println("fecha : " + fecha)*/
  println("factura texto :" + prueba.get)
}


