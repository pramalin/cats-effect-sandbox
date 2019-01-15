package io_monad_for_cats

import cats.effect.IO

object example3_sync_monolyth extends App {
  val program = IO {
    println("Welcome to Scala!  What's your name?")
    val name = Console.readLine
    println(s"Well hello, $name!")
  }

  program.unsafeRunSync
}
