val str = "0.00 20.20.30 A20.200,00A a20.400.400,00A 20.220,00"

val cuit = "\\d{2}-\\d{8}-\\d".r
val tipoFactura = "[ABCM]".r
val puntoVenta = "\\d{4}\\s?-\\s?\\d{8}".r
val fechaEmision = "\\d{2}\\s?[-/]\\s?\\d{2}\\s?[-/]\\s?\\d{2,4}".r
val importes =  "(?<![\\.,])(\\d{1,3}([.,]\\d{3})*)[.,]\\d{2}(?<!(\\.3))".r




val fec = (fechaEmision findAllIn str).mkString(" ")

(cuit findAllIn str).mkString(", ")
(tipoFactura findAllIn str).mkString(", ")
(importes findAllIn str).mkString(" ")
