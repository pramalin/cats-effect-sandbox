package datatypes

import cats.effect.IO
	
object HelloName extends App {

  def putStrlLn(value: String) = IO(println(value))
  val readLn = IO(scala.io.StdIn.readLine)

  /*
		And then we can use that to model interactions with the console in a purely functional way:
	*/

  val hello = for {
    _ <- putStrlLn("What's your name?")
    n <- readLn
    _ <- putStrlLn(s"Hello, $n!")
  } yield ()

  hello.unsafeRunSync()
}