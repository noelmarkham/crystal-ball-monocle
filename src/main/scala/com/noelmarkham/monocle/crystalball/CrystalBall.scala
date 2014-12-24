package com.noelmarkham.monocle.crystalball

import monocle._
import monocle.PLens

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

final class PCrystalBall[S, T, A, B] private[crystalball](val get: S => Future[A], val set: B => S => Future[T])(implicit ec: ExecutionContext) { self =>

  def composeLens[C, D](other: PLens[A, B, C, D]): PCrystalBall[S, T, C, D] = {
    val newGet: S => Future[C] = s => get(s).map(a => other.get(a))

    val newSet: D => S => Future[T] = d => s => for {
      newB <- get(s).map(a => other.set(d)(a))
      newT <- set(newB)(s)
    } yield newT

    new PCrystalBall(newGet, newSet)
  }

  def composeFutureLens[C, D](other: PCrystalBall[A, B, C, D]): PCrystalBall[S, T, C, D] = {
    val newGet: S => Future[C] = s => for {
      a <- get(s)
      c <- other.get(a)
    } yield c

    val newSet: D => S => Future[T] = d => s => for {
      a <- get(s)
      newB <- other.set(d)(a)
      newT <- set(newB)(s)
    } yield newT

    new PCrystalBall[S, T, C, D](newGet, newSet)
  }
}

object PLensOps {
  implicit class PLensExtension[S, T, A, B](lens: PLens[S, T, A, B])(implicit ec: ExecutionContext) {
    def composeFutureLens[C, D](other: PCrystalBall[A, B, C, D]): PCrystalBall[S, T, C, D] = {
      val newGet: S => Future[C] = s => other.get(lens.get(s))
      val newSet: D => S => Future[T] = d => s => other.set(d)(lens.get(s)).map(newB => lens.set(newB)(s))

      new PCrystalBall[S, T, C, D](newGet, newSet)
    }
  }
}

object PCrystalBall {

  def apply[S, T, A, B](get: S => Future[A])(set: B => S => Future[T])(implicit ec: ExecutionContext): PCrystalBall[S, T, A, B] =
    new PCrystalBall(get, set)
}

object CrystalBall {

  def apply[S, A](get: S => Future[A])(set: A => S => Future[S])(implicit ec: ExecutionContext): CrystalBall[S, A] =
    new PCrystalBall(get, set)
}
