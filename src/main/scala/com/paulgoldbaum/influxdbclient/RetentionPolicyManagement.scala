package com.paulgoldbaum.influxdbclient

trait RetentionPolicyManagement { self: Database =>
  def createRetentionPolicy(name: String, duration: String, replication: Int, default: Boolean = false) = {
    var stringBuilder = new StringBuilder()
      .append("CREATE RETENTION POLICY ").append(name)
      .append(" ON ").append(databaseName)
      .append(" DURATION ").append(duration)
      .append(" REPLICATION ").append(replication)

    if (default)
      stringBuilder = stringBuilder.append(" DEFAULT")

    queryWithoutResult(stringBuilder.toString())
  }

  def showRetentionPolicies() =
    query("SHOW RETENTION POLICIES ON " + databaseName)

  def dropRetentionPolicy(name: String) =
    query("DROP RETENTION POLICY " + name + " ON " + databaseName)

  def alterRetentionPolicy(name: String, duration: String = null, replication: Int = -1, default: Boolean = false) = {
    var stringBuilder = new StringBuilder()
      .append("ALTER RETENTION POLICY ").append(name)
      .append(" ON ").append(databaseName)
    if (duration != null)
      stringBuilder = stringBuilder.append(" DURATION ").append(duration)

    if (replication > -1)
      stringBuilder = stringBuilder.append(" REPLICATION ").append(replication)

    if (default)
      stringBuilder = stringBuilder.append(" DEFAULT")

    queryWithoutResult(stringBuilder.toString())
  }
}
