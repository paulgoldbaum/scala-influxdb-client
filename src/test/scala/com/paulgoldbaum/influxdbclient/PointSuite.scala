package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

class PointSuite extends FunSuite {

  test("Minimal point is serialized correctly") {
    val point = Point("key")
    assert(point.serialize() == "key")
  }

  test("Complete points are serialized correctly") {
    val point = Point("measurement", 1234567890l)
      .addTag("tag_key2", "tag_value2")
      .addTag("tag_key1", "tag_value1")
      .addField("field_key4", 12.34)
      .addField("field_key3", true)
      .addField("field_key2", 2)
      .addField("field_key1", "field_value1")

    assert(point.serialize ==
      "measurement,tag_key1=tag_value1,tag_key2=tag_value2 field_key1=\"field_value1\",field_key2=2i,field_key3=true,field_key4=12.34 1234567890")
  }

  test("Tags are serialized correctly") {
    assert(Tag("key", "value").serialize == "key=value")
  }

  test("Tags are escaped correctly") {
    assert(Tag("ke y", "va lue").serialize == "ke\\ y=va\\ lue")
    assert(Tag("ke,y", "va,lue").serialize == "ke\\,y=va\\,lue")
    assert(Tag("ke=y", "va=lue").serialize == "ke\\=y=va\\=lue")
  }

  test("String fields are serialized correctly") {
    assert(StringField("key", "value").serialize == "key=\"value\"")
  }

  test("Double fields are serialized correctly") {
    assert(DoubleField("key", 12.123).serialize == "key=12.123")
  }

  test("Long fields are serialized correctly") {
    assert(LongField("key", 12123l).serialize == "key=12123i")
  }

  test("Boolean fields are serialized correctly") {
    assert(BooleanField("key", value = true).serialize == "key=true")
  }

  test("Fields are escaped correctly") {
    assert(StringField("ke y", "a v=al\"ue").serialize == "ke\\ y=\"a v=al\\\"ue\"")
    assert(LongField("key,", 12123l).serialize == "key\\,=12123i")
  }
}
