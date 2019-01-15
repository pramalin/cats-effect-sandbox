package io_monad_for_cats

object example2_sync extends App {

  import cats.effect.IO

  val program = for {
    _ <- IO { println("Welcome to Scala!  What's your name?") }
    name <- IO { Console.readLine }
    _ <- IO { println(s"Well hello, $name!") }
  } yield ()

  program.unsafeRunSync()
}
