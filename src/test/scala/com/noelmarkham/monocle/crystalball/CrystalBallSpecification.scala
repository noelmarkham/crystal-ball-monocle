package com.noelmarkham.monocle.crystalball

import monocle._

import scalaz.{Lens => _, _}
import Scalaz._

import org.scalacheck.Properties
import org.scalacheck.Prop.forAll

import shapeless.contrib.scalacheck._
import shapeless.contrib.scalaz.instances._

import scala.concurrent._
import scala.concurrent.duration._

object CrystalBalls {
  import scala.concurrent.ExecutionContext.Implicits.global

  case class Alpha(i: Int)
  case class Beta(s: String, c: Char)
  case class Omega(alpha: Alpha, beta: Beta)

  case class Overall(omega: Omega)

  val omegaToAlphaF = CrystalBall[Omega, Alpha](omega => Future.successful(omega.alpha))(alpha => omega => Future.successful(omega.copy(alpha = alpha)))
  val omegaToBetaF = CrystalBall[Omega, Beta](omega => Future.successful(omega.beta))(beta => omega => Future.successful(omega.copy(beta = beta)))

  val alphaToIntF = CrystalBall[Alpha, Int](alpha => Future.successful(alpha.i))(i => alpha => Future.successful(alpha.copy(i = i)))
  val betaToStringF = CrystalBall[Beta, String](beta => Future.successful(beta.s))(s => beta => Future.successful(beta.copy(s = s)))

  val omegaToIntF = omegaToAlphaF composeFutureLens alphaToIntF
  val omegaToStringF = omegaToBetaF composeFutureLens betaToStringF

  val betaToChar = Lens[Beta, Char](_.c)(c => _.copy(c = c))
  val omegaToCharF = omegaToBetaF composeLens betaToChar

  val overallToOmega = Lens[Overall, Omega](_.omega)(omega => _.copy(omega = omega))
  
  import PLensOps._
  val overallToAlphaF = overallToOmega composeFutureLens omegaToAlphaF
  
}

object CrystalBallSpecification extends Properties("Crystal Ball") {

  import CrystalBalls._

  property("Composed getter works as expected") = forAll { omega: Omega =>
    Await.result(omegaToIntF.get(omega), 10.millis) === omega.alpha.i
  }

  property("Composed setter works as expected") = forAll { (omega: Omega, s: String) =>
    Await.result(omegaToStringF.set(s)(omega), 10.millis).beta.s === s
  }

  property("Get from a Crystal Ball composed with a regular Lens works as expected") = forAll { omega: Omega =>
    Await.result(omegaToCharF.get(omega), 10.millis) === omega.beta.c
  }

  property("Set from a Crystal Ball composed with a regular Lens works as expected") = forAll { (omega: Omega, c: Char) =>
    Await.result(omegaToCharF.set(c)(omega), 10.millis).beta.c === c
  }
  
  property("Get from a regular Lens composed with a Crystal Ball works as expected") = forAll { overall: Overall =>
    Await.result(overallToAlphaF.get(overall), 10.millis) === overall.omega.alpha
  }

  property("Set from a regular Lens composed with a Crystal Ball works as expected") = forAll { (overall: Overall, alpha: Alpha) =>
    Await.result(overallToAlphaF.set(alpha)(overall), 10.millis).omega.alpha === alpha
  }
}
