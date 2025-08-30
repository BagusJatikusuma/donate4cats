package id.core.donate4cats.http.view

import scalatags.Text.all.*
import scalatags.Text.TypedTag

object SignupPage {
  
  def index(): TypedTag[String] =
    html(
      head(
        scalatags.Text.tags2.title("Donate 4 cats | Signup"),
        script(src := "https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4")
      ),
      body(
        div(
          cls := "flex justify-center mt-[100px]",
          div(
            cls := "w-[600px] space-y-5",
            div(
              h1(
                cls := "text-[20px]",
                "Donate 4 cats | Signup"
              )
            ),
            form(
              div(
                cls := "space-y-3",
                div(
                  cls := "space-y-1",
                  div(label("Your name")),
                  div(
                    input(
                      cls := "bg-blue-100 w-full p-2 text-[12px]",
                      `type` := "text",
                    )
                  )
                ),
                div(
                  cls := "space-y-1",
                  div(label("Email")),
                  div(
                    input(
                      cls := "bg-blue-100 w-full p-2 text-[12px]",
                      `type` := "email",
                    )
                  )
                ),
                div(
                  cls := "space-y-1",
                  div(label("Password")),
                  div(
                    input(
                      cls := "bg-blue-100 w-full p-2 text-[12px]",
                      `type` := "password"
                    )
                  )
                ),
              ),
            ),
            div(
              cls := "space-y-1",
              div(
                button(
                  cls := "bg-blue-500 hover:bg-blue-500 text-white w-[150px] text-left p-2 px-3",
                  "Sign up"
                )
              ),
              div(
                cls := "space-x-1",
                span("already have an account?"), a(href := "/signin", cls := "text-blue-700 hover:underline", "sign in")
              )
            )
          )
        )
      )
    ) 

}
