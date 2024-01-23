package io.scala.views

import io.scala.Lexicon
import io.scala.data.ScheduleInfo
import io.scala.data.TalksInfo
import io.scala.domaines.*
import io.scala.modules.*
import io.scala.modules.elements.*
import io.scala.utils.Screen
import io.scala.utils.Screen.screenVar

import com.raquo.laminar.api.L.{*, given}

case object ScheduleView extends SimpleView {
  val selectedDay: Var[ConfDay] = Var(ConfDay.Thursday)

  lazy val globalHours: Div =
    def renderHours(name: String, hours: Time*) =
      div(
        className := "row",
        span(name),
        hours.map(_.render(span))
      )
    div(
      Title.small("Hours"),
      div(
        className := "hours",
        div(
          className := "column-header",
          span(),
          span("Thursday"),
          span("Friday")
        ),
        renderHours("Opening", Lexicon.Schedule.opening: _*),
        renderHours("First talk", Lexicon.Schedule.firstTalk: _*),
        renderHours("Lunch", Lexicon.Schedule.lunch: _*),
        renderHours("End of talks", Lexicon.Schedule.endOfTalks: _*),
        renderHours("Community party", Lexicon.Schedule.communityParty)
      )
    )

  def renderSmall(eventsByDay: Map[ConfDay, List[Event]]) =
    div(
      className := "schedule small",
      div(
        className := "tabs",
        ConfDay.values.map { day =>
          div(
            button(
              onClick --> { _ => selectedDay.set(day) },
              h2(day.toString())
            ),
            Line(margin = 8, size = 3, kind = LineKind.Colored).amend(display <-- selectedDay.signal.map { d =>
              if d == day then "flex"
              else "none"
            })
          )
        }
      ),
      div(
        ConfDay.values.map { day =>
          div(
            className := "content",
            display <-- selectedDay.signal.map { d =>
              if d == day then "flex"
              else "none"
            },
            children <-- selectedDay.signal.map {
              case i if i == day =>
                ScheduleDay(eventsByDay.get(day).getOrElse(Seq.empty)).body
              case _ => Seq(emptyNode)
            }
          )
        }
      )
    )

  def renderLarge(eventsByDay: Map[ConfDay, List[Event]]) =
    div(
      className := "schedule large",
      div(
        className := "tabs",
        ConfDay.values.map: day =>
          div(
            h2(day.toString()),
            Line(margin = 8, size = 3, kind = LineKind.Colored)
          )
      ),
      div(
        ConfDay.values.map: day =>
          div(
            className := "content",
            ScheduleDay(eventsByDay.get(day).getOrElse(Seq.empty)).body
          )
      )
    )

  def renderSchedule(eventsByDay: Map[ConfDay, List[Event]]) =
    screenVar.signal.map {
      case Screen.Desktop => renderLarge(eventsByDay)
      case _              => renderSmall(eventsByDay)
    }

  def bodyContent(eventsByDay: Map[ConfDay, List[Event]]): HtmlElement =
    sectionTag(
      className := "container",
      Title("Schedule"),
      p(
        "The schedule will be available soon.",
        className := "catch-phrase"
      ),
      Line(margin = 55),
      globalHours,
      Line(margin = 55),
      child <-- renderSchedule(eventsByDay)
    )

  def body(withDraft: Boolean): HtmlElement =
    val schedule =
      if withDraft then ScheduleInfo.schedule
      else ScheduleInfo.blankSchedule
    val eventsByDay: Map[ConfDay, List[Event]] =
      schedule
        .filter(_.day != null)
        .groupBy { _.day }
    bodyContent(eventsByDay)

  override def title: String = "Schedule"
}
