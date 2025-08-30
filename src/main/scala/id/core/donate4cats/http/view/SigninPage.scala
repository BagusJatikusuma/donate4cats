package id.core.donate4cats.http.view

import scalatags.Text.all.*
import scalatags.Text.TypedTag

object SigninPage {
  
  def index(): TypedTag[String] = 
    html(
      head(
        scalatags.Text.tags2.title("Donate 4 cats | Signin"),
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
                "Donate 4 cats | Signin"
              )
            ),
            form(
              div(
                cls := "space-y-3",
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
                  "Sign in"
                )
              ),
              div(
                cls := "space-x-1",
                span("not have an account?"), a(href := "/signup", cls := "text-blue-700 hover:underline", "create one")
              )
            )
          )
        )
      )
    )

}
