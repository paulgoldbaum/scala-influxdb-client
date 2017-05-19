package com.paulgoldbaum.influxdbclient

object Parameter {

  object Precision {
    sealed abstract class Precision(str: String) extends Serializable {
      override def toString = str
    }

    case object NANOSECONDS extends Precision("ns")
    case object MICROSECONDS extends Precision("u")
    case object MILLISECONDS extends Precision("ms")
    case object SECONDS extends Precision("s")
    case object MINUTES extends Precision("m")
    case object HOURS extends Precision("h")
  }

  object Consistency {
    sealed abstract class Consistency(str: String) extends Serializable {
      override def toString = str
    }

    case object ONE extends Consistency("one")
    case object QUORUM extends Consistency("quorum")
    case object ALL extends Consistency("all")
    case object ANY extends Consistency("any")
  }

}
