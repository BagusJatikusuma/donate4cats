package id.core.donate4cats.http.view

import id.core.donate4cats.domain.Member

import scalatags.Text.all.*
import scalatags.Text.TypedTag

object MemberHomePage {
  
  def index(member: Member): TypedTag[String] = 
    html(
      head(
        scalatags.Text.tags2.title("Donate 4 cats"),
        script(src := "/assets/js/member_homepage.js"),
        script(src := "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4")
      ),
      body(
        div(
          cls := "container mx-auto",
          div(
            cls := "py-2 flex justify-end",
            button(
              onclick := "signout()",
              cls := "cursor-pointer hover:shadow-lg bg-red-300 p-2",
              "Sign out"
            )
          ),
          div(
            cls := "py-5 flex items-center justify-between",
            div(
              cls := "cursor-pointer rounded-lg hover:border hover:border-blue-300 hover:shadow-lg p-3 space-x-2 flex items-center",
              div(
                cls := "p-3 px-5 bg-red-300 rounded-full",
                s"${member.name.asString.head.toUpper}"
              ),
              div(s"${member.name}")
            ),
            div(
              img(src := "/assets/images/cats-action.png", width := "200px")
            ),
          ),
          s"Welcome home ${member.name}"
        )
      )
    )

}
