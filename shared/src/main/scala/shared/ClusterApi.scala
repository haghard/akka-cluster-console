package shared

trait ClusterApi {

  def discoveredCluster(): shared.protocol.ClusterInfo

  def clusterProfile(): shared.protocol.ClusterProfile
}