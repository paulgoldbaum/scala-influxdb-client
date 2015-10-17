package com.paulgoldbaum.influxdbclient

import org.scalatest.BeforeAndAfter

class RetentionPolicyManagementSuite extends CustomTestSuite with BeforeAndAfter {

  var database = new Database("_test_database_rp", new HttpClient("localhost", 8086, databaseUsername, databasePassword))
  before {
    await(database.create())
  }

  after {
    await(database.drop())
  }

  test("A retention policy can be created") {
    await(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = true))
    val policies = await(database.showRetentionPolicies())
    assert(policies.series.head.records.length == 2)
    val policy = policies.series.head.records(1)
    assert(policy("name") == "test_retention_policy")
    assert(policy("duration") == "168h0m0s")
    assert(policy("replicaN") == 1)
    assert(policy("default") == true)
  }

  test("A retention policy can be created and deleted") {
    await(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false))
    await(database.dropRetentionPolicy("test_retention_policy"))

    val policiesAfterDeleting = await(database.showRetentionPolicies())
    assert(policiesAfterDeleting.series.head.records.length == 1)
  }

  test("A retention policy's duration can be altered") {
    await(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false))
    await(database.alterRetentionPolicy("test_retention_policy", "2w"))
    val policies = await(database.showRetentionPolicies())
    val policy = policies.series.head.records(1)
    assert(policy("name") == "test_retention_policy")
    assert(policy("duration") == "336h0m0s")
  }

  test("A retention policy's replication can be altered") {
    await(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false))
    await(database.alterRetentionPolicy("test_retention_policy", replication = 2))
    val policies = await(database.showRetentionPolicies())
    val policy = policies.series.head.records(1)
    assert(policy("name") == "test_retention_policy")
    assert(policy("replicaN") == 2)
  }

  test("A retention policy's defaultness can be altered") {
    await(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false))
    await(database.alterRetentionPolicy("test_retention_policy", default = true))
    val policies = await(database.showRetentionPolicies())
    val policy = policies.series.head.records(1)
    assert(policy("name") == "test_retention_policy")
    assert(policy("default") == true)
  }

  test("At least one parameter has to be altered") {
    await(database.createRetentionPolicy("test_retention_policy", "1w", 1, default = false))
    try {
      await(database.alterRetentionPolicy("test_retention_policy"))
      fail("Exception was not thrown")
    } catch {
      case e: InvalidRetentionPolicyParametersException => // expected
    }
  }

}
