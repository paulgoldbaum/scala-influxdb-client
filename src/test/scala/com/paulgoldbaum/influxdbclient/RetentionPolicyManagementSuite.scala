package com.paulgoldbaum.influxdbclient

import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class RetentionPolicyManagementSuite extends FunSuite with BeforeAndAfter {

  var database = new Database("_test_database_rp", new HttpClient("localhost", 8086))
  before {
    Await.result(database.create(), 1.second)
  }

  after {
    Await.result(database.drop(), 1.second)
  }

  test("A retention policy can be created and deleted") {
    Await.result(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false), 1.second)
    val policies = Await.result(database.showRetentionPolicies(), 1.second)
    assert(policies.series.head.records.length == 2)
    val policy = policies.series.head.records(1)
    assert(policy("name") == "test_retention_policy")
    assert(policy("duration") == "168h0m0s")
    assert(policy("replicaN") == 1)
    assert(policy("default") == false)

    Await.result(database.dropRetentionPolicy("test_retention_policy"), 1.second)

    val policiesAfterDeleting = Await.result(database.showRetentionPolicies(), 1.second)
    assert(policiesAfterDeleting.series.head.records.length == 1)
  }

  test("A retention policy can be altered") {
    Await.result(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false), 1.second)
    Await.result(database.alterRetentionPolicy("test_retention_policy", "2w", 2, default = true), 1.second)
    val policies = Await.result(database.showRetentionPolicies(), 1.second)
    val policy = policies.series.head.records(1)
    assert(policy("name") == "test_retention_policy")
    assert(policy("duration") == "336h0m0s")
    assert(policy("replicaN") == 2)
    assert(policy("default") == true)
  }

}
