package console.components

import console.components.Bootstrap.{Button, Modal}
import console.style.{GlobalStyles, Icon}
import japgolly.scalajs.react.{BackendScope, Callback, ReactComponentB, ReactEventI}

//import japgolly.scalajs.react.vdom.all._
//import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom._
import japgolly.scalajs.react.vdom.prefix_<^._

object ClusterAddComponent {

  @inline private def bss = GlobalStyles.bootstrapStyles

  case class HostPortForm(host: String, port: String)

  case class ClusterForm(name: String, selfHost:String, seeds: List[HostPortForm])

  case class EditClusterProps(clusterForm: ClusterForm, editHandler: ClusterForm => Unit, closeForm: () => Unit)
  case class State(clusterForm: ClusterForm, seeds: Int, portValid: Boolean, submitEnabled: Boolean)

  class Backend(scope: BackendScope[EditClusterProps, State]) {
    def hide(): Callback = {
      ???
    }

    //import japgolly.scalajs.react.vdom.prefix_<^._
    def updateClusterSeedPort(i: Int, e: ReactEventI): Callback = {
      ???
    }

    def updateClusterName(e: ReactEventI): Callback = {
      ???
    }

    def updateClusterSelfHost(e: ReactEventI): Callback = {
      ???
      /*
      t.modState { s =>
        val newState = s.copy(clusterForm = ClusterForm(s.clusterForm.name, e.currentTarget.value, s.clusterForm.seeds))
        updateClusterForm(newState.clusterForm)
        newState.copy(submitEnabled = getSubmitEnabled(newState))
      }*/
    }

    def updateClusterSeedHost(index: Int)(e: ReactEventI): Callback = {
      ???
    }
  }

  def component = ReactComponentB[EditClusterProps]("ClusterForm")
    .initialState_P(p => State(p.clusterForm, 0, true, false))
    .backend(new Backend(_))
    .renderPS { (scope, props, state) =>
      <.span(
        <.button(
          ^.onClick --> scope.backend.hide(),
          ^.cls := "pull-right",
          //^.icon := Icon.close,
          "Ok"
        )
      )


      /*
      Modal(Modal.Props(
        { hide =>
          span(
            button(
              `type` := "button",
              cls := "pull-right",
              onClick --> hide
              //Icon.close
            )
            //h4(color := "black",  "Discover Cluster")
          )
        },
        { hide => span(Button(Button.Props({ /*props.editHandler(state.clusterForm);*/ hide }), "OK")) },
        { scope.backend.hide() }),
        form(
          div(
            cls := "form-group col-md-8",
            label("Cluster Name"),
            input(`type` := "text", cls := "form-control", value := state.clusterForm.name, onChange ==> scope.backend.updateClusterName)
          )*/

          /*,
          div(cls := "col-md-12 form-group") {
            props.clusterForm.seeds.zipWithIndex.map { case (seed, index) =>
              div(cls := "row", key := s"$index")(
                div(cls := "form-group col-md-4")(
                  label("App host"),
                  input(
                    tpe := "text", cls := "form-control", value := state.clusterForm.selfHost,
                    onChange ==> scope.backend.updateClusterSelfHost
                  )
                ),
                div(cls := "form-group col-md-4")
                    label("Seed"),
                    input(`type` := "text", cls := "form-control", value := state.clusterForm.seeds.zipWithIndex
                      .find { case (x, i) => i == index }.map(_._1.host).getOrElse(""),
                      onChange ==> scope.backend.updateClusterSeedHost(index)
                  ),
                  div(
                    cls := s"form-group col-md-2 ${if (!state.portValid) "has-error" else ""}"
                    label("Port"),
                    input(`type` := "text", cls := "form-control",
                      value := state.clusterForm.seeds.zipWithIndex.find { case (x, i) => i == index }.map(_._1.port.toString).getOrElse(""),
                      onChange ==> { (e:ReactEventI) => scope.backend.updateClusterSeedPort(index, e) }
                    )
                  )
                )
              )
            }
          }
        )
      )*/
    }
    .componentDidMount { s =>
      s.modState(s0 => s0.copy(clusterForm = s.props.clusterForm))
    }
    .build

  def apply(editHandler: ClusterForm => Unit, closeForm: () => Unit) = {
    //val proxy: Proxy = AjaxClient[shared.ClusterApi]
    //store.getClusterForm()
    component(EditClusterProps(null, editHandler, closeForm))
  }
}
