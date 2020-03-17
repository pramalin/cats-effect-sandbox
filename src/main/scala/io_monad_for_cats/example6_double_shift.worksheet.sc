package io_monad_for_cats
import cats.effect.IO

object example6_double_shift {
  import java.io.{BufferedReader, FileReader}
  // import java.io.{BufferedReader, FileReader}

  def readLines(name: String): IO[Vector[String]] = IO {
    val reader = new BufferedReader(new FileReader(name))
    var back: Vector[String] = Vector.empty

    try {
      var line: String = null
      do {
        line = reader.readLine()
        back :+ line
      } while (line != null)
    } finally {
      reader.close()
    }

    back
  }
  // readLines: (name: String)cats.effect.IO[Vector[String]]

  for {
    _ <- IO { println("Name, pls.") }
    name <- IO { Console.readLine }
    lines <- readLines("names.txt")

    _ <- if (lines.contains(name))
      IO { println("You're on the list, boss.") }
    else
      IO { println("Get outa here!") }
  } yield ()

  import java.util.concurrent.Executors
  import scala.concurrent.ExecutionContext


  implicit val Main = ExecutionContext.global
  val BlockingFileIO = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  for {
    _ <- IO { println("Name, pls.") }
    name <- IO { Console.readLine }
    lines <- readLines("names.txt").shift(BlockingFileIO).shift(Main)

    _ <- if (lines.contains(name))
      IO { println("You're on the list, boss.") }
    else
      IO { println("Get outa here!") }
  } yield ()


  readLines("names.txt").shift(BlockingFileIO)
}
