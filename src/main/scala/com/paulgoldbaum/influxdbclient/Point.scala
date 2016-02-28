package com.paulgoldbaum.influxdbclient

case class Point(key: String, timestamp: Long = -1, tags: List[Tag] = List(), fields: List[Field] = List()) {
  def addTag(key: String, value: String) = copy(tags = Tag(key, value) :: tags)

  def addField(key: String, value: String) = copy(fields = StringField(key, value) :: fields)
  def addField(key: String, value: Double) = copy(fields = DoubleField(key, value) :: fields)
  def addField(key: String, value: Long) = copy(fields = LongField(key, value) :: fields)
  def addField(key: String, value: Boolean) = copy(fields = BooleanField(key, value) :: fields)

  def serialize() = {
    val sb = new StringBuilder
    sb.append(escapeKey(key))
    if (tags.nonEmpty) {
      sb.append(",")
      sb.append(tags.map(_.serialize).mkString(","))
    }

    if (fields.nonEmpty) {
      sb.append(" ")
      sb.append(fields.map(_.serialize).mkString(","))
    }

    if (timestamp > 0) {
      sb.append(" ")
      sb.append(timestamp)
    }

    sb.toString()
  }

  private def escapeKey(key: String) = key.replaceAll("([ ,])", "\\\\$1")
}
