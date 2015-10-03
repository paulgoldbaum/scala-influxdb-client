package com.paulgoldbaum.influxdbclient

import org.scalatest.FunSuite

class PointSuite extends FunSuite {

  test("Complete points are serialized correctly") {
    val point = Point("measurement", 1234567890l)
      .addTag("tag_key2", "tag_value2")
      .addTag("tag_key1", "tag_value1")
      .addField("field_key2", 2)
      .addField("field_key1", "field_value1")

    assert(point.serialize ==
      "measurement,tag_key1=tag_value1,tag_key2=tag_value2 field_key1=\"field_value1\",field_key2=2i 1234567890")
  }

  test("Tags are serialized correctly") {
    assert(Tag("key", "value").serialize == "key=value")
  }

  test("Tags are escaped correctly") {
    assert(Tag("ke y", "va lue").serialize == "ke\\ y=va\\ lue")
    assert(Tag("ke,y", "va,lue").serialize == "ke\\,y=va\\,lue")
    assert(Tag("ke=y", "va=lue").serialize == "ke\\=y=va\\=lue")
  }

  test("Fields are serialized correctly") {
    assert(StringField("key", "value").serialize == "key=\"value\"")
    assert(DoubleField("key", 12.123).serialize == "key=12.123")
    assert(LongField("key", 12123l).serialize == "key=12123i")
    assert(BooleanField("key", value = true).serialize == "key=t")
  }

  test("Fields are escaped correctly") {
    assert(StringField("ke y", "a v=al\"ue").serialize == "ke\\ y=\"a v=al\\\"ue\"")
    assert(LongField("key,", 12123l).serialize == "key\\,=12123i")
  }

}
