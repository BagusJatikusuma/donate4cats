package id.core.donate4cats.domain

import cats.syntax.all.*

import neotype.Newtype
import neotype.interop.cats.given

type Name = Name.Type
object Name extends Newtype[String]:
  override def validate(input: String): Boolean | String = 
    if input.isBlank() then "Member name should not be empty"
    else true

  extension (name: Name)
    def asString: String = name.show

type Email = Email.Type
object Email extends Newtype[String]:
  override def validate(input: String): Boolean | String = 
    if input.isBlank() then "Invalid email"
    else true

type Password = Password.Type
object Password extends Newtype[String]:
  override def validate(input: String): Boolean | String = 
    if input.isBlank() then "Password should ne be empty"
    else true

  extension (pass: Password)
    def asString: String = pass.show