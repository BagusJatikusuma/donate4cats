package id.core.donate4cats.http.view

import scalatags.Text.all.*
import scalatags.Text.TypedTag

// import java.util.UUID

object Homepage {
  
  private def header(): TypedTag[String] =
    // val clientId    = "c110b87666eb419ab4b09f7bcbe9b8fc"
    // val redirectUrl = "http://localhost:8080"
    // val token       = UUID.randomUUID().toString()
    div(
      cls := "px-5 py-2 flex justify-between border-b border-solid border-gray-300",
      div(
        cls := "text-[18px]",
        "Donate 4 Cats"
      ),
      div(
        div(
          a(
            href := s"/signin",
            button(
              cls := "cursor-pointer text-[12px] p-2 px-3 rounded-md border border-blue-500",
              "Sign in"
            )
          )
        )
      )
    )

  def index(): TypedTag[String] =
    html(
      head(
        scalatags.Text.tags2.title("Donate 4 cats"),
        script(src := "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4")
      ),
      div(
        header(),
      )
    )

}
