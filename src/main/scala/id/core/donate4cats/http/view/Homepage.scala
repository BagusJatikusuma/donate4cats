package id.core.donate4cats.http.view

import id.core.donate4cats.domain.Member

import scalatags.Text.all.*
import scalatags.Text.TypedTag

object Homepage {
  
  private def header(member: Option[Member] = None): TypedTag[String] =
    div(
      cls := "px-5 py-2 flex justify-between border-b border-solid border-gray-300",
      div(
        cls := "text-[18px]",
        "Donate 4 Cats"
      ),
      div(
        div {
          member match
            case None =>
              a(
                href := s"/signin",
                button(
                  cls := "cursor-pointer text-[12px] p-2 px-3 rounded-md border border-blue-500",
                  "Sign in"
                )
              )
            case Some(mbr) =>
              a(
                href := s"/private/home",
                button(
                  cls := "cursor-pointer text-[12px] p-2 px-3 rounded-md border border-blue-500",
                  "My Home"
                )
              )
        }
      )
    )

  def index(
    member: Option[Member] = None
  ): TypedTag[String] =
    html(
      head(
        scalatags.Text.tags2.title("Donate 4 cats"),
        script(src := "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4")
      ),
      div(
        header(member),
      )
    )

}
