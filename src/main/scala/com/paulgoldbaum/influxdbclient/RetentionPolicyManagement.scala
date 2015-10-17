package com.paulgoldbaum.influxdbclient

protected[influxdbclient] trait RetentionPolicyManagement { self: Database =>
  def createRetentionPolicy(name: String, duration: String, replication: Int, default: Boolean) = {
    var stringBuilder = new StringBuilder()
      .append("CREATE RETENTION POLICY \"").append(name)
      .append("\" ON \"").append(databaseName)
      .append("\" DURATION ").append(duration)
      .append(" REPLICATION ").append(replication)

    if (default)
      stringBuilder = stringBuilder.append(" DEFAULT")

    query(stringBuilder.toString())
  }

  def showRetentionPolicies() =
    query("SHOW RETENTION POLICIES ON \"%s\"".format(databaseName))

  def dropRetentionPolicy(name: String) =
    query("DROP RETENTION POLICY \"%s\" ON \"%s\"".format(name, databaseName))

  def alterRetentionPolicy(name: String, duration: String = null, replication: Int = -1, default: Boolean = false) = {
    if (duration == null && replication == -1 && !default)
      throw new InvalidRetentionPolicyParametersException("At least one parameter has to be set")

    var stringBuilder = new StringBuilder()
      .append("ALTER RETENTION POLICY \"").append(name)
      .append("\" ON \"").append(databaseName).append("\"")
    if (duration != null)
      stringBuilder = stringBuilder.append(" DURATION ").append(duration)

    if (replication > -1)
      stringBuilder = stringBuilder.append(" REPLICATION ").append(replication)

    if (default)
      stringBuilder = stringBuilder.append(" DEFAULT")

    query(stringBuilder.toString())
  }

}

class InvalidRetentionPolicyParametersException(str: String) extends Exception(str)
