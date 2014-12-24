package com.noelmarkham.monocle

package object crystalball {
  type CrystalBall[S, A] = PCrystalBall[S, S, A, A]
}
