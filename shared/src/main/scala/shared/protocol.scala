package shared

import upickle.default.macroRW
import upickle.default.{ReadWriter => RW}

import scala.collection.immutable.Seq

object protocol {

  case class SignInResponse(login: String, photo: String)

  case class ClusterInfo(name: String, seedNodes: Seq[String])

  object ClusterInfo {
    implicit val rw: RW[ClusterInfo] = macroRW
  }

  sealed trait Mode {
    def name: String

    def isActive(m: Mode): Boolean = this == m
  }

  object Mode {
    def fromString(s: String) = s match {
      case Members.name => Members
      case Roles.name   => Roles
      case Nodes.name   => Nodes
      case _            => Members
    }
  }

  case object Members extends Mode {
    override val name = "Members"
  }

  case object Roles extends Mode {
    override val name = "Roles"
  }

  case object Nodes extends Mode {
    override val name = "Nodes"
  }

  sealed trait NodeState
  object NodeState {
    def fromStr(state: String): NodeState =
      state match {
        case "" => Up
      }

    case object Up extends NodeState

    case object Unreachable extends NodeState

    case object Removed extends NodeState

    case object Exited extends NodeState

    implicit val rw: RW[NodeState] = macroRW

  }

  case class ClusterMember(address: HostPort, roles: Set[String], state: NodeState) {
    def label = address.label + s" roles[${roles.mkString(",").map(r => r)}] status[$state]"

    def labelSimple = address.label
  }

  object ClusterMember {
    implicit val rw: RW[ClusterMember] = macroRW
  }

  case class HostPort(host: String, port: Int) {
    def label = host + ":" + port
  }

  object HostPort {
    implicit val rw: RW[HostPort] = macroRW
  }

  case class ClusterProfile(system: String, members: Set[ClusterMember])

  object ClusterProfile {
    implicit val rw: RW[ClusterProfile] = macroRW
  }

  case class ClusterNode(host: String, port: Int, roles: String, status: String)
}
