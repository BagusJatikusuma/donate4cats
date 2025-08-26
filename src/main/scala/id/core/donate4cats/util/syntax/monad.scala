package id.core.donate4cats.util.syntax.monad

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import io.circe.DecodingFailure

private case class Early[A](value: A) extends Throwable

def finishWith[F[_]: MonadThrow, A](value: A): F[Unit] =
  MonadThrow[F].raiseError(Early(value))

def failWith[F[_]: MonadThrow, A](value: A): F[Unit] = 
  MonadThrow[F].raiseError(Early(value))

def errorWith[F[_]: MonadThrow, A](error: => Throwable): F[A] = 
  MonadThrow[F].raiseError(error)

def continue[F[_]: Applicative]: F[Unit] = ().pure[F] 

def continueWith[F[_]: Applicative, A](value: A): F[A] = Applicative[F].pure(value)

def imperative[F[_]: MonadThrow, A](ops: F[A]): F[A] = {
  ops.handleErrorWith {
    case Early(v: A @unchecked) => MonadThrow[F].pure(v)
    case exc                    => MonadThrow[F].raiseError(exc)
  }
}

final case class WhenMonad[F[_]: MonadThrow, A](cond: Boolean):
  def failWith(x: A): F[Unit] =
    if (cond) then MonadThrow[F].raiseError(Early(x))
    else ().pure[F]

  def finishWith(x: A): F[Unit] = failWith(x)

  def returnWith(value: A): F[Unit] = failWith(value)

  def throwError(exc: => Throwable): F[Unit] =
    if cond then
      MonadThrow[F].raiseError(exc)
    else 
      ().pure[F]

def when[F[_]: MonadThrow, A](cond: Boolean): WhenMonad[F, A] = 
  WhenMonad(cond)

def whenNot[F[_]: MonadThrow, A](cond: Boolean): WhenMonad[F, A] = 
  WhenMonad(!cond)

def run[F[_]: Sync, A](ops: => A): F[A] = Sync[F].delay(ops)

def runBlock[F[_]: Sync, A](ops: => A): F[A] = Sync[F].blocking(ops)

extension [F[_]: MonadThrow, A](ops: F[A])
  def trace: F[A] = ops.attempt.flatMap {
    case Left(err)    => 
      val ex = new Exception(s"Converted error: ${err.getMessage()}", err)
      MonadThrow[F].raiseError(ex)
    case Right(value) => value.pure[F]  
  }

extension [A](either: Either[DecodingFailure, A])
  def toValidatedNelField(field: String): ValidatedNel[String, A] =
    either.left.map(df => s"${df.getMessage.replace(s"DecodingFailure at .$field: ", "")}").toValidatedNel

extension [A](either: Either[String, A])
  def toValidatedNelFieldString(field: String): ValidatedNel[String, A] =
    either.toValidatedNel
